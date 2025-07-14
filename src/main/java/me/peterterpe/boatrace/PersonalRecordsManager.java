package me.peterterpe.boatrace;

import java.io.FileReader;
import java.io.Reader;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.peterterpe.boatrace.adapters.InstantAdapter;

public class PersonalRecordsManager {
    private Map<UUID, PersonalRecords> playerRecords = new ConcurrentHashMap<>();
    private static PersonalRecordsManager instance;
    Gson gson = new GsonBuilder().setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .create();
    public static PersonalRecordsManager getInstance() {
        if (instance == null) {
            instance = new PersonalRecordsManager();
        }
        return instance;
    }

    public void loadRecord(PersonalRecords record) {
        playerRecords.put(record.getPlayerID(), record);
    }

    public String serialize(PersonalRecords record) {
        return gson.toJson(record);
    }

    public PersonalRecords deserialize(String fileName) {
        try (Reader reader = new FileReader(fileName)) {
            return gson.fromJson(reader, PersonalRecords.class);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to read " + fileName);
            return null;
        }
    }

    public Collection<PersonalRecords> getAll() {
        return playerRecords.values();
    }
}
