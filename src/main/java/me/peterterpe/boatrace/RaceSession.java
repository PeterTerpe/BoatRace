package me.peterterpe.boatrace;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.util.*;

public class RaceSession {
    private final RaceTrack track;
    private final Set<UUID> participants = new HashSet<>();
    private final Set<UUID> participated = new HashSet<>();
    private final Set<UUID> ready = new HashSet<>();
    private long startTime;
    private boolean started = false;
    private Map<Player, RaceTimer> raceTimers = new HashMap<>();
    private boolean countdownActive = false;
    public RaceSession(RaceTrack track) {
        this.track = track;
    }

    public void addPlayer(Player player) {
        if (started) {
            player.sendMessage(Component.translatable("fail.join.started"));
            return;
        }
        participants.add(player.getUniqueId());
        participated.add(player.getUniqueId());
        giveReadyItem(player);
        broadcast(Component.translatable("success.race.join", Component.text(player.getName()), Component.text(track.getName())));
    }

    public void broadcastTitle(Title title) {
        for (UUID uuid : participants) {
            Player p = Bukkit.getPlayer(uuid);
            BoatRace.getInstance().adventure().player(p).showTitle(title);
        }
    }
    
    public void broadcastActionBar(Component msg) {
        for (UUID uuid : participants) {
            Player p = Bukkit.getPlayer(uuid);
            BoatRace.getInstance().adventure().player(p).sendActionBar(msg);
        }
    }

    public void broadcast(Component msg) {
        for (UUID uuid : participants) {
            Player p = Bukkit.getPlayer(uuid);
            BoatRace.getInstance().adventure().player(p).sendMessage(msg);
        }
    }

    public void giveReadyItem(Player player) {
        ItemStack item = new ItemStack(Material.RED_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.translatable("item.notready"));
        item.setItemMeta(meta);
        player.getInventory().setItem(8, item);
        player.updateInventory();
    }

    public void changeItem(Player player) {
        Inventory inv = player.getInventory();
        ItemStack oldItem = inv.getItem(8);
        if (oldItem != null && oldItem.getType() == Material.GREEN_DYE && oldItem.hasItemMeta()) {
            giveReadyItem(player);
            ready.remove(player.getUniqueId());
            return;
        }
        ready.add(player.getUniqueId());
        ItemStack newItem = new ItemStack(Material.GREEN_DYE);
        ItemMeta meta = newItem.getItemMeta();
        meta.displayName(Component.translatable("item.ready"));
        newItem.setItemMeta(meta);
        player.getInventory().setItem(8, newItem);
        player.updateInventory();
        if (participants.size() == ready.size()) {
            startCountdown(5);
            for (UUID uuid : ready) {
                Player p = Bukkit.getPlayer(uuid);
                p.getInventory().setItem(8, null);
                p.updateInventory();
            }
            ready.clear();
        }
        broadcastActionBar(Component.translatable("race.waiting", Component.text(ready.size()), Component.text(participants.size())));
    }

    public void removeItem(Player player) {
        player.getInventory().setItem(8, null);
        player.updateInventory();
    }

    public void startCountdown(int seconds) {
        if (started) return;
        if (participants.isEmpty()) {
            Bukkit.getLogger().warning("No participants in track "+track.getName());
            return;
        }
        this.countdownActive = true;
        tpAllStartRegion();
        broadcast(Component.translatable("race.start.message", Component.text(track.getName())));
        this.started = true;
        new BukkitRunnable() {
            int timer = seconds;
            @Override
            public void run() {
                if (timer <= 0) {
                    broadcastTitle(Title.title(Component.translatable("race.go"), Component.empty()));;
                    for (UUID uuid : participants) {
                        Player player = Bukkit.getPlayer(uuid);
                        player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 1f, 1f);
                        startTime = System.currentTimeMillis();
                    }
                    for (UUID uuid : participants) {
                        Player player = Bukkit.getPlayer(uuid);
                        RaceTimer racetimer = new RaceTimer(player);
                        raceTimers.put(player, racetimer);
                        racetimer.start(); // start race timer
                    }
                    countdownActive = false;
                    this.cancel();
                } else {
                    broadcastTitle(Title.title(Component.translatable("race.countdown", Component.text(timer)), Component.empty()));
                    for (UUID uuid : participants) {
                        Player player = Bukkit.getPlayer(uuid);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                    }
                    timer--;
                }
            }
        }.runTaskTimer(BoatRace.getInstance(), 0L, 20L); // 20 ticks
    }
    /**
     * Remove player from the race session
     * if participants are empty after removal, started will be set to false
     * @param player
     */
    public void checkFinish(Player player) {
        if (!started) return;
        if (!participants.contains(player.getUniqueId())) return;
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        long elapsed = System.currentTimeMillis() - startTime;
        for (UUID uuid : participated) {
            Bukkit.getPlayer(uuid).sendMessage(Component.translatable("race.player.finish", Component.text(player.getName()), Component.text(track.formatTime(elapsed))));
        }
        player.sendMessage(Component.translatable("race.finished"));
        if (track.addTime(player.getUniqueId(), elapsed)) {
            StorageManager.getInstance().saveTrack(track);
            RaceTrackManager.getInstance().updateLeaderboardHologram(track);
            player.sendMessage(Component.translatable("top5.congrats", Component.text("The Argument")));
        }
        stop(player.getUniqueId());
    }
    /** Remove all participants and their timers and set started to false */
    public void forceStop() {
        for (UUID uuid : participants) {
            stop(uuid);
        }
    }

    /** Remove player from participants and their timer */
    public void stop(UUID uuid) {
        participants.remove(uuid);
        ready.remove(uuid);
        Player player = Bukkit.getPlayer(uuid);
        raceTimers.get(player).stop();
        raceTimers.remove(player);
        // Change the status to not started to allow future joins.
        if (participants.isEmpty()) {
            this.started = false;
            participated.clear();
        }
    }

    /** tp participants to spawn if they are outside the start region */
    private void tpAllStartRegion() {
        Location spawn = track.getSpawn();
        if (spawn == null) return;
        Location loc = new Location(Bukkit.getWorld(track.getWorldName()), spawn.getX(), spawn.getY(), spawn.getZ());
        for (UUID uuid : participants) {
            Player player = Bukkit.getPlayer(uuid);
            if (!track.isInStartRegion(player.getLocation())) {
                player.teleport(loc);
            }
        }
    }

    public void markReady(UUID uuid) {
        ready.add(uuid);
    }

    public boolean isRunning() {
        return started;
    }

    public boolean hasPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        return participants.contains(uuid);
    }

    
    public boolean isCountdownActive() {
        return countdownActive;
    }

    public Set<UUID> getParticipants() {
        return participants;
    }

    public RaceTrack getTrack() { return track; }

    public Set<UUID> getReady() { return ready; }
}
