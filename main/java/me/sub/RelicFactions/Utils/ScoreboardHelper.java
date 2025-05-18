package me.sub.RelicFactions.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreboardHelper {

    private static final Map<Player, ScoreboardHelper> helpers = new HashMap<>();

    private final Player player;
    private final Scoreboard scoreboard;
    private final Objective objective;

    @SuppressWarnings("deprecation")
    public ScoreboardHelper(Player player, String title) {
        this.player = player;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective(
                "sidebar",
                "dummy",
                ChatColor.translateAlternateColorCodes('&', title)
        );
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(scoreboard);
        helpers.put(player, this);
    }

    public void setLines(List<String> lines) {
        // Clear previous scores
        scoreboard.getEntries().forEach(scoreboard::resetScores);

        int score = lines.size();
        for (String line : lines) {
            // Each line must be unique, so add color codes or spaces if needed
            String uniqueLine = ChatColor.translateAlternateColorCodes('&', line);
            objective.getScore(uniqueLine).setScore(score--);
        }
    }

    public void clear() {
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        helpers.remove(player);
    }

    public static ScoreboardHelper get(Player player) {
        return helpers.get(player);
    }

    public static void remove(Player player) {
        ScoreboardHelper helper = helpers.remove(player);
        if (helper != null) {
            helper.clear();
        }
    }
}
