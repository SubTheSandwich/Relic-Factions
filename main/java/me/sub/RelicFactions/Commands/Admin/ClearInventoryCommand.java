package me.sub.RelicFactions.Commands.Admin;

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

public class ClearInventoryCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }

        if (!Permission.has(sender, "clearinventory", "admin")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }

        if (args.length > 1) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.clearinventory.usage")), s));
            return true;
        }

        if (args.length == 1) {
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
                return true;
            }
            player.getInventory().clear();
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.clearinventory.other")).replace("%player%", player.getName())));
            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.clearinventory.target")).replace("%player%", p.getName())));
            return true;
        }
        p.getInventory().clear();
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.clearinventory.success"))));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            return List.of();
        }

        if (!Permission.has(sender, "clearinventory", "admin")) {
            return List.of();
        }
        if (args.length == 1) return null;
        return List.of();
    }
}
