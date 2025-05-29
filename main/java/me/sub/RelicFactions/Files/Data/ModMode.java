package me.sub.RelicFactions.Files.Data;

public class ModMode {

    private boolean inMode;
    private boolean inVanish;
    private boolean hasBypass;

    public ModMode() {
        inMode = false;
        inVanish = false;
        hasBypass = false;
    }

    public boolean isInMode() {
        return inMode;
    }

    public void setInMode(boolean inMode) {
        this.inMode = inMode;
    }

    public boolean isInBypass() {
        return hasBypass;
    }

    public void setHasBypass(boolean hasBypass) {
        this.hasBypass = hasBypass;
    }

    public boolean isInVanish() {
        return inVanish;
    }

    public void setInVanish(boolean inVanish) {
        this.inVanish = inVanish;
    }
}
