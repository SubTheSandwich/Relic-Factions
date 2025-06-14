package me.sub.RelicFactions.Commands.Admin;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Files.Normal.Messages;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EconomyCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!Permission.has(sender, "economy", "admin")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }
        if (args.length != 3) {
            Messages.send(sender, "help.economy", s);
            return true;
        }
        User user = User.get(args[1]);
        if (user == null) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
            return true;
        }
        try {
            double ignored = Double.parseDouble(args[2]);
        } catch (NumberFormatException ignored) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
            return true;
        }
        double amount = Double.parseDouble(args[2]);
        if (amount <= 0) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
            return true;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(user.getUUID());
        if (args[0].equalsIgnoreCase("set")) {
            Main.getEconomy().withdrawPlayer(player, user.getBalance().doubleValue());
            Main.getEconomy().depositPlayer(player, amount);
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.economy.set")).replace("%player%", user.getName()).replace("%amount%", Main.getEconomy().format(amount))));
            return true;
        }
        if (args[0].equalsIgnoreCase("give")) {
            Main.getEconomy().depositPlayer(player, amount);
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.economy.give")).replace("%player%", user.getName()).replace("%amount%", Main.getEconomy().format(amount))));
            return true;
        }
        if (args[0].equalsIgnoreCase("remove")) {
            Main.getEconomy().withdrawPlayer(player, amount);
            if (Main.getEconomy().getBalance(player) < 0) Main.getEconomy().depositPlayer(player, Math.abs(Main.getEconomy().getBalance(player)));
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.economy.remove")).replace("%player%", user.getName()).replace("%amount%", Main.getEconomy().format(amount))));
            return true;
        }
        Messages.send(sender, "help.economy", s);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!Permission.has(sender, "economy", "admin")) return new ArrayList<>();
        if (args.length == 1) return List.of("set", "give", "remove");
        if (args.length == 2) return null;
        return List.of();
    }
}
