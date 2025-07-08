package me.peterterpe.boatrace;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.kyori.adventure.text.Component;
import java.util.*;

public class RaceSession {
    private final RaceTrack track;
    private final Set<Player> participants = new HashSet<>();
    private final Map<Player, Long> startTimes = new HashMap<>();
    private boolean started = false;
    private Map<Player, RaceTimer> raceTimers = new HashMap<>();

    public RaceSession(RaceTrack track) {
        this.track = track;
    }

    public void addPlayer(Player player) {
        if (started) {
            player.sendMessage(Component.translatable("fail.join.started"));
            return;
        }
        participants.add(player);
        for (Player p : participants) {
            p.sendMessage(Component.translatable("success.race.join", Component.text(p.getName()), Component.text(track.getName())));   
        }
    }

    public void broadcastToParticipants(Component msg) {
        for (Player p : participants) {
            BoatRace.getInstance().adventure().player(p).sendMessage(msg);
        }
    }

    public void startCountdown(int seconds) {
        if (started) return;
        if (participants.isEmpty()) {
            Bukkit.getLogger().warning("No participants in track "+track.getName());
        }
        broadcastToParticipants(Component.translatable("race.start.message", Component.text(track.getName())));
        this.started = true;
        new BukkitRunnable() {
            int timer = seconds;
            @Override
            public void run() {
                if (timer <= 0) {
                    broadcastToParticipants(Component.translatable("race.go"));;
                    for (Player player : participants) {
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                        startTimes.put(player, System.currentTimeMillis());
                    }
                    for (Player player : participants) {
                        RaceTimer racetimer = new RaceTimer(player);
                        raceTimers.put(player, racetimer);
                        racetimer.start(); // start race timer
                    }
                    this.cancel();
                } else {
                    broadcastToParticipants(Component.translatable("race.countdown", Component.text(timer)));
                    for (Player player : participants) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                    }
                    timer--;
                }
            }
        }.runTaskTimer(BoatRace.getInstance(), 0L, 20L); // 20 ticks
    }

    public void checkFinish(Player player) {
        if (!started) return;
        if (!startTimes.containsKey(player)) return;
        long elapsed = System.currentTimeMillis() - startTimes.get(player);
        player.sendMessage(Component.translatable("race.finished", Component.text(track.getName()), Component.text(track.formatTime(elapsed))));
        if (track.addTime(elapsed)) {
            RaceTrackManager.getInstance().updateLeaderboardHologram(track);
            player.sendMessage(Component.translatable("top5.congrats", Component.text("The Argument")));
        }
        participants.remove(player);
        startTimes.remove(player);
        raceTimers.get(player).stop();
        raceTimers.remove(player);
        // Change the status to not started to allow future joins.
        if (participants.isEmpty()) {
            this.started = false;
        }
    }

    public boolean isRunning() {
        return started;
    }

    public boolean hasPlayer(Player player) {
        return participants.contains(player);
    }
}
