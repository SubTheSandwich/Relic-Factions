package me.sub.RelicFactions.Commands.Admin;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.ServerTimer;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
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

public class SOTWCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        if (args.length == 1 && args[0].equalsIgnoreCase("enable")) {

            if (!(sender instanceof Player p)) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
                return true;
            }
            User user = User.get(p);
            if (!ServerTimer.has("sotw")) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.sotw.not-running"))));
                return true;
            }
            if (Main.getInstance().sotwEnabled.contains(p.getUniqueId()) && user.hasTimer("combat")) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.command.combat"))));
                return true;
            }
            if (Main.getInstance().sotwEnabled.contains(p.getUniqueId())) {
                Main.getInstance().sotwEnabled.remove(p.getUniqueId());
            } else {
                Main.getInstance().sotwEnabled.add(p.getUniqueId());
            }

            p.sendMessage(C.chat(Objects.requireNonNull(Main.getInstance().sotwEnabled.contains(p.getUniqueId()) ? Locale.get().getString("commands.sotw.pvp-enabled") : Locale.get().getString("commands.sotw.pvp-disabled"))));
            return true;
        }

        if (!Permission.has(sender, "sotw", "admin")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }

        if (args.length == 0 || args.length > 2) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.sotw.usage"))));
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("pause")) {
                if (!ServerTimer.has("sotw")) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.sotw.not-running"))));
                    return true;
                }

                ServerTimer sotw = ServerTimer.get("sotw");
                sotw.setPaused(!sotw.isPaused());

                sender.sendMessage(C.chat(Objects.requireNonNull(sotw.isPaused() ? Locale.get().getString("commands.sotw.paused") : Locale.get().getString("commands.sotw.unpaused"))));
                return true;
            }
            if (args[0].equalsIgnoreCase("stop")) {
                if (!ServerTimer.has("sotw")) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.sotw.not-running"))));
                    return true;
                }
                ServerTimer sotw = ServerTimer.get("sotw");
                sotw.cancel();
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.sotw.stopped"))));
                return true;
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("extend")) {
                try {
                    int ignored = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                    return true;
                }
                int time = Integer.parseInt(args[1]);
                if (!ServerTimer.has("sotw")) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.sotw.not-running"))));
                    return true;
                }
                ServerTimer sotw = ServerTimer.get("sotw");
                sotw.setDuration(sotw.getDuration().add(BigDecimal.valueOf(time)));
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.sotw.extended"))));
                return true;
            }
            if (args[0].equalsIgnoreCase("start")) {
                try {
                    int ignored = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                    return true;
                }
                int time = Integer.parseInt(args[1]);
                ServerTimer sotw = ServerTimer.get("sotw");
                if (sotw != null) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.sotw.running"))));
                    return true;
                }
                sotw = new ServerTimer("sotw", BigDecimal.valueOf(time), false);
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.sotw.started"))));
                return true;
            }
        }
        sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.sotw.usage"))));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        ArrayList<String> values = new ArrayList<>();
        if (args.length == 1) {
            values.add("enable");
            if (Permission.has(sender, "sotw", "admin")) values.addAll(List.of("stop", "pause", "start", "extend"));
        }
        return values;
    }
}
