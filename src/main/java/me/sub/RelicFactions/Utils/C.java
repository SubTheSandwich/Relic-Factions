package me.sub.RelicFactions.Utils;

import me.sub.RelicFactions.Main.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.util.Objects;

public class C {

    @SuppressWarnings("deprecation")
    public static String chat(String s) {
        if (s.contains("%primary%")) s = s.replace("%primary%", Objects.requireNonNull(C.convertFromName(Main.getInstance().getConfig().getString("server.color.primary"))));
        if (s.contains("%secondary%")) s = s.replace("%secondary%", Objects.requireNonNull(C.convertFromName(Main.getInstance().getConfig().getString("server.color.secondary"))));
        if (s.contains("%servername%")) s = s.replace("%servername%", Objects.requireNonNull(Main.getInstance().getConfig().getString("server.name")));
        if (s.contains("%map-number%")) s = s.replace("%map-number%", Main.getInstance().getConfig().getInt("server.map.number") + "");
        if (s.contains("%mapstartdate%")) s = s.replace("%mapstartdate%", Objects.requireNonNull(Main.getInstance().getConfig().getString("server.map.start-date")));
        if (s.contains("%border%")) s = s.replace("%border%", Main.getInstance().getConfig().getInt("limiters.world-border") + "");
        if (s.contains("%warzone%")) s = s.replace("%warzone%", Main.getInstance().getConfig().getInt("factions.sizes.worlds.default.warzone") + "");
        if (s.contains("%discord%")) s = s.replace("%discord%", Objects.requireNonNull(Main.getInstance().getConfig().getString("server.discord")));
        if (s.contains("%website%")) s = s.replace("%website%", Objects.requireNonNull(Main.getInstance().getConfig().getString("server.website")));
        if (s.contains("%store%")) s = s.replace("%store%", Objects.requireNonNull(Main.getInstance().getConfig().getString("server.store")));
        if (s.contains("%online%")) s = s.replace("%online%", Bukkit.getOnlinePlayers().size() + "");
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String chat(String s, String alias) {
        if (s.contains("%alias%")) s = s.replace("%alias%", alias);
        return chat(s);
    }

    public static String convertFromName(String name) {
        name = name.toUpperCase();
        return switch (name) {
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
            default -> null;
        };
    }

    public static String strip(String s) {
        if (s.contains("%") || s.contains("&")) s = C.chat(s);
        Component component = LegacyComponentSerializer.legacySection().deserialize(s);
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static String strikethrough(String input) {
        StringBuilder sb = new StringBuilder();
        boolean lastWasColor = false;

        sb.append("&m");

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (lastWasColor) {
                sb.append(c);
                if (("0123456789abcdefklmnor").indexOf(Character.toLowerCase(c)) != -1 && Character.toLowerCase(c) != 'm' && Character.toLowerCase(c) != 'r') {
                    sb.append("&m");
                }
                lastWasColor = false;
            } else if (c == '&') {
                sb.append(c);
                lastWasColor = true;
            } else {
                sb.append(c);
            }
        }
        sb.append("&r");  // Reset at the end
        return sb.toString();
    }

    public static String bold(String input) {
        StringBuilder sb = new StringBuilder();
        boolean lastWasColor = false;

        sb.append("&l");

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (lastWasColor) {
                sb.append(c);
                if (("0123456789abcdefklmnor").indexOf(Character.toLowerCase(c)) != -1 && Character.toLowerCase(c) != 'l' && Character.toLowerCase(c) != 'r') {
                    sb.append("&l");
                }
                lastWasColor = false;
            } else if (c == '&') {
                sb.append(c);
                lastWasColor = true;
            } else {
                sb.append(c);
            }
        }
        sb.append("&r");  // Reset at the end
        return sb.toString();
    }

    public static String capitalizeWord(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        // Replace underscores with spaces
        String[] words = input.replace('_', ' ').split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                // Capitalize first letter, keep the rest lowercase
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
                result.append(" ");
            }
        }
        // Remove trailing space
        return result.toString().trim();
    }

    public static String serialize(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
}
