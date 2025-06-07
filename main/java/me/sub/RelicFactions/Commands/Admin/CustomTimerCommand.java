package me.sub.RelicFactions.Commands.Admin;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.CustomTimer;
import me.sub.RelicFactions.Files.Enums.Timer;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CustomTimerCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!Permission.has(sender, "customtimer", "admin")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("remove")) {
                Player player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
                    return true;
                }
                User user = User.get(player);
                for (String customTimer : user.getCustomTimers().keySet()) {
                    user.removeCustomTimer(customTimer);
                }
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.customtimer.removed-all")).replace("%player%", player.getName())));
                return true;
            }
        }
        if (args.length != 3 && args.length != 4) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.customtimer.usage")), s));
            return true;
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("pause")) {
                Player player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
                    return true;
                }
                User user = User.get(player);
                String name = args[2];
                if (!user.hasCustomTimer(name)) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.customtimer.not-found"))));
                    return true;
                }
                user.getCustomTimer(name).setPaused(!user.getCustomTimer(name).isPaused());
                String message = user.getCustomTimer(name).isPaused() ? Locale.get().getString("commands.customtimer.paused") : Locale.get().getString("commands.customtimer.unpaused");
                sender.sendMessage(C.chat(Objects.requireNonNull(message)).replace("%timer%", name));
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("add")) {
            if (args.length != 4) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.customtimer.usage")), s));
                return true;
            }
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
                return true;
            }
            String name = args[2];
            try {
                double ignored = Double.parseDouble(args[3]);
            } catch (NumberFormatException ignored) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.customtimer.usage")), s));
                return true;
            }
            double duration = Double.parseDouble(args[3]);
            User user = User.get(player);
            if (user.hasCustomTimer(name)) {
                CustomTimer timer = user.getCustomTimer(name);
                timer.setDuration(timer.getDuration().add(BigDecimal.valueOf(duration)));
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.customtimer.extended")).replace("%player%", player.getName()).replace("%timer%", name).replace("%duration%", Timer.format(BigDecimal.valueOf(duration)))));
                return true;
            }
            CustomTimer timer = new CustomTimer(player.getUniqueId(), name, BigDecimal.valueOf(duration));
            user.addCustomTimer(timer);
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.customtimer.started")).replace("%player%", player.getName()).replace("%timer%", name).replace("%duration%", Timer.format(BigDecimal.valueOf(duration)))));
            return true;
        }
        if (args[0].equalsIgnoreCase("remove")) {
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
                return true;
            }
            User user = User.get(player);
            String name = args[2];
            if (!user.hasCustomTimer(name)) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.customtimer.not-found"))));
                return true;
            }
            if (args.length != 4) {
                user.removeCustomTimer(name);
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.customtimer.removed")).replace("%player%", player.getName()).replace("%timer%", name)));
                return true;
            }
            try {
                double ignored = Double.parseDouble(args[3]);
            } catch (NumberFormatException ignored) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.customtimer.usage")), s));
                return true;
            }
            double duration = Double.parseDouble(args[3]);
            CustomTimer timer = user.getCustomTimer(name);
            if (duration < 0 || timer.getDuration().doubleValue() < duration) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                return true;
            }
            timer.setDuration(timer.getDuration().subtract(BigDecimal.valueOf(duration)));
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.customtimer.removed-time")).replace("%duration%", Timer.format(BigDecimal.valueOf(duration))).replace("%player%", player.getName()).replace("%timer%", name)));
            return true;
        }
        sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.customtimer.usage")), s));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!Permission.has(sender, "customtimer", "admin")) {
            return List.of();
        }
        if (args.length == 1) return List.of("add", "remove", "pause");
        if (args.length == 2) return null;
        if (args.length == 3) {
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) return List.of();
            User user = User.get(player);
            return user.getCustomTimers().values().stream()
                    .map(CustomTimer::getName)
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
