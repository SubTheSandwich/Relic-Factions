package me.sub.RelicFactions.Utils;

public class Calculate {

    public static double round(double value, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }
}
