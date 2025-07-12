package me.peterterpe.boatrace.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent;
import java.util.UUID;
import me.peterterpe.boatrace.BoatRace;
import me.peterterpe.boatrace.RaceManager;
import me.peterterpe.boatrace.RaceSession;

public class LeaveSessionListener implements Listener{
    RaceManager raceManager = BoatRace.getInstance().getRaceManager();
    private void removeFromSession(UUID uuid) {
        RaceSession session = raceManager.getSessionFor(uuid);
        if (session != null) {
            session.stop(uuid);
        }
        return;
    }
    @EventHandler
    public void onPlayerConnectionClose(PlayerConnectionCloseEvent e) {
        removeFromSession(e.getPlayerUniqueId());
    }
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        removeFromSession(e.getPlayer().getUniqueId());
    }
}
