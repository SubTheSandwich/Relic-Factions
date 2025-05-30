package me.sub.RelicFactions.Commands.Staff;

import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class WorldCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }

        if (!Permission.has(p, "world", "admin")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }
        if (args.length != 1) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.world.usage"))));
            return true;
        }
        World world = Bukkit.getWorld(args[0]);
        if (world == null) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.world.usage"))));
            return true;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                p.teleport(new Location(world, p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ()));
            }
        }.runTaskLater(Main.getInstance(), 1);
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.world.success")).replace("%world%", world.getName())));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return List.of();
        }

        if (!Permission.has(sender, "world", "admin")) {
            return List.of();
        }
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> suggestions = new ArrayList<>();
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                String name = world.getName();
                if (name.toLowerCase().startsWith(input)) {
                    suggestions.add(name);
                }
            }
            Collections.sort(suggestions);
            return suggestions;
        }
        return List.of();
    }
}
