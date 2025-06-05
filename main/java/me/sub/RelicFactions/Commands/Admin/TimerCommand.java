package me.sub.RelicFactions.Commands.Admin;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.PlayerTimer;
import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class TimerCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }
        if (!Permission.has(p, "timer", "admin")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }
        if (args.length < 3 || args.length > 4) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.timer.usage"))));
            return true;
        }
        User user = User.get(args[1]);
        if (user == null) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
            return true;
        }
        if (!Timer.isValid(args[2])) {
            p.sendMessage(C.chat(Objects.requireNonNull(Objects.requireNonNull(Locale.get().getString("commands.timer.not-valid")).replace("%timers%", Timer.getValidNames()))));
            return true;
        }
        Timer timer = Timer.valueOf(args[2].toUpperCase());
        int duration = timer.getDuration();
        if (args.length == 4) {
            try {
                int ignored = Integer.parseInt(args[3]);
            } catch (NumberFormatException ignored) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                return true;
            }
            duration = Integer.parseInt(args[3]);
        }
        if (args[0].equalsIgnoreCase("set")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.timer.set")).replace("%player%", user.getName()).replace("%duration%", Timer.format(duration)).replace("%timer%", timer.name())));
            PlayerTimer playerTimer = new PlayerTimer(user.getUUID(), timer, BigDecimal.valueOf(duration));
            user.addTimer(playerTimer);
            if (timer.equals(Timer.STUCK)) {
                user.setStuckLocation(p.getLocation());
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("remove")) {
            if (args.length == 4) {
                if (user.hasTimer(timer.name())) {
                    user.getTimer(timer.name()).setDuration(user.getTimer(timer.name()).getDuration().subtract(BigDecimal.valueOf(duration)));
                }
            } else {
                user.removeTimer(timer.name());
            }
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.timer.remove")).replace("%player%", user.getName()).replace("%timer%", timer.name())));
            return true;
        }
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.timer.usage"))));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) return new ArrayList<>();
        if (!Permission.has(p, "timer", "admin")) return new ArrayList<>();
        if (args.length == 1) return List.of("set", "remove");
        if (args.length == 2) return null;
        if (args.length == 3) return Arrays.stream(Timer.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return new ArrayList<>();
    }
}
