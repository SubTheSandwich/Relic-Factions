package me.sub.RelicFactions.Events.Player.Armor;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import me.sub.RelicFactions.Files.Classes.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HCFClassEvent implements Listener {

    @EventHandler
    public void onArmor(PlayerArmorChangeEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        user.updateClass();
    }
}
