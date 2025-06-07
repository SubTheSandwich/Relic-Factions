package me.sub.RelicFactions.Commands.Admin;

import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Files.Normal.Locations;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class EndCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }

        if (!Permission.has(sender, "end", "admin")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }
        if (args.length != 1) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.end.usage"))));
            return true;
        }

        Locations locations = new Locations();

        if (args[0].equalsIgnoreCase("setspawn")) {
            if (!p.getWorld().getEnvironment().equals(World.Environment.THE_END)) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.end.not-end"))));
                return true;
            }
            locations.get().set("end.spawn", p.getLocation());
            locations.save();
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.end.spawn"))));
            return true;
        }
        if (args[0].equalsIgnoreCase("setexit")) {
            if (!p.getWorld().getEnvironment().equals(World.Environment.NORMAL) && !p.getWorld().getEnvironment().equals(World.Environment.CUSTOM)) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.end.not-overworld"))));
                return true;
            }
            locations.get().set("end.exit", p.getLocation());
            locations.save();
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.end.exit"))));
            return true;
        }
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.end.usage"))));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            return List.of();
        }

        if (!Permission.has(sender, "clearinventory", "admin")) {
            return List.of();
        }

        if (args.length == 1) return List.of("setspawn", "setexit");

        return List.of();
    }
}
