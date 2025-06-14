package me.sub.RelicFactions.Files.Enums;

public enum ZoneType {

    RED,
    GREEN,
    BLUE,
    YELLOW;

    public static boolean isValid(String input) {
        if (input == null) return false;
        try {
            ZoneType.valueOf(input.toUpperCase());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
