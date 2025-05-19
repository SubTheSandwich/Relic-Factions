package me.sub.RelicFactions.Files.Enums;

public enum FactionType {

    PLAYER,
    SYSTEM,
    SAFEZONE,
    KOTH,
    ROAD;

    public static boolean isValid(String input) {
        if (input == null) return false;
        try {
            FactionType.valueOf(input.toUpperCase());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
