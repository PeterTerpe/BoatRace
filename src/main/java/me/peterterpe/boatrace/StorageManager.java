package me.peterterpe.boatrace;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.logging.Level;

public class StorageManager {
    private static StorageManager instance;
    private final BoatRace plugin = BoatRace.getInstance();

    // MySQL config
    private boolean useMySQL;
    private String host, database, user, password;
    private int port;
    File tracksDir = new File(plugin.getDataFolder(), "tracks");
    private Connection sqlConnection;
    private File yamlFile;
    private FileConfiguration yamlConfig;

    private StorageManager() {}

    public static StorageManager getInstance() {
        if (instance == null) instance = new StorageManager();
        return instance;
    }

    public void setup() {
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveDefaultConfig();

        useMySQL = plugin.getConfig().getBoolean("mysql-enabled", false);
        if (useMySQL) {
            host = plugin.getConfig().getString("mysql-host", "localhost");
            port = plugin.getConfig().getInt("mysql-port", 3306);
            database = plugin.getConfig().getString("mysql-database", "boatrace");
            user = plugin.getConfig().getString("mysql-user", "root");
            password = plugin.getConfig().getString("mysql-password", "");
            initMySQL();
        } else {
            if (!tracksDir.exists()) {
                tracksDir.mkdir();
            }
            yamlFile = new File(tracksDir, "tracks.yml");
            yamlConfig = YamlConfiguration.loadConfiguration(yamlFile);
        }
    }

    private void initMySQL() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                sqlConnection = DriverManager.getConnection(
                        "jdbc:mysql://" + host + ":" + port + "/" + database + 
                        "?useSSL=false&autoReconnect=true",
                        user, password);
                plugin.getLogger().info("[BoatRace] MySQL connected!");
                createTables();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "[BoatRace] MySQL init failed", e);
            }
        });
    }

    private void createTables() throws SQLException {
        try (Statement st = sqlConnection.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS boatrace_tracks (
                  track_name VARCHAR(50) PRIMARY KEY,
                  data TEXT NOT NULL
                )""");
        }
    }

    public void loadAll() {
        setup();
        for (File file : tracksDir.listFiles((d, name) -> name.endsWith(".json"))) {
            RaceTrack track = RaceTrackManager.getInstance().deserialize(file.getName());
            RaceTrackManager.getInstance().register(track);
        }
    }

    public void saveAll() {
        if (useMySQL) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                RaceTrackManager.getInstance().getAll().forEach(this::saveTrack)
            );
        } else {
            RaceTrackManager.getInstance().getAll().forEach(this::saveTrack);
            try {
                yamlConfig.save(yamlFile);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error saving YAML storage", e);
            }
        }
    }

    public void saveTrack(RaceTrack track) {
        String name = track.getName();
        String json = RaceTrackManager.getInstance().serialize(track);

        if (useMySQL) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (PreparedStatement ps = sqlConnection.prepareStatement(
                        "REPLACE INTO boatrace_tracks(track_name, data) VALUES (?, ?)")) {
                    ps.setString(1, name);
                    ps.setString(2, json);
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to save track " + name, ex);
                }
            });
        } else {
            RaceTrackManager.getInstance().serialize(track);
            yamlConfig.set(name + ".data", json);
            List<RaceResult> top5 = track.getTopTimes();
            for (int i = 0; i < top5.size(); i++) {
                RaceResult result = top5.get(i);
                String path = name + ".top5." + (i + 1);
                yamlConfig.set(path + ".name", result.getPlayerName());
                yamlConfig.set(path + ".time", result.getTimeInMs());
            }
            try {
                yamlConfig.save(yamlFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed saving YAML track " + name, e);
            }
        }
    }
    public void deleteTrack(RaceTrack track) {
        String name = track.getName();
        yamlConfig.set(name, null);
        try {
            yamlConfig.save(yamlFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed writing track deletion to file: " + name, e);
        }
    }
}