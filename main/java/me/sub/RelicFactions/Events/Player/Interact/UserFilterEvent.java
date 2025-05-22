package me.sub.RelicFactions.Events.Player.Interact;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.Filter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class UserFilterEvent implements Listener {

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        User user = User.get(p);
        Filter filter = user.getFilter();
        if (!filter.isEnabled()) return;
        Material material = e.getItem().getItemStack().getType();
        if (filter.getItems().contains(material)) e.setCancelled(true);
    }
}
