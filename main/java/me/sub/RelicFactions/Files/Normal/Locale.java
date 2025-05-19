package me.sub.RelicFactions.Files.Normal;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Locale {

    public static FileConfiguration get() {
        File file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder(), "locale.yml");
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void save() {
        try {
            File file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder(), "locale.yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.save(file);
        } catch (IOException e) {
            System.out.println("Unable to save file locale.yml");
        }
    }
}
