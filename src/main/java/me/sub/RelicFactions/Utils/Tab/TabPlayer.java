package me.sub.RelicFactions.Utils.Tab;

import java.util.ArrayList;
import java.util.UUID;

public class TabPlayer {

    private final UUID uuid;
    private ArrayList<TabSlot> slots;
    private String header;
    private String footer;

    public TabPlayer(UUID uuid) {
        this.uuid = uuid;
        slots = new ArrayList<>();
        header = null;
        footer = null;
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
}
