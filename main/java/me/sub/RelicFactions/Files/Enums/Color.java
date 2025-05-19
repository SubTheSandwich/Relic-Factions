package me.sub.RelicFactions.Files.Enums;

public enum Color {

    BLACK,
    BLUE,
    GREEN,
    CYAN,
    RED,
    PURPLE,
    GOLD,
    LIGHT_GRAY,
    GRAY,
    LIGHT_BLUE,
    LIME,
    AQUA,
    LIGHT_RED,
    MAGENTA,
    YELLOW,
    WHITE;

    public String toColorCode() {
        return switch (this) {
            case BLACK -> "&0";
            case BLUE -> "&1";
            case GREEN -> "&2";
            case CYAN -> "&3";
            case RED -> "&4";
            case PURPLE -> "&5";
            case GOLD -> "&6";
            case LIGHT_GRAY -> "&7";
            case GRAY -> "&8";
            case LIGHT_BLUE -> "&9";
            case LIME -> "&a";
            case AQUA -> "&b";
            case LIGHT_RED -> "&c";
            case MAGENTA -> "&d";
            case YELLOW -> "&e";
            case WHITE -> "&f";
        };
    }

    public static boolean isValid(String input) {
        if (input == null) return false;
        try {
            Color.valueOf(input.toUpperCase());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
