package me.peterterpe.boatrace;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.Map;

public class RaceTimer {
    private final Player player;
    private long startTimeNano;
    private BukkitRunnable task;
    private Map<Player, RaceTimer> raceTimers = new HashMap<>();

    public RaceTimer(Player player) {
        this.player = player;
        raceTimers.put(player, this);
    }
    public RaceTimer getInstance(Player player) {
        return raceTimers.get(player);
    }
    public void start() {
        startTimeNano = System.nanoTime();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                long elapsed = (System.nanoTime() - startTimeNano) / 1_000_000;
                int seconds = (int)(elapsed / 1000);
                int ms = (int)(elapsed % 1000);
                // 保留两位毫秒，如 05, 10, 95
                int displayMs = (ms / 10) * 10;
                String timerText = String.format("%02d:%03d", seconds, displayMs);

                Component msg = Component.translatable("race.timer")
                                        .color(NamedTextColor.GREEN)
                                        .append(Component.text(timerText).color(NamedTextColor.YELLOW));
                player.sendActionBar(msg);
            }
        };
        task.runTaskTimer(BoatRace.getInstance(), 0L, 1L); // run every tick
    }
    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }
}
