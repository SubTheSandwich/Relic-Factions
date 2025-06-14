package me.sub.RelicFactions.Events.Player.Commands;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Objects;

public class BlockCommandEvent implements Listener {

    @EventHandler
    public void onFreezeCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);

        String fullCommand = e.getMessage();
        String[] args = fullCommand.substring(1).split(" ");

        String command = args[0].toLowerCase();
        if (!user.isFrozen()) return;
        if (Main.getInstance().getConfig().getStringList("commands.freeze-allowed").contains(command)) return;
        e.setCancelled(true);
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.freeze.blocked"))));
    }
}
