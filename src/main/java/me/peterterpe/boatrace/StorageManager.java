package me.peterterpe.boatrace;

import org.bukkit.Bukkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class StorageManager {
    private static StorageManager instance;
    private final BoatRace plugin = BoatRace.getInstance();

    File tracksDir = new File(plugin.getDataFolder(), "tracks");
    private StorageManager() {}

    public static StorageManager getInstance() {
        if (instance == null) instance = new StorageManager();
        return instance;
    }

    public void setup() {
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveDefaultConfig();
        if (!tracksDir.exists()) {
            tracksDir.mkdir();
        }
    }

    public void loadAll() {
        setup();
        for (File file : tracksDir.listFiles((d, name) -> name.endsWith(".json"))) {
            RaceTrack track = RaceTrackManager.getInstance().deserialize(tracksDir + "/" + file.getName());
            RaceTrackManager.getInstance().register(track);
        }
    }

    public void saveAll() {
        RaceTrackManager.getInstance().getAll().forEach(this::saveTrack);
    }

    public void saveTrack(RaceTrack track) {
        String name = track.getName();
        String jString = RaceTrackManager.getInstance().serialize(track, name+".json");
        File trackFile = new File(tracksDir, name+".json");
        try (Writer writer = new FileWriter(trackFile)) {
            writer.write(jString);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Unable to save track '" + name + "': " + e.getMessage());
        }
    }
    public boolean deleteTrack(RaceTrack track) {
        String name = track.getName();
        File trackFile = new File(tracksDir, name);
        return trackFile.delete();
    }
}