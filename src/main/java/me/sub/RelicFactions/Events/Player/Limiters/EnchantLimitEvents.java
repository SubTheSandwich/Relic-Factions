package me.sub.RelicFactions.Events.Player.Limiters;

import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.EnchantmentTree;
import me.sub.RelicFactions.Files.Enums.Tree;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnchantLimitEvents implements Listener {

    private final Map<Inventory, UUID> playerAnvils = new HashMap<>();

    @EventHandler
    public void onEnchant(EnchantItemEvent e) {
        Player p = e.getEnchanter();
        User user = User.get(p);
        if (Permission.has(p, "admin") || p.hasPermission("relic.bypass.enchant-limiters")) return;
        for (Enchantment enchantment : e.getEnchantsToAdd().keySet()) {
            for (String s : Main.getInstance().getConfig().getStringList("limiters.enchants")) {
                String name = s.split(";")[0];
                if (name.equalsIgnoreCase(enchantment.getKey().getKey())) {
                    int levelLimit = Integer.parseInt(s.split(";")[1]);
                    if (e.getEnchantsToAdd().get(enchantment) > levelLimit) {
                        if (!user.hasFaction()) {
                            e.getEnchantsToAdd().put(enchantment, levelLimit);
                            return;
                        }
                        Faction faction = Faction.get(user.getFaction());
                        if (faction == null) {
                            e.getEnchantsToAdd().put(enchantment, levelLimit);
                            return;
                        }
                        HashMap<String, Tree> trees = faction.getTree();
                        for (Tree tree : trees.values()) {
                            if (!tree.isUnlocked()) continue;
                            if (!(tree instanceof EnchantmentTree enchantmentTree)) continue;
                            if (!enchantmentTree.getEnchantment().equals(enchantment)) continue;
                            if (enchantmentTree.getEnchantLevel() > levelLimit) levelLimit = enchantmentTree.getEnchantLevel();
                        }
                        if (e.getEnchantsToAdd().get(enchantment) > levelLimit) {
                            e.getEnchantsToAdd().put(enchantment, levelLimit);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent e) {
        if (!e.getInventory().getType().equals(InventoryType.ANVIL)) return;
        playerAnvils.put(e.getInventory(), e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!e.getInventory().getType().equals(InventoryType.ANVIL)) return;
        playerAnvils.remove(e.getInventory());
    }

    @EventHandler
    public void onPrepare(PrepareAnvilEvent e) {
        if (playerAnvils.get(e.getInventory()) == null) return;
        UUID uuid = playerAnvils.get(e.getInventory());
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) {
            playerAnvils.remove(e.getInventory());
            return;
        }
        if (Permission.has(p, "admin") || p.hasPermission("relic.bypass.enchant-limiters")) return;
        User user = User.get(p);
        ItemStack item = e.getResult();
        if (item == null) return;
        if (item.getItemMeta() == null) return;
        if (item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            for (Enchantment enchantment : meta.getStoredEnchants().keySet()) {
                for (String string : Main.getInstance().getConfig().getStringList("limiters.enchants")) {
                    String name = string.split(";")[0];
                    if (name.equalsIgnoreCase(enchantment.getKey().getKey())) {
                        int levelLimit = Integer.parseInt(string.split(";")[1]);
                        if (meta.getStoredEnchantLevel(enchantment) > levelLimit) {
                            if (!user.hasFaction()) {
                                meta.removeStoredEnchant(enchantment);
                                meta.addStoredEnchant(enchantment, levelLimit, true);
                                item.setItemMeta(meta);
                                e.setResult(item);
                                return;
                            }
                            Faction faction = Faction.get(user.getFaction());
                            if (faction == null) {
                                meta.removeStoredEnchant(enchantment);
                                meta.addStoredEnchant(enchantment, levelLimit, true);
                                item.setItemMeta(meta);
                                e.setResult(item);
                                return;
                            }
                            HashMap<String, Tree> trees = faction.getTree();
                            for (Tree tree : trees.values()) {
                                if (!tree.isUnlocked()) continue;
                                if (!(tree instanceof EnchantmentTree enchantmentTree)) continue;
                                if (!enchantmentTree.getEnchantment().equals(enchantment)) continue;
                                if (enchantmentTree.getEnchantLevel() > levelLimit)
                                    levelLimit = enchantmentTree.getEnchantLevel();
                            }
                            if (meta.getStoredEnchantLevel(enchantment) > levelLimit) {
                                meta.removeStoredEnchant(enchantment);
                                meta.addStoredEnchant(enchantment, levelLimit, true);
                                item.setItemMeta(meta);
                                e.setResult(item);
                            }
                        }
                    }
                }
            }
            return;
        }
        if (!item.getItemMeta().hasEnchants()) return;
        for (Enchantment enchantment : item.getEnchantments().keySet()) {
            for (String string : Main.getInstance().getConfig().getStringList("limiters.enchants")) {
                String name = string.split(";")[0];
                if (name.equalsIgnoreCase(enchantment.getKey().getKey())) {
                    int levelLimit = Integer.parseInt(string.split(";")[1]);
                    if (item.getEnchantments().get(enchantment) > levelLimit) {
                        if (!user.hasFaction()) {
                            item.removeEnchantment(enchantment);
                            item.addUnsafeEnchantment(enchantment, levelLimit);
                            return;
                        }
                        Faction faction = Faction.get(user.getFaction());
                        if (faction == null) {
                            item.removeEnchantment(enchantment);
                            item.addUnsafeEnchantment(enchantment, levelLimit);
                            return;
                        }
                        HashMap<String, Tree> trees = faction.getTree();
                        for (Tree tree : trees.values()) {
                            if (!tree.isUnlocked()) continue;
                            if (!(tree instanceof EnchantmentTree enchantmentTree)) continue;
                            if (!enchantmentTree.getEnchantment().equals(enchantment)) continue;
                            if (enchantmentTree.getEnchantLevel() > levelLimit) levelLimit = enchantmentTree.getEnchantLevel();
                        }
                        if (item.getEnchantments().get(enchantment) > levelLimit) {
                            item.removeEnchantment(enchantment);
                            item.addUnsafeEnchantment(enchantment, levelLimit);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onAnvil(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        User user = User.get(p);
        if (e.getClickedInventory() == null) return;
        if (e.getView().getType().equals(InventoryType.ANVIL)) {
            if (Permission.has(p, "admin") || p.hasPermission("relic.bypass.enchant-limiters")) return;
            if (e.getSlot() != 2) return;
            if (e.getClickedInventory().getItem(2) == null) return;
            ItemStack item = e.getClickedInventory().getItem(2);
            if (item == null) return;
            if (item.getItemMeta() == null) return;
            if (item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
                for (Enchantment enchantment : meta.getStoredEnchants().keySet()) {
                    for (String string : Main.getInstance().getConfig().getStringList("limiters.enchants")) {
                        String name = string.split(";")[0];
                        if (name.equalsIgnoreCase(enchantment.getKey().getKey())) {
                            int levelLimit = Integer.parseInt(string.split(";")[1]);
                            if (meta.getStoredEnchantLevel(enchantment) > levelLimit) {
                                if (!user.hasFaction()) {
                                    meta.removeStoredEnchant(enchantment);
                                    meta.addStoredEnchant(enchantment, levelLimit, true);
                                    item.setItemMeta(meta);
                                    e.getClickedInventory().setItem(2, item);
                                    return;
                                }
                                Faction faction = Faction.get(user.getFaction());
                                if (faction == null) {
                                    meta.removeStoredEnchant(enchantment);
                                    meta.addStoredEnchant(enchantment, levelLimit, true);
                                    item.setItemMeta(meta);
                                    e.getClickedInventory().setItem(2, item);
                                    return;
                                }
                                HashMap<String, Tree> trees = faction.getTree();
                                for (Tree tree : trees.values()) {
                                    if (!tree.isUnlocked()) continue;
                                    if (!(tree instanceof EnchantmentTree enchantmentTree)) continue;
                                    if (!enchantmentTree.getEnchantment().equals(enchantment)) continue;
                                    if (enchantmentTree.getEnchantLevel() > levelLimit)
                                        levelLimit = enchantmentTree.getEnchantLevel();
                                }
                                if (meta.getStoredEnchantLevel(enchantment) > levelLimit) {
                                    meta.removeStoredEnchant(enchantment);
                                    meta.addStoredEnchant(enchantment, levelLimit, true);
                                    item.setItemMeta(meta);
                                    e.getClickedInventory().setItem(2, item);
                                }
                            }
                        }
                    }
                }
                return;
            }
            if (!item.getItemMeta().hasEnchants()) return;
            for (Enchantment enchantment : item.getEnchantments().keySet()) {
                for (String string : Main.getInstance().getConfig().getStringList("limiters.enchants")) {
                    String name = string.split(";")[0];
                    if (name.equalsIgnoreCase(enchantment.getKey().getKey())) {
                        int levelLimit = Integer.parseInt(string.split(";")[1]);
                        if (item.getEnchantments().get(enchantment) > levelLimit) {
                            if (!user.hasFaction()) {
                                item.removeEnchantment(enchantment);
                                item.addUnsafeEnchantment(enchantment, levelLimit);
                                return;
                            }
                            Faction faction = Faction.get(user.getFaction());
                            if (faction == null) {
                                item.removeEnchantment(enchantment);
                                item.addUnsafeEnchantment(enchantment, levelLimit);
                                return;
                            }
                            HashMap<String, Tree> trees = faction.getTree();
                            for (Tree tree : trees.values()) {
                                if (!tree.isUnlocked()) continue;
                                if (!(tree instanceof EnchantmentTree enchantmentTree)) continue;
                                if (!enchantmentTree.getEnchantment().equals(enchantment)) continue;
                                if (enchantmentTree.getEnchantLevel() > levelLimit) levelLimit = enchantmentTree.getEnchantLevel();
                            }
                            if (item.getEnchantments().get(enchantment) > levelLimit) {
                                item.removeEnchantment(enchantment);
                                item.addUnsafeEnchantment(enchantment, levelLimit);
                            }
                        }
                    }
                }
            }
        }
    }
}