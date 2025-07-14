package me.peterterpe.boatrace.listeners;

import org.bukkit.event.Listener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import me.peterterpe.boatrace.BoatRace;
import me.peterterpe.boatrace.RaceSession;

public class RaceWaitingListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = e.getPlayer();
        RaceSession session = BoatRace.getInstance().getRaceManager().getSessionFor(player.getUniqueId());
        if (session == null || session.isRunning()) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (isReadyItem(item)) {
            session.changeItem(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInvClick(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        if (isReadyItem(item)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInvDrag(InventoryDragEvent e) {
        for (ItemStack item : e.getNewItems().values()) {
            if (isReadyItem(item)) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        Player player = (Player) e.getPlayer();
        if (BoatRace.getInstance().getRaceManager().getSessionFor(player.getUniqueId()) == null) { return; }
            ItemStack item = e.getItemDrop().getItemStack();
        if (isReadyItem(item)) {
            e.setCancelled(true);
        }
    }

    private boolean isReadyItem(ItemStack item) {
        if (item != null && (item.getType() == Material.GREEN_DYE || item.getType() == Material.RED_DYE) && item.hasItemMeta()) {
            return true;
        }
        return false;
    }
}
