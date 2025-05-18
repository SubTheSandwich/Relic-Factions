package me.sub.RelicFactions.Utils;

import java.util.HashMap;
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
}
