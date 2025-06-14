package me.sub.RelicFactions.Commands.Staff;

import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class HealCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }
        if (!Permission.has(p, "heal", "staff")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }

        if (args.length == 1) {
            if (!Permission.has(p, "heal.others", "admin")) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                return true;
            }
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
                return true;
            }
            player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getDefaultValue());
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.heal.other")).replace("%player%", player.getName())));
            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.heal.target")).replace("%player%", p.getName())));
            return true;
        }
        p.setHealth(Objects.requireNonNull(p.getAttribute(Attribute.MAX_HEALTH)).getDefaultValue());
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.heal.success"))));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(commandSender instanceof Player)) return List.of();
        if (!Permission.has(commandSender, "heal", "staff")) return List.of();
        if (args.length == 1) {
            if (Permission.has(commandSender, "heal.others", "admin")) {
                return null;
            }
        }

        return List.of();
    }
}
