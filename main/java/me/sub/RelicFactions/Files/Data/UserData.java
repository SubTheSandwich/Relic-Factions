package me.sub.RelicFactions.Files.Data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class UserData {

    File file;
    FileConfiguration customFile;
    UUID id;

    public UserData(UUID uuid) {
        id = uuid;
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions").getDataFolder().getPath() + "/data/profiles/", id.toString() + ".yml");
        customFile = YamlConfiguration.loadConfiguration(file);
    }

    public UUID getUUID() {
        return id;
    }

    public String getName() {
        return customFile.getString("name");
    }


    public Boolean exists() {
        return file.exists();
    }


    public void setup() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {

            }
        }
    }

    public FileConfiguration get() {
        return customFile;
    }

    public void save() {
        try {
            customFile.save(file);
        } catch (IOException e) {
            System.out.println("Couldn't save file");
        }
    }

    public void reload() {
        customFile = YamlConfiguration.loadConfiguration(file);
    }

    public static UserData getByName(String name) {
        File[] users = new File(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions").getDataFolder().getPath() + "/data/profiles").listFiles();
        if (users != null) {
            for (File f : users) {
                YamlConfiguration file = YamlConfiguration.loadConfiguration(f);
                if (file.getString("name").equalsIgnoreCase(name.toLowerCase())) {
                    return new UserData(UUID.fromString(file.getString("uuid")));
                }
            }
        }
        return null;
    }

    public static File[] getAll() {
        return new File(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions").getDataFolder().getPath() + "/data/profiles").listFiles();
    }
}