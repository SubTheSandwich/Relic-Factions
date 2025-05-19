package me.sub.RelicFactions.Utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Permission {

    public static boolean has(Player p, String command, String type) {
        return p.hasPermission("*") || p.hasPermission("relic.*") || p.hasPermission("relic." + type) || p.hasPermission("relic.command." + command);
    }

    public static boolean has(CommandSender p, String command, String type) {
        return p.hasPermission("*") || p.hasPermission("relic.*") || p.hasPermission("relic." + type) || p.hasPermission("relic.command." + command);
    }

    public static boolean has(Player p, String command) {
        return p.hasPermission("*") || p.hasPermission("relic.*") || p.hasPermission("relic.command." + command);
    }
}
