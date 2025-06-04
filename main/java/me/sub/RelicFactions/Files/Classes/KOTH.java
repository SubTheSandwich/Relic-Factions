package me.sub.RelicFactions.Files.Classes;

import me.sub.RelicFactions.Files.Data.Cuboid;
import me.sub.RelicFactions.Files.Data.KOTHData;
import me.sub.RelicFactions.Main.Main;
import org.bukkit.Location;

import java.util.Objects;
import java.util.UUID;

public class KOTH {

    private final KOTHData kothData;
    private final UUID uuid;
    private String name;
    private Location positionOne;
    private Location positionTwo;
    private boolean pearlable;
    private boolean special;
    private boolean modified;
    private int time;
    private UUID faction;

    public KOTH(KOTHData kothData) {
        this.kothData = kothData;
        uuid = kothData.getUUID();
        name = kothData.getName();
        pearlable = kothData.get().getBoolean("pearlable");
        special = kothData.get().getBoolean("special");
        positionOne = kothData.get().getLocation("positionOne") == null ? null : kothData.get().getLocation("positionOne");
        positionTwo = kothData.get().getLocation("positionTwo") == null ? null : kothData.get().getLocation("positionTwo");
        time = kothData.get().getInt("time");
        faction = kothData.get().getString("faction") == null ? null : UUID.fromString(Objects.requireNonNull(kothData.get().getString("faction")));
        modified = false;
    }

    public KOTHData getKothData() {
        return kothData;
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

    public boolean isPearlable() {
        return pearlable;
    }

    public void setPearlable(boolean pearlable) {
        modified = true;
        this.pearlable = pearlable;
    }

    public boolean isSpecial() {
        return special;
    }

    public void setSpecial(boolean special) {
        modified = true;
        this.special = special;
    }

    public Location getPositionTwo() {
        return positionTwo;
    }

    public void setPositionTwo(Location positionTwo) {
        modified = true;
        this.positionTwo = positionTwo;
    }

    public Location getPositionOne() {
        return positionOne;
    }

    public void setPositionOne(Location positionOne) {
        modified = true;
        this.positionOne = positionOne;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        modified = true;
        this.time = time;
    }

    public static KOTH get(UUID uuid) {
        return Main.getInstance().koths.getOrDefault(uuid, null);
    }

    public static KOTH get(String name) {
        return Main.getInstance().kothNameHolder.getOrDefault(name.toLowerCase(), null);
    }

    public boolean isSetup() {
        return positionOne != null && positionTwo != null;
    }

    public void setFaction(UUID faction) {
        modified = true;
        this.faction = faction;
    }

    public UUID getFaction() {
        return faction;
    }

    public Cuboid getCuboid() {
        if (positionTwo == null || positionOne == null) return null;
        return new Cuboid(positionOne, positionTwo);
    }
}
