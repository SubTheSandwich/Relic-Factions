package me.sub.RelicFactions.Commands.Admin;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Files.Normal.Messages;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LivesCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (args.length == 0 && sender instanceof Player p) {
            User user = User.get(p);
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.lives.check")).replace("%amount%", user.getLives() + "")));
            return true;
        }
        if (!(Permission.has(sender, "lives", "admin"))) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }
        if (args.length != 3) {
            Messages.send(sender, "help.lives");
            return true;
        }
        User user = User.get(args[1]);
        if (user == null) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
            return true;
        }
        try {
            int ignored = Integer.parseInt(args[2]);
        } catch (NumberFormatException ignored) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
            return true;
        }
        int amount = Integer.parseInt(args[2]);
        if (amount <= 0) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
            return true;
        }
        if (args[0].equalsIgnoreCase("set")) {
            user.setLives(amount);
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.lives.set")).replace("%player%", user.getName()).replace("%amount%", amount + "")));
            return true;
        }
        if (args[0].equalsIgnoreCase("add")) {
            user.setLives(user.getLives() + amount);
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.lives.add")).replace("%player%", user.getName()).replace("%amount%", amount + "")));
            return true;
        }
        if (args[0].equalsIgnoreCase("take")) {
            user.setLives(user.getLives() - amount);
            if (user.getLives() < 0) user.setLives(0);
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.lives.take")).replace("%player%", user.getName()).replace("%amount%", amount + "")));
            return true;
        }
        Messages.send(sender, "help.lives");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(Permission.has(sender, "lives", "admin"))) return new ArrayList<>();
        if (args.length == 1) return List.of("add", "set", "take");
        if (args.length == 2) return null;
        return List.of();
    }
}
