package me.sub.RelicFactions.Files.Normal;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.ModMode;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class ModModeFile {

    public static FileConfiguration get() {
        File file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder(), "features" + File.separator + "mod-mode.yml");
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void save() {
        try {
            File folder = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder(), "features");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(folder, "mod-mode.yml");

            if (!file.exists()) {
                Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).saveResource("mod-mode.yml", false);


                File defaultFile = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder(), "mod-mode.yml");
                if (defaultFile.exists()) {
                    defaultFile.renameTo(file);
                }
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.save(file);
        } catch (IOException e) {
            System.out.println("Unable to save file mod-mode.yml");
        }
    }

    public static void setItems(Player player) {
        player.getInventory().clear();
        User user = User.get(player);
        ModMode modMode = user.getModMode();
        for (String i : Objects.requireNonNull(ModModeFile.get().getConfigurationSection("mod-mode.items")).getKeys(false)) {
            if (!Objects.requireNonNull(ModModeFile.get().getString("mod-mode.items." + i + ".permission")).equalsIgnoreCase("")) {
                if (!(player.hasPermission(Objects.requireNonNull(ModModeFile.get().getString("mod-mode.items." + i + ".permission")))))
                    continue;
            }
            if (i.equalsIgnoreCase("worldedit-wand") || i.equalsIgnoreCase("compass")) {
                if (Main.getInstance().getServer().getPluginManager().getPlugin("WorldEdit") == null) continue;
            }
            if (modMode.isInVanish()) {
                if (i.equalsIgnoreCase("vanish-visible")) continue;
            } else {
                if (i.equalsIgnoreCase("vanish-hidden")) continue;
            }
            ItemStack item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(ModModeFile.get().getString("mod-mode.items." + i + ".item")))));
            ItemMeta meta = item.getItemMeta();
            ArrayList<Component> lore = new ArrayList<>();
            ModModeFile.get().getStringList("mod-mode.items." + i + ".lore").forEach(s -> lore.add(Component.text(C.chat(s))));
            Objects.requireNonNull(meta).lore(lore);
            meta.displayName(Component.text(C.chat(Objects.requireNonNull(ModModeFile.get().getString("mod-mode.items." + i + ".name")))));
            item.setItemMeta(meta);
            player.getInventory().setItem(ModModeFile.get().getInt("mod-mode.items." + i + ".slot"), item);
        }
    }

    public static boolean has(String name, Player player) {
        for (String i : Objects.requireNonNull(ModModeFile.get().getConfigurationSection("mod-mode.items")).getKeys(false)) {
            ItemStack item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(ModModeFile.get().getString("mod-mode.items." + i + ".item")))));
            ItemMeta meta = item.getItemMeta();
            ArrayList<Component> lore = new ArrayList<>();
            ModModeFile.get().getStringList("mod-mode.items." + i + ".lore").forEach(s -> lore.add(Component.text(C.chat(s))));
            Objects.requireNonNull(meta).lore(lore);
            meta.displayName(Component.text(C.chat(Objects.requireNonNull(ModModeFile.get().getString("mod-mode.items." + i + ".name")))));
            item.setItemMeta(meta);
            for (ItemStack it : player.getInventory().getContents()) {
                if (it == null) continue;
                if (it.isSimilar(item)) {
                    if (i.equalsIgnoreCase(name)) return true;
                }
            }
        }
        return false;
    }

    public static String getItem(ItemStack itemStack) {
        for (String i : Objects.requireNonNull(ModModeFile.get().getConfigurationSection("mod-mode.items")).getKeys(false)) {
            ItemStack item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(ModModeFile.get().getString("mod-mode.items." + i + ".item")))));
            ItemMeta meta = item.getItemMeta();
            ArrayList<Component> lore = new ArrayList<>();
            ModModeFile.get().getStringList("mod-mode.items." + i + ".lore").forEach(s -> lore.add(Component.text(C.chat(s))));
            Objects.requireNonNull(meta).lore(lore);
            meta.displayName(Component.text(C.chat(Objects.requireNonNull(ModModeFile.get().getString("mod-mode.items." + i + ".name")))));
            item.setItemMeta(meta);
            if (item.isSimilar(itemStack)) return i;
        }
        return null;
    }
}
