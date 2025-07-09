package me.sub.RelicFactions.Commands.User;

import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.TreeHandler;
import me.sub.RelicFactions.Files.Enums.FactionType;
import me.sub.RelicFactions.Files.Enums.Tree;
import me.sub.RelicFactions.Files.Normal.Inventories;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResearchCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }
        User user = User.get(p);
        TreeHandler treeHandler = user.getTreeHandler();
        if (args.length == 1) {
            if (!p.hasPermission("relic.command.research.others")) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                return true;
            }
            Faction faction = Faction.get(args[0]);
            if (faction == null) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.faction-only"))));
                return true;
            }

            if (!faction.getType().equals(FactionType.PLAYER)) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.player-only"))));
                return true;
            }
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.research.open.other")).replace("%faction%", faction.getName())));
            p.openInventory(getTree(faction));
            treeHandler.setFaction(faction.getUUID());
            return true;
        }
        if (args.length == 2) {
            if (!p.hasPermission("relic.command.research.reset")) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                return true;
            }
            if (args[0].equalsIgnoreCase("reset")) {
                Faction faction = Faction.get(args[1]);
                if (faction == null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.faction-only"))));
                    return true;
                }
                if (!faction.getType().equals(FactionType.PLAYER)) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.player-only"))));
                    return true;
                }
                faction.setTree(Main.getInstance().getDefaultTree());
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.research.delete")).replace("%faction%", faction.getName())));
                return true;
            }
        }
        if (!user.hasFaction()) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
            return true;
        }
        if (Faction.getAt(p.getLocation()) == null) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.research.cannot"))));
            return true;
        }
        Faction faction = Faction.getAt(p.getLocation());
        if (!Objects.requireNonNull(faction).getType().equals(FactionType.SAFEZONE) && !Objects.requireNonNull(faction).getType().equals(FactionType.PLAYER)) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.research.cannot"))));
            return true;
        }
        if (faction.getType().equals(FactionType.PLAYER)) {
            if (!faction.getUUID().equals(user.getFaction())) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.research.cannot"))));
                return true;
            }
        }
        p.openInventory(getTree(Faction.get(user.getFaction())));
        treeHandler.setFaction(user.getFaction());
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.research.open.self")).replace("%faction%", faction.getName())));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String alias,
            @NotNull String @NotNull [] args
    ) {
        if (!(sender instanceof Player p)) return List.of();

        // Tab complete for first argument
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            // Suggest "reset" if player has permission
            if (p.hasPermission("relic.command.research.reset") && "reset".startsWith(args[0].toLowerCase())) {
                suggestions.add("reset");
            }

            // Suggest faction names if player has permission
            if (p.hasPermission("relic.command.research.others")) {
                String input = args[0].toLowerCase();
                for (Faction faction : Main.getInstance().factions.values()) {
                    if (faction.getType() != FactionType.PLAYER) continue;
                    String name = faction.getName();
                    if (name.toLowerCase().startsWith(input)) {
                        suggestions.add(name);
                    }
                }
            }
            return suggestions;
        }

        // Tab complete for second argument (after "reset")
        if (args.length == 2 && args[0].equalsIgnoreCase("reset") && p.hasPermission("relic.command.research.reset")) {
            String input = args[1].toLowerCase();
            List<String> suggestions = new ArrayList<>();
            for (Faction faction : Main.getInstance().factions.values()) {
                if (faction.getType() != FactionType.PLAYER) continue;
                String name = faction.getName();
                if (name.toLowerCase().startsWith(input)) {
                    suggestions.add(name);
                }
            }
            return suggestions;
        }

        return List.of();
    }

    public static Inventory getTree(Faction faction) {
        Inventory inventory = Bukkit.createInventory(null, Inventories.get().getInt("tree.size"), Component.text(C.chat(Objects.requireNonNull(Inventories.get().getString("tree.name")))));

        for (String s : Objects.requireNonNull(Inventories.get().getConfigurationSection("tree.items")).getKeys(false)) {
            String i = "tree.items." + s + ".";

            if (Inventories.get().getString(i + "id") == null) {
                ItemStack item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(Inventories.get().getString(i + "item")))));
                ItemMeta meta = item.getItemMeta();
                meta.addItemFlags(ItemFlag.values());
                meta.displayName(Component.text(C.chat(Objects.requireNonNull(Inventories.get().getString(i + "name")))));
                ArrayList<Component> lore = new ArrayList<>();
                for (String l : Inventories.get().getStringList(i + "lore")) {
                    lore.add(Component.text(C.chat(l)));
                }
                meta.lore(lore);
                item.setItemMeta(meta);
                inventory.setItem(Inventories.get().getInt(i + "slot"), item);
                continue;
            }
            Tree tree = faction.getTree().get(Objects.requireNonNull(Inventories.get().getString(i + "id")));
            if (tree == null) {
                ItemStack item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(Inventories.get().getString(i + "item")))));
                ItemMeta meta = item.getItemMeta();
                meta.addItemFlags(ItemFlag.values());
                meta.displayName(Component.text(C.chat(Objects.requireNonNull(Inventories.get().getString(i + "name")))));
                ArrayList<Component> lore = new ArrayList<>();
                for (String l : Inventories.get().getStringList(i + "lore")) {
                    lore.add(Component.text(C.chat(l)));
                }
                meta.lore(lore);
                item.setItemMeta(meta);
                inventory.setItem(Inventories.get().getInt(i + "slot"), item);
                continue;
            }
            ItemStack item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(Inventories.get().getString(i + "item")))));
            ItemMeta meta = item.getItemMeta();
            meta.addItemFlags(ItemFlag.values());
            meta.displayName(Component.text(C.chat(Objects.requireNonNull(Inventories.get().getString(i + "name")))));
            if (tree.isUnlocked()) {
                ItemStack unlocked = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(Inventories.get().getString("tree.unlock.item")))));
                ItemMeta unlockMeta = unlocked.getItemMeta();
                unlockMeta.addItemFlags(ItemFlag.values());
                unlockMeta.displayName(Component.text(C.chat(Objects.requireNonNull(Inventories.get().getString(i + "name")))));
                ArrayList<Component> lore = new ArrayList<>();
                if (Inventories.get().getBoolean("tree.unlock.lore.retain-previous")) {
                    for (String l : Inventories.get().getStringList(i + "lore")) {
                        if (l.contains("%points%")) l = l.replace("%points%", tree.getPoints() + "");
                        lore.add(Component.text(C.chat(l)));
                    }
                } else {
                    for (String l : Inventories.get().getStringList("tree.unlock.lore.text")) {
                        if (l.contains("%points%")) l = l.replace("%points%", tree.getPoints() + "");
                        lore.add(Component.text(C.chat(l)));
                    }
                }
                unlockMeta.lore(lore);
                NamespacedKey name = new NamespacedKey(Main.getInstance(), "nodeRemoveName");
                unlockMeta.getPersistentDataContainer().set(name, PersistentDataType.STRING, tree.getName());
                unlocked.setItemMeta(unlockMeta);
                inventory.setItem(Inventories.get().getInt(i + "slot"), unlocked);
                continue;
            }
            ArrayList<Component> lore = new ArrayList<>();
            for (String l : Inventories.get().getStringList(i + "lore")) {
                if (l.contains("%points%")) l = l.replace("%points%", tree.getPoints() + "");
                lore.add(Component.text(C.chat(l)));
            }
            NamespacedKey name = new NamespacedKey(Main.getInstance(), "nodeName");
            meta.getPersistentDataContainer().set(name, PersistentDataType.STRING, tree.getName());
            meta.lore(lore);
            item.setItemMeta(meta);
            inventory.setItem(Inventories.get().getInt(i + "slot"), item);
        }

        ItemStack fill = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(Inventories.get().getString("filler.item")))));
        ItemMeta meta = fill.getItemMeta();
        if (meta == null) return inventory;
        meta.displayName(Component.text(C.chat("&e")));
        fill.setItemMeta(meta);

        if (Inventories.get().getBoolean("filler.enabled")) {
            for (int i = 0; i < inventory.getSize(); i++) {
                if (inventory.getItem(i) != null) continue;
                inventory.setItem(i, fill);
            }
        }

        return inventory;
    }
}
