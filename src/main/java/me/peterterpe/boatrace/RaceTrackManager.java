package me.peterterpe.boatrace;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Map;
import java.util.Collection;
import java.io.Reader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RaceTrackManager {
    private static RaceTrackManager instance;
    Gson gson = new GsonBuilder().setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(Location.class, new LocationAdapter())
            .registerTypeAdapter(RaceResult.class, new RaceResultAdapter())
            .create();
    private final Map<String, RaceTrack> tracks = new ConcurrentHashMap<>();

    private RaceTrackManager() {}

    public static RaceTrackManager getInstance() {
        if (instance == null) {
            instance = new RaceTrackManager();
        }
        return instance;
    }

    // Register tracks
    public void register(RaceTrack track) {
        tracks.put(track.getName(), track);
    }

    public void unregister(String name) {
        Hologram holo = DHAPI.getHologram(name);
        if (holo != null) {
            holo.delete();
        }
        StorageManager.getInstance().deleteTrack(tracks.get(name));
        tracks.remove(name);
    }

    public RaceTrack get(String name) {
        return tracks.get(name);
    }

    public Collection<RaceTrack> getAll() {
        return tracks.values();
    }
    // Serialise track to json
    public String serialize(RaceTrack track, String fileName) {
        return gson.toJson(track);
    }

    // Deserialise from json to track
    public RaceTrack deserialize(String fileName) {
        try (Reader reader = new FileReader(fileName)) {
            return gson.fromJson(reader, RaceTrack.class);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to read " + fileName);
            return null;
        }
    }

    // Update holograms
    public void updateLeaderboardHologram(RaceTrack track) {
        if (!track.hasHologram()) return;
        Location loc = track.getHoloLocation();
        List<String> lines = new ArrayList<>();
        lines.add("§6🏁 Top Times - " + track.getName());
        int rank = 1;
        for (RaceResult topResult : track.getTopTimes()) {
            lines.add(String.format("%d) %s %s", rank++, topResult.getPlayerName(), track.formatTime(topResult.getTimeInMs())));
            if (rank > 5) break;
        }

        Hologram existing = DHAPI.getHologram(track.getName());
        if (existing == null) {
            DHAPI.createHologram(track.getName(), loc, true, lines);
        } else {
            DHAPI.setHologramLines(existing, lines);
            DHAPI.moveHologram(existing, loc);
        }
    }
}
