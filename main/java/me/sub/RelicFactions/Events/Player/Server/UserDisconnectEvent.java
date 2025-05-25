package me.sub.RelicFactions.Events.Player.Server;

import me.sub.RelicFactions.Files.Classes.User;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class UserDisconnectEvent implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        user.setDisconnected(true);
        if (user.hasTimer("combat")) {
            Villager villager = p.getWorld().spawn(p.getLocation(), Villager.class);
            villager.setAI(false);
            villager.getInventory().setContents(p.getInventory().getContents());
            villager.setCustomName(p.getUniqueId().toString());
            user.setLoggerUUID(villager.getUniqueId());
        }
    }
}
