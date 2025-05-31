package me.sub.RelicFactions.Commands.Staff;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Files.Normal.Messages;
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

import java.util.List;
import java.util.Objects;

public class FreezeCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!Permission.has(sender, "freeze", "staff")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.freeze.usage"))));
            return true;
        }

        if (args[0].equalsIgnoreCase("all")) {
            if (!Permission.has(sender, "freeze.all", "admin")) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                return true;
            }
            Main.getInstance().setServerFrozen(!Main.getInstance().isServerFrozen());
            String message = Main.getInstance().isServerFrozen() ? Locale.get().getString("commands.freeze.server-frozen") : Locale.get().getString("commands.freeze.server-unfrozen");
            Bukkit.broadcastMessage(C.chat(Objects.requireNonNull(message)));
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.freeze.usage"))));
            return true;
        }

        User user = User.get(player);
        if (user.isPanic()) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.cannot-do")).replace("%player%", player.getName())));
            return true;
        }
        user.setFrozen(!user.isFrozen());
        String message = user.isFrozen() ? Locale.get().getString("commands.freeze.froze-player") : Locale.get().getString("commands.freeze.unfroze-player");
        if (!user.isFrozen()) {
            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.freeze.unfrozen"))));
        } else {
            new BukkitRunnable() {
                int ticks = 100;
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        for (Player staff : Main.getOnlineStaff()) {
                            staff.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.frozen.disconnect")).replace("%player%", user.getName())));
                            cancel();
                            return;
                        }
                    }
                    if (!user.isFrozen()) {
                        cancel();
                        return;
                    }
                    ticks++;
                    if (ticks >= 100) {
                        Messages.send(player, "staff.frozen");
                        ticks = 0;
                    }
                }
            }.runTaskTimer(Main.getInstance(), 0, 1);
        }
        message = Objects.requireNonNull(message).replace("%player%", user.getName());
        sender.sendMessage(C.chat(message));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String s,
            @NotNull String[] args
    ) {
        if (!Permission.has(sender, "freeze", "staff")) return List.of();

        // Only tab complete the first argument
        if (args.length == 1) {
            List<String> completions = new java.util.ArrayList<>();
            String current = args[0].toLowerCase();
            // Add "all" if it matches
            if ("all".startsWith(current)) {
                completions.add("all");
            }
            // Add online player names that match
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(current)) {
                    completions.add(player.getName());
                }
            }
            return completions;
        }

        return List.of();
    }

}
