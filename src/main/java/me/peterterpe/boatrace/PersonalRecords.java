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

    public void recordTime(String trackName, long elapsedMs) {
        attempts++;
        if (trackRecords.get(trackName).addRecord(new PersonalRaceResult(elapsedMs))) {
            Bukkit.getPlayer(playerId).sendMessage(Component.translatable("congrats.personaltop5"));
        }
        StorageManager.getInstance().savePlayerRecords(this);
    }

    public UUID getPlayerID() { return playerId; }
    public int getAttemps() { return attempts; }
    public List<PersonalRaceResult> getResults(String trackName) {
        return trackRecords.get(trackName).getRecords();
    }
}
