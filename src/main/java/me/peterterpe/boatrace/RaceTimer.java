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
                int seconds = (int)(elapsed / 1000);
                int ms = (int)(elapsed % 1000);
                // 保留两位毫秒，如 05, 10, 95
                int displayMs = (ms / 10) * 10;
                String timerText = String.format("%02d:%03d", seconds, displayMs);

                Component msg = Component.text("时间: ")
                                        .color(NamedTextColor.GREEN)
                                        .append(Component.text(timerText).color(NamedTextColor.YELLOW));
                player.sendActionBar(msg); // Adventure API 推荐用法 :contentReference[oaicite:7]{index=7}
            }
        };
        task.runTaskTimer(BoatRace.getInstance(), 0L, 1L); // 每 tick 执行一次（50ms）
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }
}
