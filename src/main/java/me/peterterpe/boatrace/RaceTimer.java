package me.peterterpe.boatrace;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RaceTimer {
    private final Player player;
    private long startTimeNano;
    private BukkitRunnable task;
    

    public RaceTimer(Player player) {
        this.player = player;
    }
    public void start() {
        startTimeNano = System.nanoTime();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                long elapsed = (System.nanoTime() - startTimeNano) / 1_000_000;
                int minutes = (int)(elapsed / 60000);
                int seconds = (int)(elapsed % 60000) / 1000;
                int ms = (int)(elapsed % 1000) / 10;
                String timerText = String.format("%02d:%02d:%02d", minutes, seconds, ms);
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
