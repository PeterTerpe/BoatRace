package me.peterterpe.boatrace.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import me.peterterpe.boatrace.RaceManager;
import me.peterterpe.boatrace.RaceSession;

public class CountdownMoveListener implements Listener {
    private final RaceManager raceManager;

    public CountdownMoveListener(RaceManager raceManager) {
        this.raceManager = raceManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        RaceSession session = raceManager.getSessionFor(p);
        if (session == null || !session.isCountdownActive()) return;

        Location to = e.getTo();
        Location from = e.getFrom();

        // allow looking direction, but prevent leaving
        to.setDirection(from.getDirection());

        if (!session.getTrack().isInStartRegion(to)) {
            e.setTo(from);
        }
    }
}