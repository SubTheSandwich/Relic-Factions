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
    public static boolean enabled;
    public static boolean headerEnabled;
    public static boolean footerEnabled;

    public static void load() {
        file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder(), "tab.yml");
        config = YamlConfiguration.loadConfiguration(file);

        enabled = config.getBoolean("tab.enabled");
        headerEnabled = config.getBoolean("tab.header.enabled");
        footerEnabled = config.getBoolean("tab.footer.enabled");
        
        // Load header and footer as lists and join with newlines
        List<String> headerLines = config.getStringList("tab.header.text");
        header = String.join("\n", headerLines);
                
        List<String> footerLines = config.getStringList("tab.footer.text");
        footer = String.join("\n", footerLines);
        
        // Load slot lines
        slotLines.clear();
        for (String s : config.getStringList("tab.left-rows")) {
            slotLines.add(C.chat(s));
        }
        for (String s : config.getStringList("tab.center-rows")) {
            slotLines.add(C.chat(s));
        }
        for (String s : config.getStringList("tab.right-rows")) {
            slotLines.add(C.chat(s));
        }
        for (String s : config.getStringList("tab.far-right-rows")) {
            slotLines.add(C.chat(s));
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