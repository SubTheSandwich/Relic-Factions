package me.sub.RelicFactions.Files.Data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class KOTHData {

    File file;
    FileConfiguration customFile;
    UUID id;

    public KOTHData(UUID uuid) {
        id = uuid;
        file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder().getPath() + "/data/koths/", id.toString() + ".yml");
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
            } catch (IOException _) {

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

    public static KOTHData getByName(String name) {
        File[] koths = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder().getPath() + "/data/koths").listFiles();
        if (koths != null) {
            for (File f : koths) {
                YamlConfiguration file = YamlConfiguration.loadConfiguration(f);
                if (Objects.requireNonNull(file.getString("name")).equalsIgnoreCase(name.toLowerCase())) {
                    return new KOTHData(UUID.fromString(Objects.requireNonNull(file.getString("uuid"))));
                }
            }
        }
        return null;
    }

    public static File[] getAll() {
        return new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder().getPath() + "/data/koths").listFiles();
    }

    public boolean delete() {
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
}
