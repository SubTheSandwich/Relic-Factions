package me.sub.RelicFactions.Commands.User;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BalanceCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }
        User user = User.get(p);
        if (args.length == 1) {
            String name = args[0].toLowerCase();
            User other = User.get(name);
            if (other == null) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
                return true;
            }
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.balance.other")).replace("%player%", other.getName()).replace("%balance%", Main.getEconomy().format(other.getBalance().doubleValue()))));
        } else {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.balance.self")).replace("%balance%", Main.getEconomy().format(user.getBalance().doubleValue()))));
        }
        return true;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) return null;
        return new ArrayList<>();
    }
}
