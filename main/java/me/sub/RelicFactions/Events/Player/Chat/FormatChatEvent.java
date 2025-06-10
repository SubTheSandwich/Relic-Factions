package me.sub.RelicFactions.Events.Player.Chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.PlayerTimer;
import me.sub.RelicFactions.Files.Enums.ChatType;
import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class FormatChatEvent implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        String message = C.serialize(e.message());
        Set<Player> recipients = e.viewers().stream()
                .filter(audience -> audience instanceof Player)
                .map(audience -> (Player) audience)
                .collect(Collectors.toSet());

        // Staff chat
        if (user.isStaffChat()) {
            e.setCancelled(true);
            for (Player player : Main.getOnlineStaff()) {
                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.staffchat.message"))
                        .replace("%player%", p.getName())
                        .replace("%message%", message)));
            }
            return;
        }

        // Public chat with ! prefix
        if (message.startsWith("!")) {
            e.setCancelled(true);
            String finalMessage = message.substring(1);
            if (finalMessage.isEmpty()) return;
            if (Main.getInstance().getChat().isMuted()) {
                if (!p.hasPermission("relic.bypass.chat")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.chat.chat-muted"))));
                    return;
                }
            }
            if (Main.getInstance().getChat().getSlowMode() != 0) {
                if (!p.hasPermission("relic.bypass.chat")) {
                    if (user.hasTimer("chat")) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.chat.chat-slowed")).replace("%time%", Timer.getMessageFormat(user.getTimer("chat").getDuration()))));
                        return;
                    } else {
                        PlayerTimer timer = new PlayerTimer(p.getUniqueId(), Timer.CHAT, BigDecimal.valueOf(Main.getInstance().getChat().getSlowMode()));
                        user.addTimer(timer);
                    }
                }
            }
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                for (Player recipient : recipients) {
                    if (!User.get(recipient).isGlobalChat()) continue;
                    if (user.hasFaction()) {
                        Faction faction = Faction.get(user.getFaction());
                        recipient.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("chat.public.faction"))
                                .replace("%faction%", faction.getValidName(recipient, false))
                                .replace("%player%", C.serialize(p.displayName()))
                                .replace("%message%", finalMessage)));
                    } else {
                        recipient.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("chat.public.no-faction"))
                                .replace("%player%", C.serialize(p.displayName()))
                                .replace("%message%", finalMessage)));
                    }
                }
            });
            return;
        }

        // Faction chat with @ prefix
        if (message.startsWith("@")) {
            e.setCancelled(true);
            String finalMessage = message.substring(1);
            if (finalMessage.isEmpty()) return;
            if (!user.hasFaction()) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                return;
            }
            Faction faction = Faction.get(user.getFaction());
            for (Player player : faction.getOnlineMembers()) {
                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("chat.faction"))
                        .replace("%player%", p.getName())
                        .replace("%message%", finalMessage)));
            }
            return;
        }

        // ChatType logic
        if (user.getChatType().equals(ChatType.PUBLIC)) {
            e.setCancelled(true);
            if (Main.getInstance().getChat().isMuted()) {
                if (!p.hasPermission("relic.bypass.chat")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.chat.chat-muted"))));
                    return;
                }
            }
            if (Main.getInstance().getChat().getSlowMode() != 0) {
                if (!p.hasPermission("relic.bypass.chat")) {
                    if (user.hasTimer("chat")) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.chat.chat-slowed")).replace("%time%", Timer.getMessageFormat(user.getTimer("chat").getDuration()))));
                        return;
                    } else {
                        PlayerTimer timer = new PlayerTimer(p.getUniqueId(), Timer.CHAT, BigDecimal.valueOf(Main.getInstance().getChat().getSlowMode()));
                        user.addTimer(timer);
                    }
                }
            }
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                for (Player recipient : recipients) {
                    if (!User.get(recipient).isGlobalChat()) continue;
                    if (user.hasFaction()) {
                        Faction faction = Faction.get(user.getFaction());
                        recipient.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("chat.public.faction"))
                                .replace("%faction%", faction.getValidName(recipient, false))
                                .replace("%player%", C.serialize(p.displayName()))
                                .replace("%message%", message)));
                    } else {
                        recipient.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("chat.public.no-faction"))
                                .replace("%player%", C.serialize(p.displayName()))
                                .replace("%message%", message)));
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
                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("chat.faction"))
                        .replace("%player%", p.getName())
                        .replace("%message%", message)));
            }
        } else if (user.getChatType().equals(ChatType.ALLY)) {
            e.setCancelled(true);
            if (!user.hasFaction()) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                user.setChatType(ChatType.PUBLIC);
                return;
            }
            Faction faction = Faction.get(user.getFaction());
            for (Player player : faction.getOnlineMembers()) {
                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("chat.ally"))
                        .replace("%player%", p.getName())
                        .replace("%message%", message)));
            }
            for (UUID uuid : faction.getAllies()) {
                Faction ally = Faction.get(uuid);
                if (ally == null) continue;
                for (Player player : ally.getOnlineMembers()) {
                    player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("chat.ally"))
                            .replace("%player%", p.getName())
                            .replace("%message%", message)));
                }
            }
        }
    }
}
