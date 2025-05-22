package me.sub.RelicFactions.Events.Player.Interact;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.PlayerTimer;
import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ItemUseEvents implements Listener {

    @EventHandler
    public void onThrow(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Action a = e.getAction();
        if (!a.equals(Action.RIGHT_CLICK_AIR) && !a.equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (e.getItem() == null) return;
        if (!e.getItem().getType().equals(Material.ENDER_PEARL)) return;
        User user = User.get(p);
        if (user.hasTimer("enderpearl")) {
            e.setCancelled(true);
            String message = Locale.get().getString("events.timer.cooldown.enderpearl") == null ? Locale.get().getString("events.timer.cooldown.default") : Locale.get().getString("events.timer.cooldown.enderpearl");
            message = Objects.requireNonNull(message).replace("%time%", Timer.format(user.getTimer("enderpearl").getDuration()));
            p.sendMessage(C.chat(message));
            return;
        }
        PlayerTimer timer = new PlayerTimer(p.getUniqueId(), Timer.ENDERPEARL);
        user.addTimer(timer);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        User user = User.get(p);
        if (item.getType().equals(Material.GOLDEN_APPLE)) {
            if (user.hasTimer("apple")) {
                e.setCancelled(true);
                String message = Locale.get().getString("events.timer.cooldown.apple") == null ? Locale.get().getString("events.timer.cooldown.default") : Locale.get().getString("events.timer.cooldown.apple");
                message = Objects.requireNonNull(message).replace("%time%", Timer.format(user.getTimer("apple").getDuration()));
                p.sendMessage(C.chat(message));
                return;
            }
            PlayerTimer timer = new PlayerTimer(p.getUniqueId(), Timer.APPLE);
            user.addTimer(timer);
            return;
        }
        if (item.getType().equals(Material.ENCHANTED_GOLDEN_APPLE)) {
            if (user.hasTimer("gapple")) {
                e.setCancelled(true);
                String message = Locale.get().getString("events.timer.cooldown.gapple") == null ? Locale.get().getString("events.timer.cooldown.default") : Locale.get().getString("events.timer.cooldown.gapple");
                message = Objects.requireNonNull(message).replace("%time%", Timer.format(user.getTimer("gapple").getDuration()));
                p.sendMessage(C.chat(message));
                return;
            }
            PlayerTimer timer = new PlayerTimer(p.getUniqueId(), Timer.GAPPLE);
            user.addTimer(timer);
        }
    }
}
