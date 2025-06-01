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
import java.util.List;
import java.util.Objects;

public class SaleCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!Permission.has(sender, "sale", "admin")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.sale.usage"))));
            return true;
        }

        if (args[0].equalsIgnoreCase("stop")) {
            if (!ServerTimer.has("sale")) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.sale.not-running"))));
                return true;
            }
            ServerTimer.get("sale").cancel();
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.sale.stopped"))));
            return true;
        }
        if (args[0].equalsIgnoreCase("pause")) {
            if (!ServerTimer.has("sale")) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.sale.not-running"))));
                return true;
            }
            ServerTimer.get("sale").setPaused(!ServerTimer.get("sale").isPaused());
            String message = ServerTimer.get("sale").isPaused() ? Locale.get().getString("commands.sale.paused") : Locale.get().getString("commands.sale.unpaused");
            sender.sendMessage(C.chat(Objects.requireNonNull(message)));
            return true;
        }

        if (ServerTimer.has("sale")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.sale.running"))));
            return true;
        }

        try {
            double ignored = Double.parseDouble(args[0]);
        } catch (NumberFormatException ignored) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.sale.usage"))));
            return true;
        }
        double duration = Double.parseDouble(args[0]);
        ServerTimer timer = new ServerTimer("sale", BigDecimal.valueOf(duration), false);
        sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.sale.started"))));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!Permission.has(sender, "sale", "admin")) {
            return List.of();
        }
        if (args.length == 1) return List.of("pause", "stop");
        return List.of();
    }
}
