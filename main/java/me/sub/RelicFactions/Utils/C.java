package me.sub.RelicFactions.Utils;

import me.sub.RelicFactions.Main.Main;
import org.bukkit.ChatColor;

public class C {

    public static String chat(String s) {
        if (s.contains("%primary%")) s = s.replace("%primary%", C.convertFromName(Main.getInstance().getConfig().getString("server.color.primary")));
        if (s.contains("%secondary%")) s = s.replace("%secondary%", C.convertFromName(Main.getInstance().getConfig().getString("server.color.secondary")));
        if (s.contains("%servername%")) s = s.replace("%servername%", Main.getInstance().getConfig().getString("server.name"));
        if (s.contains("%map-number%")) s = s.replace("%map-number%", Main.getInstance().getConfig().getInt("server.map.number") + "");
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String chat(String s, String alias) {
        if (s.contains("%alias%")) s = s.replace("%alias%", alias);
        return chat(s);
    }

    public static String convertFromName(String name) {
        name = name.toUpperCase();
        return switch (name) {
            default -> null;
            case "BLACK" -> "&0";
            case "BLUE" -> "&1";
            case "GREEN" -> "&2";
            case "CYAN" -> "&3";
            case "RED" -> "&4";
            case "PURPLE" -> "&5";
            case "GOLD" -> "&6";
            case "LIGHT_GRAY" -> "&7";
            case "GRAY" -> "&8";
            case "LIGHT_BLUE" -> "&9";
            case "LIME" -> "&a";
            case "AQUA" -> "&b";
            case "LIGHT_RED" -> "&c";
            case "MAGENTA" -> "&d";
            case "YELLOW" -> "&e";
            case "WHITE" -> "&f";
        };
    }

    public static String strip(String s) {
        if (s.contains("&")) s = C.chat(s);
        return ChatColor.stripColor(s);
    }
}
