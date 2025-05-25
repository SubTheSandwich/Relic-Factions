package me.sub.RelicFactions.Commands.User;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.PlayerTimer;
import me.sub.RelicFactions.Files.Enums.Timer;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PvPCommand implements TabExecutor {

    // TODO: Implement

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("enable")) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
                return true;
            }
            User user = User.get(p);
            if (!user.hasTimer("pvp") && !user.hasTimer("starting")) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.pvp.no-timer"))));
                return true;
            }
            if (user.hasTimer("pvp")) {
                user.removeTimer("pvp");
            } else {
                user.removeTimer("starting");
            }
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.pvp.enabled"))));
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("revive")) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
                return true;
            }
            User user = User.get(p);
            User revive = User.get(args[1]);
            if (revive == null) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-player"))));
                return true;
            }

            if (!revive.isDeathBanned()) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.lives.not-deathbanned"))));
                return true;
            }

            if (user.getLives() <= 0) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.lives.not-enough"))));
                return true;
            }
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.lives.revive")).replace("%player%", revive.getName())));
            user.setLives(user.getLives() - 1);
            revive.setDeathBanned(false);
            return true;
        }
        if (args.length < 2 || args.length > 3) {
            if (!Permission.has(sender, "pvp", "admin")) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.pvp.usage"))));
            } else {
                Messages.send(sender, "help.pvp");
            }
            return true;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                User user = User.get(args[1]);
                if (user == null) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-player"))));
                    return true;
                }
                PlayerTimer timer = new PlayerTimer(user.getUUID(), Timer.PVP);
                user.addTimer(timer);
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.pvp.given")).replace("%player%", user.getName())));
                return true;
            }
            Messages.send(sender, "help.pvp");
            return true;
        }
        if (args[0].equalsIgnoreCase("set")) {
            User user = User.get(args[1]);
            if (user == null) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-player"))));
                return true;
            }
            try {
                int ignored = Integer.parseInt(args[2]);
            } catch (NumberFormatException ignored) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                return true;
            }
            int amount = Integer.parseInt(args[2]);
            PlayerTimer timer = Optional.ofNullable(user.getTimer("pvp"))
                    .orElse(user.getTimer("starting"));
            if (timer == null) {
                PlayerTimer playerTimer = new PlayerTimer(user.getUUID(), Timer.PVP, BigDecimal.valueOf(amount));
                user.addTimer(playerTimer);
            } else {
                timer.setDuration(amount);
            }
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.pvp.set")).replace("%player%", user.getName()).replace("%time%", Timer.format(amount))));
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        List<String> values = new ArrayList<>();
        if (args.length == 1) {
            values.addAll(List.of("revive", "enable"));
            if (Permission.has(sender, "pvp", "admin"))  values.addAll(List.of("set", "give"));
        }
        if (args.length == 2) {
            if (Permission.has(sender, "pvp", "admin")) return null;
        }
        return values;
    }
}
