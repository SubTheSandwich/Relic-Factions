package me.sub.RelicFactions.Events.Player.Server;

import me.sub.RelicFactions.Files.Classes.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class UserDisconnectEvent implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        user.setDisconnected(true);
    }
}
