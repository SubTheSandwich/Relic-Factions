package me.sub.RelicFactions.Commands.Admin;

import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class GamemodeCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {

        if (!Permission.has(sender, "gamemode", "admin")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
                return true;
            }
            if (!Permission.has(p, "gamemode", "admin")) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                return true;
            }
            if (s.equalsIgnoreCase("gmc")) {
                if (!Permission.has(sender, "gamemode.creative", "admin") && !p.hasPermission("relic.command.gamemode.*")) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                    return true;
                }
                p.setGameMode(GameMode.CREATIVE);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.gamemode.switched")).replace("%gamemode%", "Creative")));
                return true;
            }
            if (s.equalsIgnoreCase("gmsp")) {
                if (!Permission.has(sender, "gamemode.spectator", "admin") && !p.hasPermission("relic.command.gamemode.*")) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                    return true;
                }
                p.setGameMode(GameMode.SPECTATOR);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.gamemode.switched")).replace("%gamemode%", "Spectator")));
                return true;
            }
            if (s.equalsIgnoreCase("gma")) {
                if (!Permission.has(sender, "gamemode.adventure", "admin") && !p.hasPermission("relic.command.gamemode.*")) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                    return true;
                }
                p.setGameMode(GameMode.ADVENTURE);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.gamemode.switched")).replace("%gamemode%", "Adventure")));
                return true;
            }
            if (s.equalsIgnoreCase("gms")) {
                if (!Permission.has(sender, "gamemode.survival", "admin") && !p.hasPermission("relic.command.gamemode.*")) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                    return true;
                }
                p.setGameMode(GameMode.SURVIVAL);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.gamemode.switched")).replace("%gamemode%", "Survival")));
                return true;
            }
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.gamemode.usage")), s));
            return true;
        }

        boolean b = s.equalsIgnoreCase("gma") || s.equalsIgnoreCase("gms") || s.equalsIgnoreCase("gmc") || s.equalsIgnoreCase("gmsp");
        if (args.length == 1) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
                return true;
            }
            if (!Permission.has(p, "gamemode", "admin")) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                return true;
            }
            if (b) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.gamemode.usage")), "gamemode"));
                return true;
            }
            if (args[0].equalsIgnoreCase("creative") || args[0].equalsIgnoreCase("c")) {
                if (!Permission.has(sender, "gamemode.creative", "admin") && !p.hasPermission("relic.command.gamemode.*")) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                    return true;
                }
                p.setGameMode(GameMode.CREATIVE);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.gamemode.switched")).replace("%gamemode%", "Creative")));
                return true;
            }
            if (args[0].equalsIgnoreCase("spectator") || args[0].equalsIgnoreCase("sp")) {
                if (!Permission.has(sender, "gamemode.spectator", "admin") && !p.hasPermission("relic.command.gamemode.*")) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                    return true;
                }
                p.setGameMode(GameMode.SPECTATOR);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.gamemode.switched")).replace("%gamemode%", "Spectator")));
                return true;
            }
            if (args[0].equalsIgnoreCase("adventure") || args[0].equalsIgnoreCase("a")) {
                if (!Permission.has(sender, "gamemode.adventure", "admin") && !p.hasPermission("relic.command.gamemode.*")) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                    return true;
                }
                p.setGameMode(GameMode.ADVENTURE);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.gamemode.switched")).replace("%gamemode%", "Adventure")));
                return true;
            }
            if (args[0].equalsIgnoreCase("survival") || args[0].equalsIgnoreCase("s")) {
                if (!Permission.has(sender, "gamemode.survival", "admin") && !p.hasPermission("relic.command.gamemode.*")) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                    return true;
                }
                p.setGameMode(GameMode.SURVIVAL);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.gamemode.switched")).replace("%gamemode%", "Survival")));
                return true;
            }
        }

        if (args.length == 2) {
            if (!Permission.has(sender, "gamemode", "admin")) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                return true;
            }
            if (b) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.gamemode.usage")), "gamemode"));
                return true;
            }
            Player p = Bukkit.getPlayer(args[1]);
            if (p == null) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-player"))));
                return true;
            }
            if (args[0].equalsIgnoreCase("creative") || args[0].equalsIgnoreCase("c")) {
                if (!Permission.has(sender, "gamemode.others", "admin") && !Permission.has(sender, "gamemode.creative", "admin") && !sender.hasPermission("relic.command.gamemode.*")) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                    return true;
                }
                p.setGameMode(GameMode.CREATIVE);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.gamemode.for-other")).replace("%player%", sender.getName()).replace("%gamemode%", "Creative")));
                sender.sendMessage(C.chat(Objects.requireNonNull(Objects.requireNonNull(Locale.get().getString("commands.gamemode.other")).replace("%player%", p.getName()).replace("%gamemode%", "Creative"))));
                return true;
            }
            if (args[0].equalsIgnoreCase("spectator") || args[0].equalsIgnoreCase("sp")) {
                if (!Permission.has(sender, "gamemode.others", "admin") && !Permission.has(sender, "gamemode.spectator", "admin") && !p.hasPermission("relic.command.gamemode.*")) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                    return true;
                }
                p.setGameMode(GameMode.SPECTATOR);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.gamemode.for-other")).replace("%player%", sender.getName()).replace("%gamemode%", "Spectator")));
                sender.sendMessage(C.chat(Objects.requireNonNull(Objects.requireNonNull(Locale.get().getString("commands.gamemode.other")).replace("%player%", p.getName()).replace("%gamemode%", "Spectator"))));
                return true;
            }
            if (args[0].equalsIgnoreCase("adventure") || args[0].equalsIgnoreCase("a")) {
                if (!Permission.has(sender, "gamemode.others", "admin") && !Permission.has(sender, "gamemode.adventure", "admin") && !p.hasPermission("relic.command.gamemode.*")) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                    return true;
                }
                p.setGameMode(GameMode.ADVENTURE);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.gamemode.for-other")).replace("%player%", sender.getName()).replace("%gamemode%", "Adventure")));
                sender.sendMessage(C.chat(Objects.requireNonNull(Objects.requireNonNull(Locale.get().getString("commands.gamemode.other")).replace("%player%", p.getName()).replace("%gamemode%", "Adventure"))));
                return true;
            }
            if (args[0].equalsIgnoreCase("survival") || args[0].equalsIgnoreCase("s")) {
                if (!Permission.has(sender, "gamemode.others", "admin") && !Permission.has(sender, "gamemode.survival", "admin") && !p.hasPermission("relic.command.gamemode.*")) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                    return true;
                }
                p.setGameMode(GameMode.SURVIVAL);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.gamemode.for-other")).replace("%player%", sender.getName()).replace("%gamemode%", "Survival")));
                sender.sendMessage(C.chat(Objects.requireNonNull(Objects.requireNonNull(Locale.get().getString("commands.gamemode.other")).replace("%player%", p.getName()).replace("%gamemode%", "Survival"))));
                return true;
            }
        }

        if (!Permission.has(sender, "gamemode", "admin")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }
        if (b) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.gamemode.usage")), "gamemode"));
            return true;
        }
        sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.gamemode.usage")), s));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String s,
            @NotNull String @NotNull [] args
    ) {
        // List of shortcut commands that should not return tab completions
        boolean b = s.equalsIgnoreCase("gma") ||
                s.equalsIgnoreCase("gms") ||
                s.equalsIgnoreCase("gmc") ||
                s.equalsIgnoreCase("gmsp");
        if (b) {
            return List.of(); // Return nothing for these commands
        }

        // First argument: gamemode
        if (args.length == 1) {
            List<String> modes = new java.util.ArrayList<>();
            if (Permission.has(sender, "gamemode.creative", "admin") ||
                    sender.hasPermission("relic.command.gamemode.*")) {
                modes.add("creative");
                modes.add("c");
            }
            if (Permission.has(sender, "gamemode.survival", "admin") ||
                    sender.hasPermission("relic.command.gamemode.*")) {
                modes.add("survival");
                modes.add("s");
            }
            if (Permission.has(sender, "gamemode.adventure", "admin") ||
                    sender.hasPermission("relic.command.gamemode.*")) {
                modes.add("adventure");
                modes.add("a");
            }
            if (Permission.has(sender, "gamemode.spectator", "admin") ||
                    sender.hasPermission("relic.command.gamemode.*")) {
                modes.add("spectator");
                modes.add("sp");
            }
            String current = args[0].toLowerCase();
            return modes.stream()
                    .filter(mode -> mode.startsWith(current))
                    .toList();
        }

        // Second argument: player name
        if (args.length == 2) {
            if (Permission.has(sender, "gamemode.others", "admin") ||
                    sender.hasPermission("relic.command.gamemode.*")) {
                return null;
            }
        }

        return List.of();
    }
}
