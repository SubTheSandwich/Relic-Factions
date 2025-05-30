package me.sub.RelicFactions.Events.Player.Chat;

import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Enums.ChatType;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Objects;
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
        if (user.isStaffChat()) {
            for (Player player : Main.getOnlineStaff()) {
                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.staffchat.message")).replace("%player%", p.getName()).replace("%message%", finalMessage)));
            }
            return;
        }
        if (finalMessage.startsWith("!")) {
            finalMessage = finalMessage.substring(1);
            if (finalMessage.isEmpty()) {
                e.setCancelled(true);
                return;
            }
            String finalMessage1 = finalMessage;
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                for (Player recipient : recipients) {
                    if (!User.get(recipient).isGlobalChat()) continue;
                    if (user.hasFaction()) {
                        Faction faction = Faction.get(user.getFaction());
                        recipient.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("chat.public.faction")).replace("%faction%", faction.getValidName(recipient, false)).replace("%player%", p.getDisplayName()).replace("%message%", finalMessage1)));
                    } else {
                        recipient.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("chat.public.no-faction")).replace("%player%", p.getDisplayName()).replace("%message%", finalMessage1)));
                    }
                }
            });
            return;
        }
        if (finalMessage.startsWith("@")) {
            finalMessage = finalMessage.substring(1);
            e.setCancelled(true);
            if (finalMessage.isEmpty()) {
                return;
            }
            if (!user.hasFaction()) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                return;
            }
            Faction faction = Faction.get(user.getFaction());
            for (Player player : faction.getOnlineMembers()) {
                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("chat.faction")).replace("%player%", p.getName()).replace("%message%", finalMessage)));
            }
            return;
        }
        if (user.getChatType().equals(ChatType.PUBLIC)) {
            String finalMessage2 = finalMessage;
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                for (Player recipient : recipients) {
                    if (!User.get(recipient).isGlobalChat()) continue;
                    if (user.hasFaction()) {
                        Faction faction = Faction.get(user.getFaction());
                        recipient.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("chat.public.faction")).replace("%faction%", faction.getValidName(recipient, false)).replace("%player%", p.getDisplayName()).replace("%message%", finalMessage2)));
                    } else {
                        recipient.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("chat.public.no-faction")).replace("%player%", p.getDisplayName()).replace("%message%", finalMessage2)));
                    }
                }
            });
        } else if (user.getChatType().equals(ChatType.FACTION)) {
            e.setCancelled(true);
            if (!user.hasFaction()) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                user.setChatType(ChatType.PUBLIC);
                return;
            }
            Faction faction = Faction.get(user.getFaction());
            for (Player player : faction.getOnlineMembers()) {
                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("chat.faction")).replace("%player%", p.getName()).replace("%message%", finalMessage)));
            }
        }
    }
}
