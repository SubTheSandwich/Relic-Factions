package me.sub.RelicFactions.Utils.Tab;

import java.util.ArrayList;
import java.util.UUID;

public class TabPlayer {

    private final UUID uuid;
    private ArrayList<TabSlot> slots;
    private String header;
    private String footer;
    private int currentKoth;
    private int timeSinceLastKothUpdate;

    public TabPlayer(UUID uuid) {
        this.uuid = uuid;
        slots = new ArrayList<>();
        header = null;
        footer = null;
        currentKoth = 0;
        timeSinceLastKothUpdate = 0;
    }

    public UUID getUUID() {
        return uuid;
    }

    public ArrayList<TabSlot> getSlots() {
        return slots;
    }

    public void setSlots(ArrayList<TabSlot> slots) {
        this.slots = slots;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public int getCurrentKoth() {
        return currentKoth;
    }

    public void setCurrentKoth(int currentKoth) {
        this.currentKoth = currentKoth;
    }

    public int getTimeSinceLastKothUpdate() {
        return timeSinceLastKothUpdate;
    }

    public void setTimeSinceLastKothUpdate(int timeSinceLastKothUpdate) {
        this.timeSinceLastKothUpdate = timeSinceLastKothUpdate;
    }
}
