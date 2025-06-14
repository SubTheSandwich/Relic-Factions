package me.sub.RelicFactions.Commands.Staff;

import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class FlyCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }
        if (!Permission.has(p, "fly", "staff")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }
        if (args.length == 1) {
            if (!Permission.has(p, "fly.others", "admin")) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                return true;
            }
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
                return true;
            }
            player.setAllowFlight(!player.getAllowFlight());
            player.setFlying(player.isFlying());
            String message = player.getAllowFlight() ? Locale.get().getString("commands.fly.enabled-other") : Locale.get().getString("commands.fly.disabled-other");
            String play = player.getAllowFlight() ? Locale.get().getString("commands.fly.enabled-other-target") : Locale.get().getString("commands.fly.disabled-other-target");
            message = Objects.requireNonNull(message).replace("%player%", player.getName());
            play = Objects.requireNonNull(play).replace("%player%", p.getName());
            p.sendMessage(C.chat(message));
            player.sendMessage(C.chat(play));
            return true;
        }

        p.setAllowFlight(!p.getAllowFlight());
        p.setFlying(p.isFlying());
        String message = p.getAllowFlight() ? Locale.get().getString("commands.fly.enabled") : Locale.get().getString("commands.fly.disabled");
        p.sendMessage(C.chat(Objects.requireNonNull(message)));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) return List.of();
        if (args.length == 1) {
            if (Permission.has(sender, "fly.others", "admin")) {
                return null;
            }
        }
        return List.of();
    }
}
