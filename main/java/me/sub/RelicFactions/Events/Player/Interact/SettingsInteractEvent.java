package me.sub.RelicFactions.Events.Player.Interact;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Normal.Inventories;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class SettingsInteractEvent implements Listener {

    @EventHandler
    public void onProfile(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (e.getView().getTitle().equalsIgnoreCase(C.chat(Objects.requireNonNull(Inventories.get().getString("profile.name"))))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equalsIgnoreCase(C.chat(Objects.requireNonNull(Inventories.get().getString("settings.name"))))) return;
        if (e.getCurrentItem() == null) return;
        if (!e.getCurrentItem().hasItemMeta()) return;
        e.setCancelled(true);
        ItemStack item = e.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        for (Enchantment ench : meta.getEnchants().keySet()) {
            meta.removeEnchant(ench);
        }
        item.setItemMeta(meta);
        String clicked = null;
        for (String i : Objects.requireNonNull(Inventories.get().getConfigurationSection("settings.items")).getKeys(false)) {
            if (item.isSimilar(Inventories.get("settings", i))) {
                clicked = i;
                break;
            }
        }
        User user = User.get(p);
        if (clicked == null) return;
        switch (clicked.toUpperCase()) {
            case "MOUNTAINS" -> {
                user.setMountains(!user.isMountains());
                String message = Locale.get().getString("commands.settings.mountains");
                message = Objects.requireNonNull(message).replace("%status%", Objects.requireNonNull(user.isMountains() ? Locale.get().getString("primary.enabled") : Locale.get().getString("primary.disabled")));
                p.sendMessage(C.chat(message));
            }
            case "FOUNDDIAMONDS" -> {
                user.setFoundDiamonds(!user.isFoundDiamonds());
                String message = Locale.get().getString("commands.settings.foundDiamonds");
                message = Objects.requireNonNull(message).replace("%status%", Objects.requireNonNull(user.isFoundDiamonds() ? Locale.get().getString("primary.enabled") : Locale.get().getString("primary.disabled")));
                p.sendMessage(C.chat(message));
            }
            case "MESSAGES" -> {
                user.setMessages(!user.isMessages());
                String message = Locale.get().getString("commands.settings.messages");
                message = Objects.requireNonNull(message).replace("%status%", Objects.requireNonNull(user.isMessages() ? Locale.get().getString("primary.enabled") : Locale.get().getString("primary.disabled")));
                p.sendMessage(C.chat(message));
            }
            case "SOUNDS" -> {
                user.setMessageSounds(!user.isMessageSounds());
                String message = Locale.get().getString("commands.settings.message-sounds");
                message = Objects.requireNonNull(message).replace("%status%", Objects.requireNonNull(user.isMessageSounds() ? Locale.get().getString("primary.enabled") : Locale.get().getString("primary.disabled")));
                p.sendMessage(C.chat(message));
            }
            case "SCOREBOARD" -> {
                user.setScoreboard(!user.isScoreboard());
                String message = Locale.get().getString("commands.settings.scoreboard");
                message = Objects.requireNonNull(message).replace("%status%", Objects.requireNonNull(user.isScoreboard() ? Locale.get().getString("primary.enabled") : Locale.get().getString("primary.disabled")));
                p.sendMessage(C.chat(message));
            }
            case "CHAT" -> p.performCommand("tgc");
            default -> throw new IllegalStateException("Unexpected value: " + clicked.toUpperCase());
        }
        p.closeInventory();
        p.performCommand("settings");
    }
}
