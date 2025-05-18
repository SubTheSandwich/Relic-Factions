package me.sub.RelicFactions.Utils;

import me.sub.RelicFactions.Main.Main;

import java.text.NumberFormat;
import java.util.Locale;

public class Calculate {

    public static double round(double value, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }

    public static String formatMoney(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag(Main.getInstance().getConfig().getString("economy.type")));
        return formatter.format(amount);
    }
}
