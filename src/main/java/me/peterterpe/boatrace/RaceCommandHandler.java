package me.peterterpe.boatrace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import net.kyori.adventure.text.Component;

public class RaceCommandHandler implements TabExecutor {
    private void createTrack(String name, String world) {
        var manager = RaceTrackManager.getInstance();
        var track = new RaceTrack(name, world);
        manager.register(track);
        StorageManager.getInstance().saveTrack(track);
    }
    private List<String> getAllTracks() {
        Collection<RaceTrack> tracks = RaceTrackManager.getInstance().getAll();
        List<String> trackNames = new ArrayList<>();
        if (!(tracks.isEmpty())) {
            for (RaceTrack track : tracks) {
                String trackName = track.getName();
                trackNames.add(trackName);
            }
        }
        return trackNames;
    }
    // on-tab completion
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        final List<String> validArg = new ArrayList<>();
        final List<String> permittedArg = new ArrayList<>();
        switch (args.length) {
            case 1:
                final Map<String, String> subCommands = Map.of(
                    "create", "boatrace.command.create",
                    "start",  "boatrace.command.start",
                    "delete", "boatrace.command.delete",
                    "setstart1", "boatrace.command.modify",
                    "setstart2", "boatrace.command.modify",
                    "setfinish1", "boatrace.command.modify",
                    "setfinish2", "boatrace.command.modify",
                    "list",   "boatrace.command.list",
                    "join", "boatrace.command.join",
                    "stop", "boatrace.command.stop"
                );
                for (var entry : subCommands.entrySet()) {
                    if (!(sender instanceof Player player) || player.hasPermission(entry.getValue())) {
                        permittedArg.add(entry.getKey());
                    }
                }
                StringUtil.copyPartialMatches(args[0], permittedArg, validArg);
                return validArg;
            case 2:
                List<String> trackNames = getAllTracks();
                StringUtil.copyPartialMatches(args[1], trackNames, validArg);
                return validArg;
            case 3:
                if (Arrays.asList("setstart1", "setstart2", "setfinish1", "setfinish2").contains(args[0])) {
                    if (!(sender instanceof Player player)) {
                        return validArg;
                    }
                    Block target = player.getTargetBlockExact(5);  // get target position
                    if (target != null) {
                        Location loc = target.getLocation();
                        String coords = loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ();
                        return Collections.singletonList(coords);
                    }
                }
        }
        return validArg;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.translatable("help.head"));
            sender.sendMessage(Component.translatable("help.create"));
            sender.sendMessage(Component.translatable("help.create"));
            sender.sendMessage(Component.translatable("help.setstart", Component.text("①")));
            sender.sendMessage(Component.translatable("help.setstart", Component.text("②")));
            sender.sendMessage(Component.translatable("help.setfinish", Component.text("①")));
            sender.sendMessage(Component.translatable("help.setfinish", Component.text("②")));
            sender.sendMessage(Component.translatable("help.join"));
            sender.sendMessage(Component.translatable("help.start"));
            sender.sendMessage(Component.translatable("help.stop"));
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create": 
                if (!(sender.hasPermission("boatrace.command.create"))) {
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
                sender.sendMessage(Component.translatable("track.create.success", Component.text(args[1])));
                break;
            case "delete": 
                if (!(sender.hasPermission("boatrace.command.delete"))) {
                    noPerm(sender);
                    return false;
                }
                if (args.length < 2) {
                    needArg(sender);
                    return false;
                }
                if (getAllTracks().contains(args[1])) {
                    var manager = RaceTrackManager.getInstance();
                    manager.unregister(args[1]);
                    sender.sendMessage(Component.translatable("success.track.delete", Component.text(args[1])));
                } else {
                    sender.sendMessage(Component.translatable("error.track.notfound"));
                }
                break;
            case "setstart1": case "setstart2": case "setfinish1": case "setfinish2":
                if (!(sender.hasPermission("boatrace.command.modify"))) {
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
                break;
            case "join":
                if (!(sender.hasPermission("boatrace.command.join"))) {
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
                break;
            case "start":
                if (!(sender.hasPermission("boatrace.command.start"))) {
                    noPerm(sender);
                    return false;
                }
                if (args.length < 2) return needArg(sender);
                var track = RaceTrackManager.getInstance().get(args[1]);
                if (track == null) return noTrack(sender);
                RaceSession session = BoatRace.getInstance().getRaceManager().getSession(track);
                if (session == null) return noRes(sender);
                BoatRace.getInstance().getRaceManager().getSession(track).startCountdown(5);;
                break;
            case "stop":
                if (!(sender.hasPermission("boatrace.command.stop"))) {
                    noPerm(sender);
                    return false;
                }
                if (args.length < 2) return needArg(sender);
                track = RaceTrackManager.getInstance().get(args[1]);
                session = BoatRace.getInstance().getRaceManager().getSession(track);
                if (session == null) return noRes(sender);
                BoatRace.getInstance().getRaceManager().endSession(track).forceStop();
                break;
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
