package me.sub.RelicFactions.Utils.Tab;

import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TabSlot slot = (TabSlot) o;
        return Objects.equals(name, slot.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return "TabSlot{" +
                "name='" + name + '\'' +
                '}';
    }
}
