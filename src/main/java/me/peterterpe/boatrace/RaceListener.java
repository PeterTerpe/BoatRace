package me.peterterpe.boatrace;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Player;

public class RaceListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        // 防止无意义检查（如仅旋转视角时触发）
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

        RaceTrackManager manager = RaceTrackManager.getInstance();
        for (RaceTrack track : manager.getAll()) {
            // 确保玩家所在世界与赛道一致
            if (!player.getWorld().getName().equals(track.getWorldName())) continue;

            // 判断是否为正在比赛的赛道以及玩家是否在参赛中
            RaceSession session = BoatRace.getInstance().getRaceManager().getSession(track);
            if (session == null || !session.isRunning() || !session.hasPlayer(player)) continue;

            // 检查是否到达终点区域
            if (track.isInFinishRegion(event.getTo())) {
                session.checkFinish(player);
                // 如果需要一次性触发，可在这里移除玩家
                break;
            }
        }
    }
}
