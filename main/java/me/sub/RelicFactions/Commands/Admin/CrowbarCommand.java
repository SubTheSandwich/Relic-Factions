package me.sub.RelicFactions.Commands.Admin;

import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CrowbarCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!Permission.has(sender, "crowbar", "admin")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.crowbar.usage"))));
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
            return true;
        }

        try {
            int ignored = Integer.parseInt(args[1]);
        } catch (NumberFormatException ignored) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
            return true;
        }
        int uses = Integer.parseInt(args[1]);
        if (uses < 0) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
            return true;
        }

        ItemStack crowbar = getCrowbar(uses);
        sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.crowbar.success")).replace("%player%", player.getName()).replace("%uses%", uses + "")));
        player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.crowbar.target")).replace("%uses%", uses + "")));
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(crowbar);
        if (!leftovers.isEmpty()) {
            Location dropLoc = player.getLocation();
            leftovers.values().forEach(leftover ->
                    player.getWorld().dropItemNaturally(dropLoc, leftover)
            );
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!Permission.has(sender, "crowbar", "admin")) {
            return List.of();
        }
        if (args.length == 1) return null;
        return List.of();
    }

    public static ItemStack getCrowbar(int uses) {
        ItemStack itemStack = new ItemStack(Material.DIAMOND_HOE);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return null;
        ArrayList<String> lore = new ArrayList<>();
        lore.add(C.chat("&eUses: &b" + uses));
        meta.setLore(lore);
        NamespacedKey use = new NamespacedKey(Main.getInstance(), "crowbar_uses");
        NamespacedKey max = new NamespacedKey(Main.getInstance(), "maxUses");
        meta.getPersistentDataContainer().set(use, PersistentDataType.INTEGER, uses);
        meta.getPersistentDataContainer().set(max, PersistentDataType.INTEGER, uses);
        meta.setDisplayName(C.chat("&bCrowbar"));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static ItemStack getCrowbar(int uses, int maxUses) {
        ItemStack itemStack = new ItemStack(Material.DIAMOND_HOE);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return null;
        ArrayList<String> lore = new ArrayList<>();
        lore.add(C.chat("&eUses: &b" + uses));
        meta.setLore(lore);
        NamespacedKey use = new NamespacedKey(Main.getInstance(), "crowbar_uses");
        NamespacedKey max = new NamespacedKey(Main.getInstance(), "maxUses");
        meta.getPersistentDataContainer().set(use, PersistentDataType.INTEGER, uses);
        meta.getPersistentDataContainer().set(max, PersistentDataType.INTEGER, maxUses);
        meta.setDisplayName(C.chat("&bCrowbar"));
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
