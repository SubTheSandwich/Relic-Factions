package me.sub.RelicFactions.Files.Normal;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class TreeFile {

    private static FileConfiguration config;
    private static File file;

    public static void load() {
        File folder = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder(), "features");
        file = new File(folder, "tree.yml");
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
            File folder = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder(), "features");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File targetFile = new File(folder, "tree.yml");

            if (!targetFile.exists()) {
                Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).saveResource("tree.yml", false);

                File defaultFile = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder(), "tree.yml");
                if (defaultFile.exists()) {
                    defaultFile.renameTo(targetFile);
                }
            }

            // Use cached config if available, otherwise load fresh
            FileConfiguration configToSave = (config != null) ? config : YamlConfiguration.loadConfiguration(targetFile);
            configToSave.save(targetFile);
            
            // Update our cached references
            file = targetFile;
            config = configToSave;
        } catch (IOException e) {
            System.out.println("Unable to save file tree.yml");
        }
    }
}
