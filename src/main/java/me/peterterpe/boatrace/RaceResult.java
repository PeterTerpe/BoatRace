package me.peterterpe.boatrace;

import org.bukkit.entity.Player;

import com.google.gson.annotations.Expose;

public class RaceResult {
    @Expose private final String playerName;
    @Expose private final long timeInMs;

    public RaceResult(String playerName, long timeInMs) {
        this.playerName = playerName;
        this.timeInMs = timeInMs;
    }

    public boolean addTop(Player player, Long elapsed) {
        // placeholder
        return false;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getTimeInMs() {
        return timeInMs;
    }
    
}