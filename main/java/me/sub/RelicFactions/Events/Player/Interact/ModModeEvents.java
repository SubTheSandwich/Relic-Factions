package me.sub.RelicFactions.Events.Player.Interact;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Files.Normal.ModModeFile;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class ModModeEvents implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Player p = e.getPlayer();
        if (!(e.getRightClicked() instanceof Player clicked)) return;
        User user = User.get(p);
        if (user.getModMode() == null) return;
        ItemStack item = p.getInventory().getItemInMainHand();
        if (!item.hasItemMeta()) return;
        String name = ModModeFile.getItem(item);
        if (name == null) return;
        String action = ModModeFile.get().getString("mod-mode.items." + name + ".action");
        e.setCancelled(true);
        if (Objects.requireNonNull(action).equalsIgnoreCase("UNHANDLED")) return;
        if (action.equalsIgnoreCase("INSPECTOR")) {
            p.performCommand("invsee " + clicked.getName());
            return;
        }
        if (action.equalsIgnoreCase("FREEZE")) {
            p.performCommand("freeze " + clicked.getName());
            return;
        }
        if (action.equalsIgnoreCase("VANISH")) {
            p.performCommand("vanish");
            return;
        }
        if (action.equalsIgnoreCase("RANDOM_TP")) {
            p.performCommand("tp " + getRandomPlayer().getName());
            return;
        }
        if (action.equalsIgnoreCase("ONLINE_STAFF")) {
            String staffOrAdmin = Bukkit.getOnlinePlayers().stream()
                    .filter(player -> Permission.has(player, "staff") || Permission.has(player, "admin"))
                    .map(Player::getName)
                    .collect(java.util.stream.Collectors.joining(", "));
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.mod-mode.online-staff")).replace("%players%", staffOrAdmin)));
            return;
        }
    }

    @EventHandler
    public void onDrop(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        User user = User.get(p);
        if (user.getModMode() == null) return;
        if (user.getModMode().isInBypass()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        if (user.getModMode() == null) return;
        if (user.getModMode().isInBypass()) return;
        e.setCancelled(true);
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.mod-mode.cant"))));
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getClickedInventory() == null) return;
        if (!e.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;
        User user = User.get(p);
        if (user.getModMode() == null) return;
        if (user.getModMode().isInBypass()) return;
        e.setCancelled(true);
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.mod-mode.cant"))));
    }

    @EventHandler
    public void onClick(InventoryCreativeEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getClickedInventory() == null) return;
        if (!e.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;
        User user = User.get(p);
        if (user.getModMode() == null) return;
        if (user.getModMode().isInBypass()) return;
        e.setCancelled(true);
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.mod-mode.cant"))));
    }

    @EventHandler
    public void onSwitch(PlayerSwapHandItemsEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        if (user.getModMode() == null) return;
        if (user.getModMode().isInBypass()) return;
        e.setCancelled(true);
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.mod-mode.cant"))));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Action a = e.getAction();
        User user = User.get(p);
        if (!a.equals(Action.RIGHT_CLICK_AIR) && !a.equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (user.getModMode() == null) return;
        if (e.getItem() == null) return;
        if (!e.getItem().hasItemMeta()) return;
        String name = ModModeFile.getItem(e.getItem());
        if (name == null) return;
        String action = ModModeFile.get().getString("mod-mode.items." + name + ".action");
        e.setCancelled(true);
        if (Objects.requireNonNull(action).equalsIgnoreCase("UNHANDLED")) return;
        if (action.equalsIgnoreCase("INSPECTOR")) return; // Handled in player right click
        if (action.equalsIgnoreCase("FREEZE")) return; // Handled in player right click
        if (action.equalsIgnoreCase("VANISH")) {
            p.performCommand("vanish");
            return;
        }
        if (action.equalsIgnoreCase("RANDOM_TP")) {
            p.performCommand("tp " + getRandomPlayer().getName());
            return;
        }
        if (action.equalsIgnoreCase("ONLINE_STAFF")) {
            String staffOrAdmin = Bukkit.getOnlinePlayers().stream()
                    .filter(player -> Permission.has(player, "staff") || Permission.has(player, "admin"))
                    .map(Player::getName)
                    .collect(java.util.stream.Collectors.joining(", "));
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.mod-mode.online-staff")).replace("%players%", staffOrAdmin)));
            return;
        }
    }
    public Player getRandomPlayer() {
        List<? extends Player> players = new java.util.ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) {
            return null; // No players online
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(players.size());
        return players.get(randomIndex);
    }

    @EventHandler
    public void onView(InventoryClickEvent e) {
        if (e.getView().getTitle().equalsIgnoreCase(C.chat("&eInventory Inspector"))) e.setCancelled(true);
    }

}
