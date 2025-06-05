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
    STUCK(Main.getInstance().getConfig().getInt("timers.stuck")),
    ENDERPEARL(Main.getInstance().getConfig().getInt("timers.enderpearl")),
    APPLE(Main.getInstance().getConfig().getInt("timers.apple")),
    GAPPLE(Main.getInstance().getConfig().getInt("timers.gapple")),
    LFF(Main.getInstance().getConfig().getInt("timers.lff")),
    REQUEST(Main.getInstance().getConfig().getInt("timers.request")),
    REPORT(Main.getInstance().getConfig().getInt("timers.report")),
    LOGOUT(Main.getInstance().getConfig().getInt("timers.logout"));

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

    public static String getMessageFormat(long diffMillis) {
        long diff = diffMillis / 1000;
        long days = diff / (24 * 3600);
        long hours = (diff % (24 * 3600)) / 3600;
        long minutes = (diff % 3600) / 60;
        long seconds = diff % 60;
        StringBuilder format = new StringBuilder();

        if (days > 0) {
            format.append(days)
                    .append(" day")
                    .append(days == 1 ? "" : "s");
            if (hours > 0 || minutes > 0 || seconds > 0) format.append(" ");
        }
        if (hours > 0) {
            format.append(hours)
                    .append(" hour")
                    .append(hours == 1 ? "" : "s");
            if (minutes > 0 || seconds > 0) format.append(" ");
        }
        if (minutes > 0) {
            format.append(minutes)
                    .append(" minute")
                    .append(minutes == 1 ? "" : "s");
            if (seconds > 0) format.append(" ");
        }
        if (seconds > 0 || (days == 0 && hours == 0 && minutes == 0)) {
            format.append(seconds)
                    .append(" second")
                    .append(seconds == 1 ? "" : "s");
        }
        return format.toString();
    }

    public static String getMessageFormat(BigDecimal secondsInput) {
        long totalSeconds = secondsInput.setScale(0, java.math.RoundingMode.FLOOR).longValue();

        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds % (24 * 3600)) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder format = new StringBuilder();

        if (days > 0) {
            format.append(days)
                    .append(" day")
                    .append(days == 1 ? "" : "s");
            if (hours > 0 || minutes > 0 || seconds > 0) format.append(" ");
        }
        if (hours > 0) {
            format.append(hours)
                    .append(" hour")
                    .append(hours == 1 ? "" : "s");
            if (minutes > 0 || seconds > 0) format.append(" ");
        }
        if (minutes > 0) {
            format.append(minutes)
                    .append(" minute")
                    .append(minutes == 1 ? "" : "s");
            if (seconds > 0) format.append(" ");
        }
        if (seconds > 0 || (days == 0 && hours == 0 && minutes == 0)) {
            format.append(seconds)
                    .append(" second")
                    .append(seconds == 1 ? "" : "s");
        }
        return format.toString();
    }
}
