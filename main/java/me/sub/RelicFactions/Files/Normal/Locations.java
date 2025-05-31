package me.sub.RelicFactions.Files.Normal;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Locations {

    File file;
    FileConfiguration customFile;

    public Locations() {
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions").getDataFolder(), "locations.yml");
        customFile = YamlConfiguration.loadConfiguration(file);
    }


    public FileConfiguration get() {
        return customFile;
    }


    public void save() {
        try {
            customFile.save(file);
        } catch (IOException e) {
            System.out.println("Unable to save file locations.yml");
        }
    }

}
