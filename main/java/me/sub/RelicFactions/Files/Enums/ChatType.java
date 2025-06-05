package me.sub.RelicFactions.Files.Enums;

public enum ChatType {

    PUBLIC,
    ALLY,
    FACTION;

    public static boolean isValid(String input) {
        if (input == null) return false;
        try {
            ChatType.valueOf(input.toUpperCase());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
