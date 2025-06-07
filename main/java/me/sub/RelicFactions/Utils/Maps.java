package me.sub.RelicFactions.Utils;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.Cuboid;
import me.sub.RelicFactions.Files.Data.CustomTimer;
import me.sub.RelicFactions.Files.Data.PlayerTimer;
import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Maps {

    public static String serializeCustomMap(HashMap<String, CustomTimer> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, CustomTimer> entry : map.entrySet()) {
            sb.append(entry.getKey().replace(",", "\\,"))
                    .append("=")
                    .append(entry.getValue().serialize())
                    .append("\n");
        }
        return sb.toString();
    }

    public static HashMap<String, CustomTimer> deserializeCustomMap(String s) {
        HashMap<String, CustomTimer> map = new HashMap<>();
        if (s == null || s.isEmpty()) return map;
        String[] lines = s.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            int idx = line.indexOf('=');
            if (idx == -1) continue;
            String key = line.substring(0, idx).replace("\\,", ",");
            String value = line.substring(idx + 1);
            CustomTimer timer = CustomTimer.deserialize(value);
            map.put(key, timer);
        }
        return map;
    }

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

    public static String timersToString(HashMap<String, PlayerTimer> timers) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, PlayerTimer> entry : timers.entrySet()) {
            PlayerTimer pt = entry.getValue();
            String part = entry.getKey() + "," +
                    pt.getUUID().toString() + "," +
                    pt.getTimer().name() + "," +
                    pt.getDuration().toPlainString() + "," +
                    pt.isPaused();
            sb.append(part).append(";");
        }
        if (!sb.isEmpty()) sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public static HashMap<String, PlayerTimer> stringToTimers(String s) {
        HashMap<String, PlayerTimer> timers = new HashMap<>();
        if (s == null || s.isEmpty()) return timers;
        String[] parts = s.split(";");
        for (String part : parts) {
            String[] data = part.split(",");
            if (data.length != 5) continue;
            String key = data[0];
            UUID uuid = UUID.fromString(data[1]);
            Timer timer = Timer.valueOf(data[2]);
            BigDecimal duration = new BigDecimal(data[3]);
            boolean paused = Boolean.parseBoolean(data[4]);
            PlayerTimer pt = new PlayerTimer(uuid, timer, duration, paused);
            timers.put(key, pt);
        }
        return timers;
    }

    public static String toBase64(User user, ItemStack[] items) {
        if (items == null) return null;
        try {
            YamlConfiguration config = new YamlConfiguration();
            config.set("items", items);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            writer.write(config.saveToString());
            writer.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            Main.getInstance().getLogger().severe("Failed to convert to Base64. File UUID: " + user.getUUID().toString());
            return null;
        }
    }

    public static ItemStack[] fromBase64(User user, String data) {
        if (data == null) return new ItemStack[0];
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            String yaml = new String(bytes, StandardCharsets.UTF_8);
            YamlConfiguration config = new YamlConfiguration();
            config.loadFromString(yaml);
            List<?> rawList = config.getList("items");
            if (rawList == null) return new ItemStack[0];
            List<ItemStack> items = rawList.stream()
                    .filter(ItemStack.class::isInstance)
                    .map(ItemStack.class::cast)
                    .toList();
            return items.toArray(new ItemStack[0]);
        } catch (Exception e) {
            Main.getInstance().getLogger().severe("Failed to convert from Base64. File UUID: " + user.getUUID().toString());
            return new ItemStack[0];
        }
    }


}
