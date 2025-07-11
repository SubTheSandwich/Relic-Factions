package me.sub.RelicFactions.Commands.User;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MessageCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {

        if (!Main.getInstance().getConfig().getBoolean("features.msg")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.feature-disabled"))));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.message.usage")), s));
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
            return true;
        }

        if (sender.getName().equalsIgnoreCase(player.getName())) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.message.self"))));
            return true;
        }
        String message = C.strip(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        message = C.strip(message);
        if (message.isEmpty()) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.message.usage")), s));
            return true;
        }

        User other = User.get(player);
        if (!other.isMessages()) {
            if (!Permission.has(sender, "message.bypass", "admin")) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.message.disabled")).replace("%player%", player.getName())));
            }
            return true;
        }

        if (sender instanceof Player p) {
            User user = User.get(p);
            if (!user.isMessages()) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.message.self-disabled"))));
                return true;
            }
            user.setLastMessaged(player.getUniqueId());
            other.setLastMessaged(p.getUniqueId());
            if (user.isMessageSounds()) {
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            }
            if (other.isMessageSounds()) {
                player.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            }
        }
        sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.message.to")).replace("%player%", player.getName()).replace("%message%", message)));
        player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.message.from")).replace("%player%", sender.getName()).replace("%message%", message)));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) return List.of();
        if (args.length == 1) {
            return null;
        }
        return List.of();
    }
}
