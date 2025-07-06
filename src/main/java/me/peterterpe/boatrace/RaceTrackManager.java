package me.peterterpe.boatrace;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;

import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RaceTrackManager {
    private static RaceTrackManager instance;
    Gson gson = new GsonBuilder().setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(Location.class, new LocationAdapter())
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

    public RaceTrack get(String name) {
        return tracks.get(name);
    }

    public Collection<RaceTrack> getAll() {
        return tracks.values();
    }

    public void unregister(String name) {
        Hologram holo = DHAPI.getHologram(name);
        if (holo != null) {
            holo.delete();
        }
        tracks.remove(name);
    }

    // Serialise track to json
    public String serialize(RaceTrack track) {
        return gson.toJson(track);
    }

    // Deserialise from json to track
    public RaceTrack deserialize(String json) {
        return gson.fromJson(json, RaceTrack.class);
    }

    // Update holograms
    public void updateLeaderboardHologram(RaceTrack track) {
        if (!track.hasHologram()) return;

        Location loc = track.getHoloLocation();
        List<String> lines = new ArrayList<>();
        lines.add("§6🏁 Top Times - " + track.getName());
        int rank = 1;
        for (long time : track.getTopTimes()) {
            lines.add(String.format("%d) %s", rank++, track.formatTime(time)));
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
