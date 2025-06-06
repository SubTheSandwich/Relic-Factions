package me.sub.RelicFactions.Commands.Admin;

import me.sub.RelicFactions.Files.Classes.Conquest;
import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.RunningConquest;
import me.sub.RelicFactions.Files.Data.ConquestData;
import me.sub.RelicFactions.Files.Data.Zone;
import me.sub.RelicFactions.Files.Enums.FactionType;
import me.sub.RelicFactions.Files.Enums.ZoneType;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ConquestCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        if (!Permission.has(sender, "conquest", "admin")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }

        if (args.length < 2 || args.length > 4) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.usage"))));
            return true;
        }

        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("create")) {
                Conquest conquest = Conquest.get(args[0]);
                if (conquest != null) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.already-exists")).replace("%conquest%", conquest.getName())));
                    return true;
                }
                UUID uuid = UUID.randomUUID();
                ConquestData conquestData = new ConquestData(uuid);
                conquestData.setup();
                conquestData.get().set("uuid", uuid.toString());
                conquestData.get().set("name", args[0]);
                conquestData.get().set("red", new Zone(ZoneType.RED).serialize());
                conquestData.get().set("green", new Zone(ZoneType.GREEN).serialize());
                conquestData.get().set("blue", new Zone(ZoneType.BLUE).serialize());
                conquestData.get().set("yellow", new Zone(ZoneType.YELLOW).serialize());
                conquestData.save();
                conquest = new Conquest(conquestData);
                Main.getInstance().conquests.put(uuid, conquest);
                Main.getInstance().conquestNameHolder.put(args[0].toLowerCase(), conquest);
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.created")).replace("%conquest%", conquest.getName())));
                return true;
            }

            Conquest conquest = Conquest.get(args[0]);
            if (conquest == null) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.doesnt-exist")).replace("%conquest%", args[0])));
                return true;
            }

            if (args[1].equalsIgnoreCase("delete")) {
                if (Main.getInstance().getRunningConquest() != null && Main.getInstance().getRunningConquest().getConquest().getUUID().equals(conquest.getUUID())) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.cannot-delete")).replace("%conquest%", args[0])));
                    return true;
                }
                String name = conquest.getName();
                UUID uuid = conquest.getUUID();
                boolean del = conquest.getConquestData().delete();
                if (del) {
                    Main.getInstance().conquests.remove(uuid);
                    Main.getInstance().conquestNameHolder.remove(name.toLowerCase());
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.deleted")).replace("%conquest%", name)));
                } else {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.error"))));
                }
                return true;
            }
            if (args[1].equalsIgnoreCase("start")) {

                if (!conquest.isSetup()) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.not-setup")).replace("%conquest%", args[0])));
                    return true;
                }

                if (Main.getInstance().getRunningConquest() != null && Main.getInstance().getRunningConquest().getConquest().getUUID().equals(conquest.getUUID())) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.already-started")).replace("%conquest%", args[0])));
                    return true;
                }

                RunningConquest runningConquest = new RunningConquest(conquest);
                Main.getInstance().setRunningConquest(runningConquest);
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.started")).replace("%conquest%", conquest.getName())));
                return true;
            }
            if (args[1].equalsIgnoreCase("stop")) {

                if (!conquest.isSetup()) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.not-setup")).replace("%conquest%", args[0])));
                    return true;
                }

                if (Main.getInstance().getRunningConquest() == null || !Main.getInstance().getRunningConquest().getConquest().getUUID().equals(conquest.getUUID())) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.not-running")).replace("%conquest%", args[0])));
                    return true;
                }

                Main.getInstance().setRunningConquest(null);
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.stopped")).replace("%conquest%", conquest.getName())));
                Main.getInstance().sendGlobalMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.end.cancelled")).replace("%conquest%", conquest.getName())));
                return true;
            }
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.usage"))));
            return true;
        }

        Conquest conquest = Conquest.get(args[0]);
        if (conquest == null) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.doesnt-exist")).replace("%conquest%", args[0])));
            return true;
        }

        if (args.length == 3) {
            if (args[1].equalsIgnoreCase("setarea")) {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
                    return true;
                }
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.setarea.usage"))));
                return true;
            }
            if (args[1].equalsIgnoreCase("setpoints")) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.setpoints.usage"))));
                return true;
            }
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.usage"))));
            return true;
        }

        if (args[1].equalsIgnoreCase("setarea")) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
                return true;
            }
            if (!ZoneType.isValid(args[2])) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.setarea.invalid-zone")).replace("%zone%", args[2])));
                return true;
            }
            Zone zone = conquest.getZone(ZoneType.valueOf(args[2].toUpperCase()));
            if (!args[3].equalsIgnoreCase("1") && !args[3].equalsIgnoreCase("2")) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.setarea.usage"))));
                return true;
            }
            boolean which = args[3].equalsIgnoreCase("1");
            Location clone = p.getLocation().clone();
            clone.setY(0);
            if (which) {
                if (zone.getCornerTwo() != null) {
                    if (!Objects.equals(zone.getCornerTwo().getWorld(), p.getWorld())) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.world"))));
                        return true;
                    }
                }
                zone.setCornerOne(clone);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.setarea.one")).replace("%conquest%", conquest.getName()).replace("%zone%", zone.getName())));
            } else {
                if (zone.getCornerOne() != null) {
                    if (!Objects.equals(zone.getCornerOne().getWorld(), p.getWorld())) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.world"))));
                        return true;
                    }
                }
                zone.setCornerTwo(clone);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.setarea.two")).replace("%conquest%", conquest.getName()).replace("%zone%", zone.getName())));
            }
            switch (zone.getType()) {
                case YELLOW -> conquest.setYellow(zone);
                case GREEN -> conquest.setGreen(zone);
                case RED -> conquest.setRed(zone);
                case BLUE -> conquest.setBlue(zone);
                case null, default -> {
                    return true;
                }
            }
            return true;
        }
        if (args[1].equalsIgnoreCase("setpoints")) {
            if (Main.getInstance().getRunningConquest() == null) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.not-running")).replace("%conquest%", conquest.getName())));
                return true;
            }
            if (!Main.getInstance().getRunningConquest().getConquest().getUUID().equals(conquest.getUUID())) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.not-running")).replace("%conquest%", conquest.getName())));
                return true;
            }
            Faction faction = Faction.get(args[2]);
            if (faction == null) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.faction-only"))));
                return true;
            }
            if (!faction.getType().equals(FactionType.PLAYER)) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.player-only"))));
                return true;
            }
            RunningConquest runningConquest = Main.getInstance().getRunningConquest();
            int points;
            try {
                points = Integer.parseInt(args[3]);
            } catch (NumberFormatException ignored) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                return true;
            }
            if (points < 0) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                return true;
            }

            runningConquest.getPoints().put(faction.getUUID(), points);
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.setpoints.success")).replace("%faction%", faction.getName()).replace("%points%", points + "")));
            return true;
        }

        sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.conquest.usage"))));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        List<String> subcommands = List.of(
                "create", "delete", "start", "stop", "setarea", "setpoints"
        );
        List<String> conquestNames = Main.getInstance().conquestNameHolder.keySet().stream().toList();
        List<String> zoneNames = List.of("red", "green", "blue", "yellow");
        List<String> areaNumbers = List.of("1", "2");

        if (args.length == 1) {
            List<String> options = new ArrayList<>(conquestNames);
            return options.stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) return List.of();
            return subcommands.stream()
                    .filter(sub -> sub.startsWith(args[1].toLowerCase()))
                    .toList();
        }

        if (args.length == 3) {
            String sub = args[1].toLowerCase();
            if (sub.equals("setarea")) {
                return zoneNames.stream()
                        .filter(zone -> zone.startsWith(args[2].toLowerCase()))
                        .toList();
            }
            if (sub.equals("setpoints")) {
                // Suggest faction names
                return Main.getInstance().factionNameHolder.keySet().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .toList();
            }
        }

        if (args.length == 4) {
            String sub = args[1].toLowerCase();
            if (sub.equals("setarea")) {
                return areaNumbers.stream()
                        .filter(num -> num.startsWith(args[3].toLowerCase()))
                        .toList();
            }
            if (sub.equals("setpoints")) {
                return List.of("<points>");
            }
        }

        return List.of();
    }
}
