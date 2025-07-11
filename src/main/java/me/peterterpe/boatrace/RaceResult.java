package me.peterterpe.boatrace;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.google.gson.annotations.Expose;

public class RaceResult {
    @Expose private final UUID playerID;
    @Expose private final long timeInMs;

    public RaceResult(UUID playerID, long timeInMs) {
        this.playerID = playerID;
        this.timeInMs = timeInMs;
    }

    public boolean addTop(Player player, Long elapsed) {
        // placeholder
        return false;
    }

    public UUID getPlayerID() {
        return playerID;
    }

    public long getTimeInMs() {
        return timeInMs;
    }
    
}