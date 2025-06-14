package me.sub.RelicFactions.Files.Data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class ConquestData {

    File file;
    FileConfiguration customFile;
    UUID id;

    public ConquestData(UUID uuid) {
        id = uuid;
        file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder().getPath() + "/data/conquests/", id.toString() + ".yml");
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

    public static ConquestData getByName(String name) {
        File conquestsDir = new File(
                Objects.requireNonNull(
                        Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")
                ).getDataFolder().getPath() + "/data/conquests"
        );

        File[] koths = conquestsDir.listFiles((_, filename) -> filename.endsWith(".yml"));

        if (koths != null) {
            for (File f : koths) {
                YamlConfiguration file = YamlConfiguration.loadConfiguration(f);
                if (Objects.requireNonNull(file.getString("name"))
                        .equalsIgnoreCase(name.toLowerCase())) {
                    return new ConquestData(
                            UUID.fromString(Objects.requireNonNull(file.getString("uuid")))
                    );
                }
            }
        }
        return null;
    }

    public static File[] getAll() {
        File conquestsDir = new File(
                Objects.requireNonNull(
                        Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")
                ).getDataFolder().getPath() + "/data/conquests"
        );

        return conquestsDir.listFiles((_, filename) -> filename.endsWith(".yml"));
    }

    public boolean delete() {
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
}
