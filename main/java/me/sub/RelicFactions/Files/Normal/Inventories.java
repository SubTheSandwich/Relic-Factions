package me.sub.RelicFactions.Files.Normal;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.ModMode;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Inventories {

    public static FileConfiguration get() {
        File file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder(), "features" + File.separator + "inventories.yml");
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void save() {
        try {
            File folder = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder(), "features");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(folder, "inventories.yml");

            if (!file.exists()) {
                Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).saveResource("inventories.yml", false);


                File defaultFile = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder(), "inventories.yml");
                if (defaultFile.exists()) {
                    defaultFile.renameTo(file);
                }
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.save(file);
        } catch (IOException e) {
            System.out.println("Unable to save file inventories.yml");
        }
    }

    public static ItemStack get(String inventory, String name) {
        if (inventory == null || name == null) return null;
        ItemStack item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(Inventories.get().getString(inventory + ".items." + name + ".item")))));
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(C.chat(Objects.requireNonNull(Inventories.get().getString(inventory + ".items." + name + ".name"))));
        item.setItemMeta(meta);
        return item;
    }
}
