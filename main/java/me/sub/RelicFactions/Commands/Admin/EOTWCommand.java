package me.sub.RelicFactions.Commands.Admin;

import me.sub.RelicFactions.Files.Classes.KOTH;
import me.sub.RelicFactions.Files.Data.ServerTimer;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import net.kyori.adventure.text.Component;
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

public class EOTWCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (sender instanceof Player) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-console"))));
            return true;
        }

        if (!Permission.has(sender, "end", "admin")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.eotw.usage"))));
            return true;
        }

        if (args[0].equalsIgnoreCase("stop")) {
            if (!ServerTimer.has("eotw") && !Main.getInstance().isEOTW()) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.eotw.not-running"))));
                return true;
            }
            if (ServerTimer.has("eotw")) {
                ServerTimer.remove("eotw");
            } else {
                Main.getInstance().setEOTW(false);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "koth " + Main.getInstance().getConfig().getString("eotw.koth") + " stop");
            }
            Bukkit.broadcast(Component.text(C.chat(Objects.requireNonNull(Locale.get().getString("commands.eotw.stopped")))));
            return true;
        }
        if (args[0].equalsIgnoreCase("start")) {
            if (ServerTimer.has("eotw") || Main.getInstance().isEOTW()) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.eotw.already-started"))));
                return true;
            }
            KOTH koth = KOTH.get(Objects.requireNonNull(Main.getInstance().getConfig().getString("eotw.koth")));
            if (koth == null) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.eotw.koth-not-right"))));
                return true;
            }
            if (!koth.isSetup()) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.eotw.koth-not-right"))));
                return true;
            }
            Bukkit.broadcast(Component.text(C.chat(Objects.requireNonNull(Locale.get().getString("commands.eotw.starting")).replace("%time%", Main.getInstance().getConfig().getInt("eotw.time") + ""))));
            new ServerTimer("EOTW", BigDecimal.valueOf(Main.getInstance().getConfig().getInt("eotw.time")).multiply(BigDecimal.valueOf(60)), false);
            return true;
        }

        if (args[0].equalsIgnoreCase("commence")) {
            if (!ServerTimer.has("eotw") || Main.getInstance().isEOTW()) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.eotw.not-starting"))));
                return true;
            }
            Bukkit.dispatchCommand(Main.getInstance().getServer().getConsoleSender(), "koth " + Main.getInstance().getConfig().getString("eotw.koth") + " start");
            Main.getInstance().setEOTW(true);
            ServerTimer.remove("eotw");
            Bukkit.broadcast(Component.text(C.chat(Objects.requireNonNull(Locale.get().getString("commands.eotw.commenced")))));
            return true;
        }
        sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.eotw.usage"))));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        return List.of();
    }
}
