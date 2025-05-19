package me.sub.RelicFactions.Files.Data;

import me.sub.RelicFactions.Files.Enums.Timer;

public class PlayerTimer {

    private final Timer timer;
    private int duration;
    private boolean paused;

    public PlayerTimer(Timer timer) {
        this.timer = timer;
        this.duration = timer.getDuration();
        paused = false;
    }

    public Timer getTimer() {
        return timer;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
