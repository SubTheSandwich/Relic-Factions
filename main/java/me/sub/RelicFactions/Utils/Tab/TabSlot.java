package me.sub.RelicFactions.Utils.Tab;

import java.util.UUID;

public class TabSlot {

    private final UUID uuid;
    private final String name;

    public TabSlot(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public UUID getUUID() {
        return uuid;
    }
}
