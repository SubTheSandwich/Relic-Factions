package me.sub.RelicFactions.Files.Normal;

import me.sub.RelicFactions.Utils.C;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Inventories {

    private static FileConfiguration config;
    private static File file;

    public static void load() {
        File folder = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder(), "features");
        file = new File(folder, "inventories.yml");
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

            File targetFile = new File(folder, "inventories.yml");

            if (!targetFile.exists()) {
                Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).saveResource("inventories.yml", false);

                File defaultFile = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder(), "inventories.yml");
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
            System.out.println("Unable to save file inventories.yml");
        }
    }

    public static ItemStack get(String inventory, String name) {
        if (inventory == null || name == null) return null;
        ItemStack item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(Inventories.get().getString(inventory + ".items." + name + ".item")))));
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        meta.displayName(Component.text(C.chat(Objects.requireNonNull(Inventories.get().getString(inventory + ".items." + name + ".name")))));
        item.setItemMeta(meta);
        return item;
    }
}
