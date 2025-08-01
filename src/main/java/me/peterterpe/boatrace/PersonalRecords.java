package me.peterterpe.boatrace;

import java.util.UUID;

import org.bukkit.Bukkit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.peterterpe.boatrace.adapters.InstantAdapter;
import net.kyori.adventure.text.Component;
import com.google.gson.annotations.Expose;
import java.util.List;
import java.util.Map;
import java.time.Instant;
import java.util.HashMap;

public class PersonalRecords {
    @Expose private UUID playerId;
    @Expose private int attempts = 0;
    @Expose private final Map<String, PersonalTrackRecord> trackRecords = new HashMap<>();
    Gson gson = new GsonBuilder().setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .create();

    public PersonalRecords() {}
    public PersonalRecords(UUID playerID) { this.playerId = playerID; }

    public void recordTime(String trackName, long elapsedMs) {
        attempts++;
        if (trackRecords.computeIfAbsent(trackName, key -> new PersonalTrackRecord()).addRecord(new PersonalRaceResult(elapsedMs))) {
            Bukkit.getPlayer(playerId).sendMessage(Component.translatable("congrats.personaltop5"));
        }
        StorageManager.getInstance().savePlayerRecords(this);
    }

    public UUID getPlayerID() { return playerId; }
    public int getAttempts() { return attempts; }
    public List<PersonalRaceResult> getResults(String trackName) {
        PersonalTrackRecord trackRecord = trackRecords.get(trackName);
        if (trackRecord == null) {
            return null;
        }
        return trackRecord.getRecords();
    }

    public int getAttempts(String trackName) {
        PersonalTrackRecord trackRecord = trackRecords.get(trackName);
        if (trackRecord == null) {
            return 0;
        }
        return trackRecord.getAttempts();
    }
}
