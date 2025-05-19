package me.sub.RelicFactions.Files.Enums;

import me.sub.RelicFactions.Main.Main;

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
}
