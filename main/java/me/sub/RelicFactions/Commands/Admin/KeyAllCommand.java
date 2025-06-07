package me.sub.RelicFactions.Commands.Admin;

import me.sub.RelicFactions.Files.Data.ServerTimer;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class KeyAllCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!Permission.has(sender, "keyall", "admin")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.key-all.usage"))));
            return true;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("pause")) {
                if (!ServerTimer.has("keyall")) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.key-all.not-running"))));
                    return true;
                }
                ServerTimer.get("keyall").setPaused(!ServerTimer.get("keyall").isPaused());
                String message = ServerTimer.get("keyall").isPaused() ? Locale.get().getString("commands.key-all.paused") : Locale.get().getString("commands.key-all.unpaused");
                sender.sendMessage(C.chat(Objects.requireNonNull(message)));
                return true;
            }
            if (args[0].equalsIgnoreCase("stop")) {
                if (!ServerTimer.has("keyall")) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.key-all.not-running"))));
                    return true;
                }
                ServerTimer.get("keyall").cancel();
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.key-all.stopped"))));
                Main.getInstance().setKeyAllCommand(null);
                return true;
            }
        }
        try {
            double ignored = Double.parseDouble(args[0]);
        } catch (NumberFormatException ignored) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.key-all.usage"))));
            return true;
        }
        double duration = Double.parseDouble(args[0]);
        ServerTimer ignored = new ServerTimer("keyall", BigDecimal.valueOf(duration), false);
        String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Main.getInstance().setKeyAllCommand(command);
        sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.key-all.started"))));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!Permission.has(sender, "keyall", "admin")) {
            return List.of();
        }
        if (args.length == 1) return List.of("pause", "stop");
        return List.of();
    }
}
