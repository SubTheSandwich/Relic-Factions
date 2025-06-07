package me.sub.RelicFactions.Files.Data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class MountainData {

    File file;
    FileConfiguration customFile;
    UUID id;

    public MountainData(UUID uuid) {
        id = uuid;
        file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder().getPath() + "/data/mountains/", id.toString() + ".yml");
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

    public static MountainData getByName(String name) {
        File mountainsDir = new File(
                Objects.requireNonNull(
                        Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")
                ).getDataFolder().getPath() + "/data/mountains"
        );
        File[] mountains = mountainsDir.listFiles(
                (_, filename) -> filename.toLowerCase().endsWith(".yml")
        );
        if (mountains != null) {
            for (File f : mountains) {
                YamlConfiguration file = YamlConfiguration.loadConfiguration(f);
                if (Objects.requireNonNull(file.getString("name"))
                        .equalsIgnoreCase(name.toLowerCase())) {
                    return new MountainData(
                            UUID.fromString(Objects.requireNonNull(file.getString("uuid")))
                    );
                }
            }
        }
        return null;
    }

    public static File[] getAll() {
        File mountainsDir = new File(
                Objects.requireNonNull(
                        Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")
                ).getDataFolder().getPath() + "/data/mountains"
        );
        return mountainsDir.listFiles(
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
