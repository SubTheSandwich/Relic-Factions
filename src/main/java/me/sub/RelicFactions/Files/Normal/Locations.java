package me.sub.RelicFactions.Files.Normal;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Locations {

    private static File file;
    private static FileConfiguration config;

    public static void load() {
        file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder(), "locations.yml");
        config = YamlConfiguration.loadConfiguration(file);
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
            System.out.println("Unable to save file locations.yml");
        }
    }
}
