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
    private static final List<String> emptyStringList = new ArrayList<>();
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
                final Map<String, String> subCommands = Map.ofEntries(
                    Map.entry("create", "boatrace.command.create"),
                    Map.entry("start",  "boatrace.command.start"),
                    Map.entry("delete", "boatrace.command.delete"),
                    Map.entry("setstart1", "boatrace.command.modify"),
                    Map.entry("setstart2", "boatrace.command.modify"),
                    Map.entry("setfinish1", "boatrace.command.modify"),
                    Map.entry("setfinish2", "boatrace.command.modify"),
                    Map.entry("hologram", "boatrace.command.modify"),
                    Map.entry("list",   "boatrace.command.list"),
                    Map.entry("join", "boatrace.command.join"),
                    Map.entry("stop", "boatrace.command.stop"),
                    Map.entry("setspawn", "boatrace.command.modify"),
                    Map.entry("check", "boatrace.command")
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
                if (Arrays.asList("setstart1", "setstart2", "setfinish1", "setfinish2", "setspawn").contains(args[0])) {
                    return getTargetBlock(sender);
                } else {
                    if (args[0].equals("hologram")) {
                        validArg.add("show");
                        validArg.add("hide");
                        validArg.add("setpos");
                        return validArg;
                    }
                }
            case 4:
                if (args[0].equals("hologram")) {
                    return getTargetBlock(sender);
                    }
        }
        return validArg;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.translatable("help.head"));
            sender.sendMessage(Component.translatable("help.create"));
            sender.sendMessage(Component.translatable("help.delete"));
            sender.sendMessage(Component.translatable("help.setstart", Component.text("1")));
            sender.sendMessage(Component.translatable("help.setstart", Component.text("2")));
            sender.sendMessage(Component.translatable("help.setfinish", Component.text("1")));
            sender.sendMessage(Component.translatable("help.setfinish", Component.text("2")));
            sender.sendMessage(Component.translatable("help.join"));
            sender.sendMessage(Component.translatable("help.start"));
            sender.sendMessage(Component.translatable("help.stop"));
            sender.sendMessage(Component.translatable("help.hologram"));
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "setspawn":
                if (args.length < 2) return needArg(sender);
                RaceTrack track = RaceTrackManager.getInstance().get(args[1]);
                if (track == null) return noTrack(sender);
                if (args.length == 5) {
                    try {
                        double x = Double.parseDouble(args[2]);
                        double y = Double.parseDouble(args[3]);
                        double z = Double.parseDouble(args[4]);
                        track.setSpawn(new Location(null, x, y, z));
                        sender.sendMessage(Component.translatable("success.spawn.setpos", Component.text(
                            Double.toString(x) + " " + 
                            Double.toString(y) + " " + 
                            Double.toString(z))));
                    } catch (Exception e) {
                        sender.sendMessage(Component.translatable("error.invalid.pos"));
                    }
                } else {
                    if (sender instanceof Player player) {
                        Location location = player.getLocation();
                        track.setSpawn(location);
                        sender.sendMessage(Component.translatable("success.spawn.setpos", Component.text(
                            Double.toString(location.getX()) + " " + 
                            Double.toString(location.getY()) + " " + 
                            Double.toString(location.getZ()))));
                    } else {
                        return needArg(sender);
                    }
                }
                break;
            case "create":
                if (!(sender.hasPermission("boatrace.command.create"))) {
                    noPerm(sender);
                    return false;
                }
                if (args.length < 2) {
                    needArg(sender);
                    return false;
                }
                track = RaceTrackManager.getInstance().get(args[1]);
                if (!(track == null)) {
                    sender.sendMessage(Component.translatable("error.track.exists"));
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
                sender.sendMessage(Component.translatable("success.track.create", Component.text(args[1])));
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
                        track = RaceTrackManager.getInstance().get(args[1]);
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
                            track = RaceTrackManager.getInstance().get(args[1]);
                            if (track == null) return noTrack(sender);
                            Location location = new Location(Bukkit.getWorld(track.getWorldName()), x, y, z);
                            track.setPoint(pointMap.get(sub), location);
                            StorageManager.getInstance().saveTrack(track);
                            sender.sendMessage(Component.translatable("track.modify.success", Component.text(track.getName())));
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
                    track = RaceTrackManager.getInstance().get(args[1]);
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
                track = RaceTrackManager.getInstance().get(args[1]);
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
            case "hologram":
                if (args.length < 3) return needArg(sender);
                track = RaceTrackManager.getInstance().get(args[1]);
                if (track == null) return noTrack(sender);
                switch (args[2]) {
                    case "show":
                        track.setShowHologram(true);
                        sender.sendMessage(Component.translatable("success.hologram.show"));
                        break;
                    case "hide":
                        track.setShowHologram(false);
                        sender.sendMessage(Component.translatable("success.hologram.hide"));
                        break;
                    case "setpos":
                        if (args.length == 6) {
                            try {
                                double x = Double.parseDouble(args[3]);
                                double y = Double.parseDouble(args[4]);
                                double z = Double.parseDouble(args[5]);
                                track.setHoloLocation(new Location(null, x, y, z));
                                sender.sendMessage(Component.translatable("success.hologram.setpos", Component.text(
                                    Double.toString(x) + " " + 
                                    Double.toString(y) + " " + 
                                    Double.toString(z))));
                            } catch (Exception e) {
                                sender.sendMessage(Component.translatable("error.invalid.pos"));
                            }
                        } else {
                            if (sender instanceof Player player) {
                                Location location = player.getLocation();
                                track.setHoloLocation(location);
                                sender.sendMessage(Component.translatable("success.hologram.setpos", Component.text(
                                    Double.toString(location.getX()) + " " + 
                                    Double.toString(location.getY()) + " " + 
                                    Double.toString(location.getZ()))));
                            } else {
                                return needArg(sender);
                            }
                        }
                }
                RaceTrackManager.getInstance().updateLeaderboardHologram(track);
                StorageManager.getInstance().saveTrack(track);
                break;
            case "check":
                if (sender instanceof Player p) {
                    if (args.length < 2) return needArg(sender);
                    track = RaceTrackManager.getInstance().get(args[1]);
                    if (track == null) return noTrack(p);
                    PersonalRecords record = PersonalRecordsManager.getInstance().getRecord(p.getUniqueId());
                    List<PersonalRaceResult> results = record.getResults(args[1]);
                    if (results == null) return noRecord(p);
                    int attempts = record.getAttemps();
                    sender.sendMessage(Component.translatable("info.track.attempts", Component.text(track.getName()), Component.text(attempts)));
                    int rank = 1;
                    for (PersonalRaceResult result : results) {
                        sender.sendMessage(Component.translatable("info.track.result", Component.text(rank), 
                        Component.text(track.formatTime(result.getElapsed())), Component.text(result.getTimestamp().toString())));
                        rank++;
                    }
                } else {
                    sender.sendMessage(Component.translatable("error.notplayer"));
                    return false;
                }
                break;
            default: sender.sendMessage(Component.translatable("error.command.notfound"));
        }
        return true;
    }

    private List<String> getTargetBlock(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return emptyStringList;
        }
        Block target = player.getTargetBlockExact(5);  // get target position
        if (target != null) {
            Location loc = target.getLocation();
            String coords = loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ();
            return Collections.singletonList(coords);
        }
        return emptyStringList;
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
    private boolean noRecord(CommandSender sender) {
        sender.sendMessage(Component.translatable("error.record.notfound"));
        return false;
    }
}
