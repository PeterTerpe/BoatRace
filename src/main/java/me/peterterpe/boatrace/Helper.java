package me.peterterpe.boatrace;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.kyori.adventure.text.format.NamedTextColor;

public final class Helper {
    public static void setPlayerNameColor(Player player, NamedTextColor color) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        // Create or fetch a team for that color:
        String teamName = "boatrace_" + color.toString();
        Team team = board.getTeam(teamName);
        if (team == null) {
            team = board.registerNewTeam(teamName);
            team.color(color);
        }

        // Remove the player from any other color teams on this scoreboard:
        for (Team t : board.getTeams()) {
            if (t != team && t.hasEntry(player.getName())) {
                t.removeEntry(player.getName());
            }
        }

        // Finally, add them to the correct team:
        team.addEntry(player.getName());
    }

    public static void setNameColorByStatus(Player player, int status) {
        switch (status) {
            case 0:
                // Default white
                setPlayerNameColor(player, NamedTextColor.WHITE);
                break;
            case 1:
                // Joined race
                setPlayerNameColor(player, NamedTextColor.YELLOW);
            case 2:
                // Ready for race
                setPlayerNameColor(player, NamedTextColor.GREEN);
            case 3:
                //Racing
                setPlayerNameColor(player, NamedTextColor.AQUA);
            default:
                break;
        }
    }
}
