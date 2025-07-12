package me.peterterpe.boatrace.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
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
        RaceSession session = raceManager.getSessionFor(p.getUniqueId());
        if (session == null || !session.isCountdownActive()) return;
        Location to = e.getTo();
        if (!session.getTrack().isInStartRegion(to)) {
            e.setCancelled(true);
            if (!p.isInsideVehicle()) return;
            Entity vehicle = p.getVehicle();
            if (!(vehicle instanceof Boat boat)) return;
            boat.remove();
        }
    }
}