package me.sub.RelicFactions.Commands.User;

import me.sub.RelicFactions.Commands.Staff.PanicCommand;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class PayCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }
        if (args.length != 2) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.pay.usage"))));
            return true;
        }
        User paid = User.get(args[0]);
        if (paid == null) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
            return true;
        }

        if (paid.getUUID().equals(p.getUniqueId())) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.pay.self"))));
            return true;
        }

        try {
            double ignored = Double.parseDouble(args[1]);
        } catch (NumberFormatException ignored) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
            return true;
        }

        double amount = Double.parseDouble(args[1]);
        if (amount <= 0.01) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
            return true;
        }

        if (!Main.getEconomy().has(p, amount)) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.pay.not-enough"))));
            return true;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(paid.getUUID());
        Main.getEconomy().depositPlayer(player, amount);
        Main.getEconomy().withdrawPlayer(p, amount);
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.pay.sent")).replace("%amount%", Main.getEconomy().format(amount)).replace("%player%", paid.getName())));
        if (player.isOnline()) {
            Player pay = player.getPlayer();
            Objects.requireNonNull(pay).sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.pay.received")).replace("%amount%", Main.getEconomy().format(amount)).replace("%player%", p.getName())));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String s,
            @NotNull String[] args
    ) {
        if (!(sender instanceof Player player)) return List.of();
        if (args.length == 1) {
            return null;
        }
        return List.of();
    }
}
