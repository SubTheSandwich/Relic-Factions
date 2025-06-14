package me.sub.RelicFactions.Files.Data;

public class Chat {

    private boolean muted;
    private int slowMode;

    public Chat() {
        muted = false;
        slowMode = 0;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public int getSlowMode() {
        return slowMode;
    }

    public void setSlowMode(int slowMode) {
        this.slowMode = slowMode;
    }
}
