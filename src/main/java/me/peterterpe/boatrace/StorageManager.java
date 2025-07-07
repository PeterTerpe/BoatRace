package me.peterterpe.boatrace;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;

public class StorageManager {
    private static StorageManager instance;
    private final BoatRace plugin = BoatRace.getInstance();

    // MySQL config
    private boolean useMySQL;
    private String host, database, user, password;
    private int port;

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
            yamlFile = new File(plugin.getDataFolder(), "tracks.yml");
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
        if (useMySQL) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (Statement st = sqlConnection.createStatement();
                     ResultSet rs = st.executeQuery("SELECT track_name, data FROM boatrace_tracks")) {
                    while (rs.next()) {
                        String name = rs.getString("track_name");
                        String json = rs.getString("data");
                        plugin.getLogger().info("[BoatRace] Load track " + name);
                        RaceTrack track = RaceTrackManager.getInstance().deserialize(json);
                        RaceTrackManager.getInstance().register(track);
                    }
                } catch (SQLException ex) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to load tracks from MySQL", ex);
                }
            });
        } else {
            if (!yamlFile.exists()) {
                plugin.saveResource("tracks.yml", false);
            }
            yamlConfig = YamlConfiguration.loadConfiguration(yamlFile);
            for (String name : yamlConfig.getKeys(false)) {
                String json = yamlConfig.getString(name + ".data");
                RaceTrack track = RaceTrackManager.getInstance().deserialize(json);
                RaceTrackManager.getInstance().register(track);
            }
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
            yamlConfig.set(name + ".data", json);
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