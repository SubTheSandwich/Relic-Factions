package me.sub.RelicFactions.Commands.Admin;

import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TPHereCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }

        if (!Permission.has(sender, "teleport", "admin")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }
        if (args.length != 1) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.tphere.usage"))));
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(p.getLocation());
            }
        }.runTaskLater(Main.getInstance(), 1);
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.teleport.player.here")).replace("%player%", player.getName())));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            return List.of();
        }

        if (!Permission.has(sender, "teleport", "admin")) {
            return List.of();
        }
        ArrayList<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                suggestions.add(player.getName());
            }
            return suggestions;
        }
        return List.of();
    }
}
