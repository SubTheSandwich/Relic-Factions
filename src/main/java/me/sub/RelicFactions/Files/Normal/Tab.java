package me.sub.RelicFactions.Files.Normal;

import me.sub.RelicFactions.Utils.C;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tab {

    public static String header;
    public static String footer;
    private static FileConfiguration config;
    private static File file;
    private static final List<String> slotLines = new ArrayList<>();

    public static void load() {
        file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder(), "tab.yml");
        config = YamlConfiguration.loadConfiguration(file);
        
        // Load header and footer as lists and join with newlines
        List<String> headerLines = config.getStringList("header");
        header = String.join("\n", headerLines);
                
        List<String> footerLines = config.getStringList("footer");
        footer = String.join("\n", footerLines);
        
        // Load slot lines
        slotLines.clear();
        for (int column = 1; column <= 4; column++) {
            for (int slot = 1; slot <= 20; slot++) {
                String line = C.chat(Objects.requireNonNull(config.getString("slots." + column + "." + slot)));
                slotLines.add(line);
            }
        }
    }

    public static List<String> getSlotLines() {
        if (slotLines.isEmpty()) {
            load();
        }
        return slotLines;
    }

    public static FileConfiguration get() {
        if (config == null) {
            load();
        }
        return config;
    }

    public static void save() {
        try {
            if (config != null && file != null) {
                config.save(file);
            }
        } catch (IOException e) {
            System.out.println("Unable to save file tab.yml");
        }
    }
}