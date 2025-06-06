package me.sub.RelicFactions.Files.Data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class FactionData {

    File file;
    FileConfiguration customFile;
    UUID id;

    public FactionData(UUID uuid) {
        id = uuid;
        file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder().getPath() + "/data/factions/", id.toString() + ".yml");
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

    public static FactionData getByName(String name) {
        File factionsDir = new File(
                Objects.requireNonNull(
                        Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")
                ).getDataFolder().getPath() + "/data/factions"
        );
        File[] users = factionsDir.listFiles(
                (dir, filename) -> filename.toLowerCase().endsWith(".yml")
        );
        if (users != null) {
            for (File f : users) {
                YamlConfiguration file = YamlConfiguration.loadConfiguration(f);
                if (Objects.requireNonNull(file.getString("name"))
                        .equalsIgnoreCase(name.toLowerCase())) {
                    return new FactionData(
                            UUID.fromString(Objects.requireNonNull(file.getString("uuid")))
                    );
                }
            }
        }
        return null;
    }

    public static File[] getAll() {
        File factionsDir = new File(
                Objects.requireNonNull(
                        Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")
                ).getDataFolder().getPath() + "/data/factions"
        );
        return factionsDir.listFiles(
                (_, filename) -> filename.toLowerCase().endsWith(".yml")
        );
    }

    public boolean delete() {
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
}