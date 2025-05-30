package me.sub.RelicFactions.Commands.User;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Normal.Locale;
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

public class ReplyCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }

        if (args.length < 1) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.reply.usage")), s));
            return true;
        }

        User user = User.get(p);
        if (user.getLastMessaged() == null) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.reply.none"))));
            return true;
        }

        Player player = Bukkit.getPlayer(user.getLastMessaged());
        if (player == null) {
            user.setLastMessaged(null);
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.reply.offline"))));
            return true;
        }

        User other = User.get(player);
        if (!other.isMessages()) {
            if (!Permission.has(p, "message.bypass", "admin")) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.message.disabled")).replace("%player%", player.getName())));
            }
            return true;
        }

        if (!user.isMessages()) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.message.self-disabled"))));
            return true;
        }
        String message = C.strip(String.join(" ", Arrays.copyOfRange(args, 0, args.length)));
        message = C.strip(message);
        if (message.isEmpty()) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.reply.usage")), s));
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
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.message.to")).replace("%player%", player.getName()).replace("%message%", message)));
        player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.message.from")).replace("%player%", p.getName()).replace("%message%", message)));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of();
    }
}
