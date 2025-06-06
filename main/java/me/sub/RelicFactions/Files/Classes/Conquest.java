package me.sub.RelicFactions.Files.Classes;

import me.sub.RelicFactions.Files.Data.ConquestData;
import me.sub.RelicFactions.Files.Data.Zone;
import me.sub.RelicFactions.Files.Enums.ZoneType;
import me.sub.RelicFactions.Main.Main;

import java.util.UUID;

public class Conquest {

    private final ConquestData conquestData;
    private final UUID uuid;
    private String name;
    private Zone red;
    private Zone green;
    private Zone yellow;
    private Zone blue;

    private boolean modified;

    public Conquest(ConquestData conquestData) {
        this.conquestData = conquestData;
        uuid = conquestData.getUUID();
        name = conquestData.getName();
        red = Zone.deserialize(conquestData.get().getString("red"));
        green = Zone.deserialize(conquestData.get().getString("green"));
        yellow = Zone.deserialize(conquestData.get().getString("yellow"));
        blue = Zone.deserialize(conquestData.get().getString("blue"));
        modified = false;
    }

    public ConquestData getConquestData() {
        return conquestData;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        modified = true;
        this.name = name;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public Zone getRed() {
        return red;
    }

    public void setRed(Zone red) {
        modified = true;
        this.red = red;
    }

    public Zone getGreen() {
        return green;
    }

    public void setGreen(Zone green) {
        modified = true;
        this.green = green;
    }

    public Zone getYellow() {
        return yellow;
    }

    public void setYellow(Zone yellow) {
        modified = true;
        this.yellow = yellow;
    }

    public Zone getBlue() {
        return blue;
    }

    public void setBlue(Zone blue) {
        modified = true;
        this.blue = blue;
    }

    public static Conquest get(UUID uuid) {
        return Main.getInstance().conquests.getOrDefault(uuid, null);
    }

    public static Conquest get(String name) {
        return Main.getInstance().conquestNameHolder.getOrDefault(name.toLowerCase(), null);
    }

    public Zone getZone(ZoneType zoneType) {
        switch (zoneType) {
            case RED -> {
                return red;
            }
            case BLUE -> {
                return blue;
            }
            case GREEN -> {
                return green;
            }
            case YELLOW -> {
                return yellow;
            }
            case null, default -> {
                return null;
            }
        }
    }

    public boolean isSetup() {
        return red.isSetup() && yellow.isSetup() && blue.isSetup() && green.isSetup();
    }
}
