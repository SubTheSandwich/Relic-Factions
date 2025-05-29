package me.sub.RelicFactions.Events.Player.Movement;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;

public class FreezeMovementEvent implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        if (e.getTo() == null) return;
        if (e.getFrom().getX() == e.getTo().getX() && e.getFrom().getZ() == e.getTo().getZ()) return;
        if (user.isFrozen() || user.isPanic()) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.freeze.cannot"))));
            e.setCancelled(true);
            return;
        }
        if (Main.getInstance().isServerFrozen()) {
            if (p.hasPermission("relic.bypass.freeze")) return;
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.freeze.cannot"))));
            e.setCancelled(true);
        }
    }
}
