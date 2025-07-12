package me.peterterpe.boatrace;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.UUID;

public class RaceManager {
    // 每个赛道 (RaceTrack) 正对应一个正在进行的比赛 (RaceSession)
    private final Map<RaceTrack, RaceSession> activeSessions = new HashMap<>();

    /**
     * 如果指定赛道没有正在进行的比赛，就创建一个新的 RaceSession。
     */
    public RaceSession startSessionIfAbsent(RaceTrack track) {
        return activeSessions.computeIfAbsent(track, RaceSession::new);
    }

    /**
     * 获取指定赛道当前的比赛会话，可能为 null（未 start）。
     */
    public RaceSession getSession(RaceTrack track) {
        return activeSessions.get(track);
    }

    public RaceSession getSessionFor(UUID playerID) {
        for (RaceSession session : activeSessions.values()) {
            if (session.getParticipants().contains(playerID)) {
                return session;
            }
        }
        return null;
    }

    /**
     * 在比赛结束或取消时调用，移除该赛道的会话。
     */
    public RaceSession endSession(RaceTrack track) {
        return activeSessions.remove(track);
    }

    /**
     * 快捷方法，根据玩家当前所在位置检测是否完成赛道，
     * 并结束会话（具体逻辑在 RaceListener 中调用）。
     */
    public void checkFinishIfNeeded(me.peterterpe.boatrace.listeners.RaceListener listener, 
                                    Player player, 
                                    Location location) {
        for (RaceTrack track : RaceTrackManager.getInstance().getAll()) {
            if (!player.getWorld().getName().equals(track.getWorldName())) continue;

            RaceSession session = getSession(track);
            if (session == null || !session.isRunning() || !session.hasPlayer(player)) continue;

            if (track.isInFinishRegion(location)) {
                session.checkFinish(player);
                // 若该 session 没有参与者，可选择自动结束
                // if (session.isEmpty()) endSession(track);
                break;
            }
        }
    }
}