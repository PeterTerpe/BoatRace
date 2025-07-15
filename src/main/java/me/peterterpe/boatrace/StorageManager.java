package me.peterterpe.boatrace;

import org.bukkit.Bukkit;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationStore;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class StorageManager {
    private static StorageManager instance;
    private final BoatRace plugin = BoatRace.getInstance();

    File tracksDir = new File(plugin.getDataFolder(), "tracks");
    File playerDir = new File(plugin.getDataFolder(), "players");
    File localesDir = new File(plugin.getDataFolder(), "locales");
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
        if (!playerDir.exists()) {
            playerDir.mkdir();
        }
        if (!localesDir.exists()) {
            localesDir.mkdirs();
        }
    }

    public void loadAll() {
        setup();
        for (File file : tracksDir.listFiles((d, name) -> name.endsWith(".json"))) {
            RaceTrack track = RaceTrackManager.getInstance().deserialize(tracksDir + "/" + file.getName());
            RaceTrackManager.getInstance().register(track);
            RaceTrackManager.getInstance().updateLeaderboardHologram(track);
        }
        for (File file : playerDir.listFiles((d, name) -> name.endsWith(".json"))) {
            PersonalRecords record = PersonalRecordsManager.getInstance().deserialize(playerDir + "/" + file.getName());
            PersonalRecordsManager.getInstance().loadRecord(record);
        }
        loadLocales();
    }

    public void saveAll() {
        RaceTrackManager.getInstance().getAll().forEach(this::saveTrack);
        PersonalRecordsManager.getInstance().getAll().forEach(this::savePlayerRecords);
    }

    public void saveTrack(RaceTrack track) {
        String name = track.getName();
        String jString = RaceTrackManager.getInstance().serialize(track);
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

    public void savePlayerRecords(PersonalRecords records) {
        String jString = PersonalRecordsManager.getInstance().serialize(records);
        String uString = records.getPlayerID().toString();
        File recordFile = new File(playerDir, records.getPlayerID().toString()+".json");
        try (Writer writer = new FileWriter(recordFile)) {
            writer.write(jString);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Unable to save player file '" + uString + "': " + e.getMessage());
        }
    }

    private void loadLocales() {
        if (!localesDir.exists() || !localesDir.isDirectory()) return;
        TranslationStore.StringBased<MessageFormat> store = TranslationStore.messageFormat(Key.key("namespace:value"));
        for (File file : localesDir.listFiles((d, n) -> n.startsWith("Bundle_") && n.endsWith(".properties"))) {
            Locale locale = parseLocaleFromName(file.getName());
            try (FileInputStream fis = new FileInputStream(file)) {
                ResourceBundle bundle = new PropertyResourceBundle(fis);
                store.registerAll(locale, bundle, true);
                Bukkit.getLogger().info("Loaded locale " + locale + " from " + file.getName());
            } catch (IOException ex) {
                Bukkit.getLogger().warning("Failed to load " + file.getName() + ": " + ex.getMessage());
            }
        }
        GlobalTranslator.translator().addSource(store);
    }

    private Locale parseLocaleFromName(String filename) {
        String core = filename.substring("Bundle_".length(), filename.length() - ".properties".length());
        String[] parts = core.split("_");
        try {
            Locale.Builder builder = new Locale.Builder().setLanguage(parts[0]);
            if (parts.length >= 2) {
                builder.setRegion(parts[1]);
            }
            return builder.build();
        } catch (IllformedLocaleException e) {
            Bukkit.getLogger().warning("Invalid locale in " + filename + ": " + e.getMessage());
            return Locale.getDefault();
        }
    }
}