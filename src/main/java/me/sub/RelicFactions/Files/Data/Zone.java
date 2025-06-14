package me.sub.RelicFactions.Files.Data;

import me.sub.RelicFactions.Files.Enums.ZoneType;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Objects;

public class Zone {

    private final ZoneType type;
    private Location cornerOne;
    private Location cornerTwo;

    public Zone(ZoneType type) {
        this.type = type;
        cornerOne = null;
        cornerTwo = null;
    }

    public ZoneType getType() {
        return type;
    }

    public Location getCornerOne() {
        return cornerOne;
    }

    public void setCornerOne(Location cornerOne) {
        this.cornerOne = cornerOne;
    }

    public Location getCornerTwo() {
        return cornerTwo;
    }

    public void setCornerTwo(Location cornerTwo) {
        this.cornerTwo = cornerTwo;
    }

    public boolean isSetup() {
        return cornerOne != null && cornerTwo != null;
    }

    // Converts a Location to a string, or "null" if null
    private static String locationToString(Location loc) {
        if (loc == null) return "null";
        return Objects.requireNonNull(loc.getWorld()).getName() + "," +
                loc.getX() + "," +
                loc.getY() + "," +
                loc.getZ();
    }

    // Converts a string to a Location, or null if "null"
    private static Location stringToLocation(String str) {
        if (str == null || str.equals("null")) return null;
        String[] parts = str.split(",");
        if (parts.length != 4) return null;
        return new Location(
                Bukkit.getWorld(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3])
        );
    }

    // Serializes the Zone to a string
    public String serialize() {
        return type.name() + ";" +
                locationToString(cornerOne) + ";" +
                locationToString(cornerTwo);
    }

    // Deserializes a string to a Zone object
    public static Zone deserialize(String str) {
        if (str == null) return null;
        String[] parts = str.split(";");
        if (parts.length != 3) return null;
        ZoneType type = ZoneType.valueOf(parts[0]);
        Zone zone = new Zone(type);
        zone.setCornerOne(stringToLocation(parts[1]));
        zone.setCornerTwo(stringToLocation(parts[2]));
        return zone;
    }

    @Override
    public String toString() {
        return "Zone{" +
                "type=" + type +
                ", cornerOne=" + cornerOne +
                ", cornerTwo=" + cornerTwo +
                '}';
    }

    public String getName() {
        return type.name();
    }

    public Cuboid getCuboid() {
        return new Cuboid(cornerOne, cornerTwo);
    }
}