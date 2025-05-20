package me.sub.RelicFactions.Files.Enums;

import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.Calculate;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

public enum Timer {

    STARTING(Main.getInstance().getConfig().getInt("timers.starting")),
    PVP(Main.getInstance().getConfig().getInt("timers.pvp")),
    COMBAT(Main.getInstance().getConfig().getInt("timers.combat")),
    HOME(Main.getInstance().getConfig().getInt("timers.home")),
    ENDERPEARL(Main.getInstance().getConfig().getInt("timers.enderpearl")),
    APPLE(Main.getInstance().getConfig().getInt("timers.apple")),
    GAPPLE(Main.getInstance().getConfig().getInt("timers.gapple"));

    private final int duration;

    Timer(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public static boolean isValid(String input) {
        if (input == null) return false;
        try {
            Timer.valueOf(input.toUpperCase());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static String getValidNames() {
        return String.join(", ",
                Arrays.stream(Timer.values())
                        .map(Enum::name)
                        .toArray(String[]::new)
        );
    }

    public static String format(BigDecimal time) {
        Calendar cl = Calendar.getInstance();
        cl.clear();
        cl.add(Calendar.SECOND, time.intValue());
        String format;
        String timer;
        if (time.doubleValue() >= 3600) {
            format = "HH:mm:ss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            timer = simpleDateFormat.format(cl.getTimeInMillis());
        } else if (time.doubleValue() >= 60) {
            format = "mm:ss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            timer = simpleDateFormat.format(cl.getTimeInMillis());
        } else {
            timer = Calculate.round(time.doubleValue(), 1) + "s";
        }
        return timer;
    }

    public static String format(int time) {
        Calendar cl = Calendar.getInstance();
        cl.clear();
        cl.add(Calendar.SECOND, time);
        String format;
        String timer;
        if (time >= 3600) {
            format = "HH:mm:ss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            timer = simpleDateFormat.format(cl.getTimeInMillis());
        } else if (time >= 60) {
            format = "mm:ss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            timer = simpleDateFormat.format(cl.getTimeInMillis());
        } else {
            timer = Calculate.round(time, 1) + "s";
        }
        return timer;
    }
}
