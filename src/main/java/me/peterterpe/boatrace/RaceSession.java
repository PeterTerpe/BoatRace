package me.peterterpe.boatrace;

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

    public RaceSession(RaceTrack track) {
        this.track = track;
    }

    public void addPlayer(Player player) {
        if (started) return;
        participants.add(player);
        player.sendMessage(Component.translatable("race.joinMessage", Component.text(track.getName())));
    }

    public void broadcastToParticipants(Component msg) {
        for (Player p : participants) {
            BoatRace.getInstance().adventure().player(p).sendMessage(msg);
        }
    }

    public void startCountdown(int seconds) {
        if (started) return;
        broadcastToParticipants(Component.translatable("race.startMessage", Component.text(track.getName())));
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
                    started = true;
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
        for (Player player : participants) {
            RaceTimer racetimer = new RaceTimer(player);
            racetimer.start(); // start race timer
        }
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
        // RaceTimer timer = RaceTimer.getInstance(player);
        // .stop();
    }

    public boolean isRunning() {
        return started;
    }

    public boolean hasPlayer(Player player) {
        return participants.contains(player);
    }
}
