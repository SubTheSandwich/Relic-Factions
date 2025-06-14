package me.sub.RelicFactions.Events.Player.Limiters;

import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class EnchantLimitEvents implements Listener {

    @EventHandler
    public void onEnchant(EnchantItemEvent e) {
        Player p = e.getEnchanter();
        if (Permission.has(p, "admin") || p.hasPermission("relic.bypass.enchant-limiters")) return;
        for (Enchantment enchantment : e.getEnchantsToAdd().keySet()) {
            for (String s : Main.getInstance().getConfig().getStringList("limiters.enchants")) {
                String name = s.split(";")[0];
                if (name.equalsIgnoreCase(enchantment.getKey().getKey())) {
                    int levelLimit = Integer.parseInt(s.split(";")[1]);
                    if (e.getEnchantsToAdd().get(enchantment) > levelLimit) {
                        e.getEnchantsToAdd().put(enchantment, levelLimit);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onAnvil(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getClickedInventory() == null) return;
        if (e.getView().getType().equals(InventoryType.ANVIL)) {
            if (Permission.has(p, "admin") || p.hasPermission("relic.bypass.enchant-limiters")) return;
            if (e.getSlot() != 2) return;
            if (e.getClickedInventory().getItem(2) == null) return;
            ItemStack item = e.getClickedInventory().getItem(2);
            if (item == null) return;
            if (item.getItemMeta() == null) return;
            if (!item.getItemMeta().hasEnchants()) return;
            for (Enchantment enchantment : item.getEnchantments().keySet()) {
                for (String string : Main.getInstance().getConfig().getStringList("limiters.enchants")) {
                    String name = string.split(";")[0];
                    if (name.equalsIgnoreCase(enchantment.getKey().getKey())) {
                        int levelLimit = Integer.parseInt(string.split(";")[1]);
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