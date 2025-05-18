package me.sub.RelicFactions.Events.Player.Chat;

import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Set;

public class FormatChatEvent implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        e.setCancelled(true);
        Player p = e.getPlayer();
        User user = User.get(p);
        String message = e.getMessage();
        message = C.strip(message);
        Set<Player> recipients = new HashSet<>(e.getRecipients());
        String finalMessage = message;
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            for (Player recipient : recipients) {
                if (user.hasFaction()) {
                    Faction faction = Faction.get(user.getFaction());
                    recipient.sendMessage(C.chat(Main.getInstance().getConfig().getString("chat.format.faction").replace("%faction%", faction.getValidName(recipient)).replace("%player%", p.getDisplayName()).replace("%message%", finalMessage)));
                } else {
                    recipient.sendMessage(C.chat(Main.getInstance().getConfig().getString("chat.format.no-faction").replace("%player%", p.getDisplayName()).replace("%message%", finalMessage)));
                }
            }
        });
    }
}
