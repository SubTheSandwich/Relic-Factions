package me.sub.RelicFactions.Events.Player.Server;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class UserDisconnectEvent implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        Player p = e.getPlayer();
        User user = User.get(p);
        user.setDisconnected(true);
        if (user.hasTimer("combat")) {
            Villager villager = p.getWorld().spawn(p.getLocation(), Villager.class);
            villager.setRemoveWhenFarAway(false);
            villager.setPersistent(true);
            villager.setHealth(p.getHealth());
            villager.setAI(false);
            villager.setCustomName(C.chat("&e" + user.getName()));
            NamespacedKey key = new NamespacedKey(Main.getInstance(), "logger_uuid");
            villager.getPersistentDataContainer().set(key, PersistentDataType.STRING, p.getUniqueId().toString());
            user.setLoggerUUID(villager.getUniqueId());
            user.setLastInventoryContents(p.getInventory().getContents());
            double seconds = user.getTimer("combat").getDuration().doubleValue();
            long ticks = (long) Math.ceil(seconds) * 20L;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!villager.isDead()){
                        villager.remove();
                        user.setLoggerUUID(null);
                    }
                }
            }.runTaskLater(Main.getInstance(), ticks);
        }
    }
}
