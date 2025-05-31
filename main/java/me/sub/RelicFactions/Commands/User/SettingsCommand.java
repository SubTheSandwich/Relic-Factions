package me.sub.RelicFactions.Commands.User;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Normal.Inventories;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class SettingsCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }
        User user = User.get(p);
        Inventory inventory = Bukkit.createInventory(null, Inventories.get().getInt("settings.size"), C.chat(Objects.requireNonNull(Inventories.get().getString("settings.name"))));
        for (String i : Objects.requireNonNull(Inventories.get().getConfigurationSection("settings.items")).getKeys(false)) {

            ItemStack item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(Inventories.get().getString("settings.items." + i + ".item")))));
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.setDisplayName(C.chat(Objects.requireNonNull(Inventories.get().getString("settings.items." + i + ".name"))));
            switch (i.toUpperCase()) {
                case "MESSAGES" -> {
                    if (user.isMessages()) {
                        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                    }
                }
                case "SOUNDS" -> {
                    if (user.isMessageSounds()) {
                        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                    }
                }
                case "SCOREBOARD" -> {
                    if (user.isScoreboard()) {
                        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                    }
                }
                case "CHAT" -> {
                    if (user.isGlobalChat()) {
                        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + i.toUpperCase());
            }
            item.setItemMeta(meta);
            inventory.setItem(Inventories.get().getInt("settings.items." + i + ".slot"), item);
        }

        ItemStack fill = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(Inventories.get().getString("filler.item")))));
        ItemMeta meta = fill.getItemMeta();
        if (meta == null) return true;
        meta.setDisplayName(C.chat("&e"));
        fill.setItemMeta(meta);

        if (Inventories.get().getBoolean("filler.enabled")) {
            for (int i = 0; i < inventory.getSize(); i++) {
                if (inventory.getItem(i) != null) continue;
                inventory.setItem(i, fill);
            }
        }

        p.openInventory(inventory);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of();
    }
}
