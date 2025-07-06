package me.peterterpe.boatrace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.kyori.adventure.text.Component;

public class RaceCommandHandler implements TabExecutor {
    private void createTrack(String name, String world) {
        var manager = RaceTrackManager.getInstance();
        var track = new RaceTrack(name, world);
        manager.register(track);
        StorageManager.getInstance().saveTrack(track);
    }
    // on-tab completion
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        final List<String> validArg = new ArrayList<>();
        final List<String> permittedArg = new ArrayList<>();
        if (args.length == 1) {
            
            return validArg;
        }
        switch (args.length) {
            case 1:
                Map<String, String> subCommands = Map.of(
                    "create", "boatrace.create",
                    "start",  "boatrace.start",
                    "delete", "boatrace.delete",
                    "setstart1", "boatrace.modify",
                    "setstart2", "boatrace.modify",
                    "setfinish1", "boatrace.modify",
                    "setfinish2", "boatrace.modify",
                    "list",   "boatrace.list",
                    "join", "boatrace.join",
                    "start", "boatrace.start"
                );
                for (var entry : subCommands.entrySet()) {
                    if (!(sender instanceof Player player) || player.hasPermission(entry.getValue())) {
                        permittedArg.add(entry.getKey());
                    }
                }
                StringUtil.copyPartialMatches(args[0], permittedArg, validArg);
            case 2:
                String sub = args[0].toLowerCase();
                switch (sub) {
                    case "create":
                        Collection<RaceTrack> tracks = RaceTrackManager.getInstance().getAll();
                        List<String> trackNames = new ArrayList<>();
                        if (!(tracks.isEmpty())) {
                            for (RaceTrack track : tracks) {
                                String trackName = track.getName();
                                trackNames.add(trackName);
                            }
                        }
                        StringUtil.copyPartialMatches(args[0], trackNames, validArg);
                    default:
                        break;
                }
            default:
                break;
        }
        return validArg;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        
        
        if (args.length == 0) {
            sender.sendMessage("§6--- /race 帮助 ---");
            sender.sendMessage("/race create <name> - 创建赛道");
            sender.sendMessage("/race setstart1 <name> - 设置起点角①");
            sender.sendMessage("/race setstart2 <name> - 设置起点角②");
            sender.sendMessage("/race setfinish1 <name> - 设置终点角①");
            sender.sendMessage("/race setfinish2 <name> - 设置终点角②");
            sender.sendMessage("/race join <name> - 加入比赛");
            sender.sendMessage("/race start <name> - 启动比赛");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create": {
                if (!(sender.hasPermission("boatrace.create"))) {
                    noPerm(sender);
                    return false;
                }
                if (args.length < 2) {
                    needArg(sender);
                    return false;
                }
                if (args.length == 2) {
                    if (sender instanceof Player player) {
                        String worldName = player.getWorld().getName(); // Use the player's current world
                        createTrack(args[1], worldName);
                    } else {
                        needArg(sender);
                        return false;
                    }
                } else {
                    createTrack(args[1], args[2]);
                }
                sender.sendMessage(Component.translatable("track.create.success", Component.text(args[2])));
            }
            case "setstart1": case "setstart2": case "setfinish1": case "setfinish2": {
                if (!(sender.hasPermission("boatrace.modify"))) {
                    noPerm(sender);
                    return false;
                }
                Map<String, Integer> pointMap = Map.of("setstart1", 1, "setstart2", 2, "setfinish1", 3, "setfinish2", 4);
                if (args.length < 2) return needArg(sender);
                if (args.length == 2) {
                    if (sender instanceof Player player) {
                        RaceTrack track = RaceTrackManager.getInstance().get(args[1]);
                        if (track == null) return noTrack(sender);
                        track.setPoint(pointMap.get(sub), player.getLocation());
                        StorageManager.getInstance().saveTrack(track);
                        player.sendMessage(Component.translatable("track."+sub+".success"));
                    } else {
                        sender.sendMessage("§cCoordinates must be specified!");
                        return needArg(sender);
                    }
                } else {
                    if (args.length == 5) {
                        try {
                            double x = Double.parseDouble(args[2]);
                            double y = Double.parseDouble(args[3]);
                            double z = Double.parseDouble(args[4]);
                            RaceTrack track = RaceTrackManager.getInstance().get(args[1]);
                            if (track == null) return noTrack(sender);
                            Location location = new Location(Bukkit.getWorld(track.getWorldName()), x, y, z);
                            track.setPoint(pointMap.get(sub), location);
                            StorageManager.getInstance().saveTrack(track);
                            sender.sendMessage(Component.translatable("track."+sub+".success"));
                        } catch (Exception e) {
                            sender.sendMessage(Component.translatable("error.invalid.pos"));
                            return false;
                        }
                    }
                }
            }
            case "join": {
                if (!(sender.hasPermission("boatrace.join"))) {
                    noPerm(sender);
                    return false;
                }
                if (sender instanceof Player p) {
                    var track = RaceTrackManager.getInstance().get(args[1]);
                    if (track == null) return noTrack(p);
                    BoatRace.getInstance().getRaceManager().startSessionIfAbsent(track);
                    RaceSession session = BoatRace.getInstance().getRaceManager().getSession(track);
                    session.addPlayer(p);
                } else {
                    sender.sendMessage(Component.translatable("error.notplayer"));
                    return false;
                }
                
            }
            case "start": {
                if (!(sender.hasPermission("boatrace.start"))) {
                    noPerm(sender);
                    return false;
                }
                if (args.length < 2) return needArg(sender);
                var track = RaceTrackManager.getInstance().get(args[1]);
                if (track == null) return noTrack(sender);
                RaceSession session = BoatRace.getInstance().getRaceManager().getSession(track);
                if (session == null) return noRes(sender);
                session.startCountdown(5);
            }
            default: sender.sendMessage(Component.translatable("error.command.notfound"));
        }
        return true;
    }

    private boolean noPerm(CommandSender sender) {
        sender.sendMessage(Component.translatable("error.command.noperm"));
        return false;
    }
    private boolean needArg(CommandSender sender) {
        sender.sendMessage(Component.translatable("error.command.arg"));
        return false;
    }
    private boolean noTrack(CommandSender sender) {
        sender.sendMessage(Component.translatable("error.track.notfound"));
        return false;
    }
    private boolean noRes(CommandSender sender) {
        sender.sendMessage(Component.translatable("error.session.notfound"));
        return false;
    }
}
