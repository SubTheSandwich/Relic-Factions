package me.sub.RelicFactions.Commands.Admin;

import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Calculate;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class TeleportCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        if (!Permission.has(sender, "teleport", "admin")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }

        if (args.length == 0 || args.length > 4) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.teleport.usage")), s));
            return true;
        }

        if (args.length == 1) {

            if (!(sender instanceof Player p)) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
                return true;
            }

            if (args[0].equalsIgnoreCase("top")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.teleport(p.getWorld().getHighestBlockAt(p.getLocation()).getLocation().add(0,1,0));
                    }
                }.runTaskLater(Main.getInstance(), 1);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.teleport.top"))));
                return true;
            }

            if (args[0].equalsIgnoreCase("all")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getUniqueId().equals(p.getUniqueId())) continue;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.teleport(p.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                        }
                    }.runTaskLater(Main.getInstance(), 1);
                }
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.teleport.all"))));
                return true;
            }
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
                return true;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    p.teleport(player.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                }
            }.runTaskLater(Main.getInstance(), 1);
            p.sendMessage(C.chat(Objects.requireNonNull(Objects.requireNonNull(Locale.get().getString("commands.teleport.player.online")).replace("%player%", player.getName()))));
            return true;
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("here")) {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
                    return true;
                }
                Player player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
                    return true;
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.teleport(p.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                    }
                }.runTaskLater(Main.getInstance(), 1);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.teleport.player.here")).replace("%player%", player.getName())));
                return true;
            }
            Player from = Bukkit.getPlayer(args[0]);
            if (from == null) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
                return true;
            }

            Player to = Bukkit.getPlayer(args[1]);
            if (to == null) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
                return true;
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    from.teleport(to.getLocation());
                }
            }.runTaskLater(Main.getInstance(), 1);

            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.teleport.player.online-other")).replace("%player%", from.getName()).replace("%other%", to.getName())));
            return true;
        }

        if (args.length == 3) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
                return true;
            }
            boolean ignoreOne = args[0].equalsIgnoreCase("~");
            boolean ignoreTwo = args[1].equalsIgnoreCase("~");
            boolean ignoreThree = args[2].equalsIgnoreCase("~");
            try {
                if (!ignoreOne) {
                    double ignored = Double.parseDouble(args[0]);
                }
                if (!ignoreTwo) {
                    double ignored = Double.parseDouble(args[1]);
                }
                if (!ignoreThree) {
                    double ignored = Double.parseDouble(args[2]);
                }
            } catch (NumberFormatException ignored) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                return true;
            }
            double x = ignoreOne ? p.getLocation().getX() : Double.parseDouble(args[0]);
            double y = ignoreTwo ? p.getLocation().getY() : Double.parseDouble(args[1]);
            double z = ignoreThree ? p.getLocation().getZ() : Double.parseDouble(args[2]);
            Location location = new Location(p.getWorld(), x, y, z);
            new BukkitRunnable() {
                @Override
                public void run() {
                    p.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
                }
            }.runTaskLater(Main.getInstance(), 1);
            p.sendMessage(
                    C.chat(
                            Objects.requireNonNull(Locale.get().getString("commands.teleport.location.self"))
                                    .replace("%x%", String.valueOf(Calculate.round(x, 2)))
                                    .replace("%y%", String.valueOf(Calculate.round(y, 2)))
                                    .replace("%z%", String.valueOf(Calculate.round(z, 2)))
                    )
            );
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (args[0].equalsIgnoreCase("@p") || args[0].equalsIgnoreCase("@s")) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
                return true;
            }
            player = p;
        }
        if (player == null) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
            return true;
        }

        boolean ignoreOne = args[1].equalsIgnoreCase("~");
        boolean ignoreTwo = args[2].equalsIgnoreCase("~");
        boolean ignoreThree = args[3].equalsIgnoreCase("~");
        try {
            if (!ignoreOne) {
                double ignored = Double.parseDouble(args[1]);
            }
            if (!ignoreTwo) {
                double ignored = Double.parseDouble(args[2]);
            }
            if (!ignoreThree) {
                double ignored = Double.parseDouble(args[3]);
            }
        } catch (NumberFormatException ignored) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
            return true;
        }

        double x = ignoreOne ? player.getLocation().getX() : Double.parseDouble(args[1]);
        double y = ignoreTwo ? player.getLocation().getY() : Double.parseDouble(args[2]);
        double z = ignoreThree ? player.getLocation().getZ() : Double.parseDouble(args[3]);
        Location location = new Location(player.getWorld(), x, y, z);
        Player finalPlayer = player;
        new BukkitRunnable() {
            @Override
            public void run() {
                finalPlayer.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
            }
        }.runTaskLater(Main.getInstance(), 1);
        sender.sendMessage(
                C.chat(
                        Objects.requireNonNull(Locale.get().getString("commands.teleport.location.online"))
                                .replace("%player%", player.getName())
                                .replace("%x%", String.valueOf(Calculate.round(x, 2)))
                                .replace("%y%", String.valueOf(Calculate.round(y, 2)))
                                .replace("%z%", String.valueOf(Calculate.round(z, 2)))
                )
        );
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String s,
            @NotNull String[] args
    ) {
        // Only tab complete if sender has permission
        if (!Permission.has(sender, "teleport", "admin")) {
            return List.of();
        }

        // First argument
        if (args.length == 1) {
            List<String> suggestions = new java.util.ArrayList<>();
            suggestions.add("top");
            suggestions.add("all");
            suggestions.add("here");
            // Add online player names
            for (Player player : Bukkit.getOnlinePlayers()) {
                suggestions.add(player.getName());
            }
            String current = args[0].toLowerCase();
            return suggestions.stream()
                    .filter(sugg -> sugg.toLowerCase().startsWith(current))
                    .toList();
        }

        // Second argument
        if (args.length == 2) {
            // If first arg is "here", suggest online players
            if (args[0].equalsIgnoreCase("here")) {
                String current = args[1].toLowerCase();
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(current))
                        .toList();
            }
            // If first arg is a player, suggest online players for second arg
            Player first = Bukkit.getPlayer(args[0]);
            if (first != null) {
                String current = args[1].toLowerCase();
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(current))
                        .toList();
            }
            // Otherwise, suggest ~ for coordinates
            return List.of("~");
        }

        // Third and fourth arguments: suggest ~ for coordinates
        if (args.length == 3 || args.length == 4) {
            return List.of("~");
        }

        return List.of();
    }

}
