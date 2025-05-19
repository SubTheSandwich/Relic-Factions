package me.sub.RelicFactions.Utils;

import me.sub.RelicFactions.Files.Data.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class Maps {

    public static String serialize(HashMap<UUID, Integer> map) {
        if (map.isEmpty()) return "";
        return map.entrySet().stream()
                .map(e -> e.getKey().toString() + ":" + e.getValue())
                .collect(Collectors.joining(";"));
    }

    public static HashMap<UUID, Integer> deserialize(String str) {
        HashMap<UUID, Integer> map = new HashMap<>();
        if (str == null || str.isEmpty()) return map;
        String[] entries = str.split(";");
        for (String entry : entries) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                try {
                    UUID key = UUID.fromString(parts[0]);
                    Integer value = Integer.parseInt(parts[1]);
                    map.put(key, value);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return map;
    }

    public static String uuidListToString(ArrayList<UUID> list) {
        return list.stream()
                .map(UUID::toString)
                .collect(Collectors.joining(","));
    }

    public static ArrayList<UUID> stringToUuidList(String str) {
        ArrayList<UUID> list = new ArrayList<>();
        if (str == null || str.isEmpty()) return list;
        for (String s : str.split(",")) {
            try {
                list.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {}
        }
        return list;
    }

    public static String cuboidListToString(ArrayList<Cuboid> cuboids) {
        StringBuilder sb = new StringBuilder();
        for (Cuboid cuboid : cuboids) {
            String part = Objects.requireNonNull(cuboid.getPoint1().getWorld()).getName() + "," +
                    cuboid.getPoint1().getBlockX() + "," +
                    cuboid.getPoint1().getBlockY() + "," +
                    cuboid.getPoint1().getBlockZ() + "," +
                    cuboid.getPoint2().getBlockX() + "," +
                    cuboid.getPoint2().getBlockY() + "," +
                    cuboid.getPoint2().getBlockZ();
            sb.append(part).append(";");
        }
        if (!sb.isEmpty()) sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public static ArrayList<Cuboid> stringToCuboidList(String s) {
        ArrayList<Cuboid> cuboids = new ArrayList<>();
        if (s == null || s.isEmpty()) return cuboids;
        String[] parts = s.split(";");
        for (String part : parts) {
            String[] data = part.split(",");
            if (data.length != 7) continue;
            String worldName = data[0];
            int x1 = Integer.parseInt(data[1]);
            int y1 = Integer.parseInt(data[2]);
            int z1 = Integer.parseInt(data[3]);
            int x2 = Integer.parseInt(data[4]);
            int y2 = Integer.parseInt(data[5]);
            int z2 = Integer.parseInt(data[6]);
            org.bukkit.World world = Bukkit.getWorld(worldName);
            if (world == null) continue;
            Location loc1 = new Location(world, x1, y1, z1);
            Location loc2 = new Location(world, x2, y2, z2);
            cuboids.add(new Cuboid(loc1, loc2));
        }
        return cuboids;
    }

}
