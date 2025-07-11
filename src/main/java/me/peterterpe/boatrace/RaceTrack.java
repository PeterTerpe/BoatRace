package me.peterterpe.boatrace;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RaceTrack {
    @Expose private String name;
    @Expose private String worldName;

    // 起点、终点区域定义（两个角的位置）
    @Expose private Location startA;
    @Expose private Location startB;
    @Expose private Location finishA;
    @Expose private Location finishB;

    // 全息图展示位置与开关
    @Expose private boolean showHologram;
    @Expose private Location holoLocation;

    // 记录排行榜（单位毫秒）
    @Expose private List<RaceResult> topTimes = new ArrayList<>();

    // 玩家个人最好成绩映射：UUID -> 最好时间（毫秒）
    @Expose private transient /* 不参与序列化的缓存，可改为持久化结构 */ 
        final List<String> dummy = Collections.emptyList();

    public RaceTrack() {} // Gson 需要的空构造

    public RaceTrack(String name, String worldName) {
        this.name = name;
        this.worldName = worldName;
    }

    public void setPoint(int point, Location location) {
        switch (point) {
            case 1:
                this.startA = location;
                break;
            case 2:
                this.startB = location;
                break;
            case 3:
                this.finishA = location;
                break;
            case 4:
                this.finishB = location;
            default:
                break;
        }
    }

    // Getter & Setter

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) { this.worldName = worldName; }
    public Location getStartA() { return startA; }
    public void setStartA(Location startA) { this.startA = startA; }
    public Location getStartB() { return startB; }
    public void setStartB(Location startB) { this.startB = startB; }
    public Location getFinishA() { return finishA; }
    public void setFinishA(Location finishA) { this.finishA = finishA; }
    public Location getFinishB() { return finishB; }
    public void setFinishB(Location finishB) { this.finishB = finishB; }
    public boolean hasHologram() { return showHologram && holoLocation != null; }
    public boolean isShowHologram() { return showHologram; }
    public void setShowHologram(boolean show) { this.showHologram = show; }
    public Location getHoloLocation() { return holoLocation; }
    public void setHoloLocation(Location holoLocation) { this.holoLocation = new Location(Bukkit.getWorld(worldName), holoLocation.getX(), holoLocation.getY(), holoLocation.getZ()); }
    public List<RaceResult> getTopTimes() { return topTimes; }
    public void setTopTimes(List<RaceResult> times) { this.topTimes = times; }

    /**
     * 检查给定位置是否在赛道起点区域内。
     */
    public boolean isInStartRegion(Location loc) {
        return isWithin(loc, startA, startB);
    }

    /**
     * 检查给定位置是否在终点区域内。
     */
    public boolean isInFinishRegion(Location loc) {
        return isWithin(loc, finishA, finishB);
    }

    private boolean isWithin(Location loc, Location a, Location b) {
        if (loc == null || a == null || b == null) return false;
        BoundingBox box = BoundingBox.of(a, b);
        return box.contains(loc.toVector());
    }

    /**
     * 尝试将给定时间加入排行榜（Top5），若排入前5返回 true。
     */
    public boolean addTime(String playerName, long time) {
        // check if player is on board
        for (int i = 0; i < topTimes.size(); i++) {
            if (topTimes.get(i).getPlayerName() == playerName) {
                if (topTimes.get(i).getTimeInMs() > time) {
                    topTimes.remove(i);
                    break;
                } else {
                    return false;
                }
            }
        }
        // if the scoreboard is empty
        if (topTimes.size() == 0) {
            topTimes.add(new RaceResult(playerName, time));
            return true;
        }
        for (int i = 0; i < topTimes.size(); i++) {
            if (topTimes.get(i).getTimeInMs() < time) {
                continue;
            } else {
                topTimes.add(i, new RaceResult(playerName, time));
                if (topTimes.size() > 5) {
                    topTimes = topTimes.subList(0, 4);
                }
                return true;
            }
        }
        return false;
    }
    // Format milisec to mm:ss.SSS
    public String formatTime(long millis) {
        long totalSec = millis / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;
        long ms = millis % 1000;
        return String.format("%02d:%02d.%03d", min, sec, ms);
    }
}
