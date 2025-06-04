package me.sub.RelicFactions.Commands.Admin;

import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.KOTH;
import me.sub.RelicFactions.Files.Classes.RunningKOTH;
import me.sub.RelicFactions.Files.Data.KOTHData;
import me.sub.RelicFactions.Files.Enums.FactionType;
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

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public class KOTHCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        if (!Permission.has(sender, "koth", "admin")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }

        if (args.length < 2 || args.length > 3) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.usage"))));
            return true;
        }

        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("create")) {
                KOTH koth = KOTH.get(args[0]);
                if (koth != null) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.already-exists")).replace("%koth%", koth.getName())));
                    return true;
                }
                UUID uuid = UUID.randomUUID();
                KOTHData kothData = new KOTHData(uuid);
                kothData.get().set("uuid", uuid.toString());
                kothData.get().set("name", args[0]);
                kothData.get().set("pearlable", true);
                kothData.get().set("special", false);
                kothData.get().set("time", Main.getInstance().getConfig().getInt("koth.time"));
                kothData.save();
                koth = new KOTH(kothData);
                Main.getInstance().koths.put(uuid, koth);
                Main.getInstance().kothNameHolder.put(args[0].toLowerCase(), koth);
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.created")).replace("%koth%", koth.getName())));
                return true;
            }

            KOTH koth = KOTH.get(args[0]);
            if (koth == null) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.doesnt-exist")).replace("%koth%", args[0])));
                return true;
            }

            if (args[1].equalsIgnoreCase("delete")) {
                if (Main.getInstance().runningKOTHS.containsKey(koth.getUUID())) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.cannot-delete")).replace("%koth%", args[0])));
                    return true;
                }
                String name = koth.getName();
                UUID uuid = koth.getUUID();
                boolean del = koth.getKothData().delete();
                if (del) {
                    Main.getInstance().koths.remove(uuid);
                    Main.getInstance().kothNameHolder.remove(name.toLowerCase());
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.deleted")).replace("%koth%", name)));
                } else {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.error"))));
                }
                return true;
            }

            if (args[1].equalsIgnoreCase("start")) {
                if (Main.getInstance().runningKOTHS.containsKey(koth.getUUID())) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.already-started")).replace("%koth%", args[0])));
                    return true;
                }
                if (!koth.isSetup()) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.not-setup")).replace("%koth%", args[0])));
                    return true;
                }
                RunningKOTH runningKOTH = new RunningKOTH(koth);
                Main.getInstance().runningKOTHS.put(koth.getUUID(), runningKOTH);
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.started")).replace("%koth%", args[0])));
                return true;
            }

            if (args[1].equalsIgnoreCase("stop")) {
                if (!Main.getInstance().runningKOTHS.containsKey(koth.getUUID())) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.not-running")).replace("%koth%", args[0])));
                    return true;
                }
                Main.getInstance().runningKOTHS.remove(koth.getUUID());
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.stopped")).replace("%koth%", args[0])));
                Main.getInstance().sendGlobalMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.koth.end.cancelled")).replace("%koth%", args[0])));
                return true;
            }

            if (args[1].equalsIgnoreCase("setpos1")) {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
                    return true;
                }

                if (koth.getPositionTwo() != null) {
                    if (!Objects.equals(koth.getPositionTwo().getWorld(), koth.getPositionOne().getWorld())) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.world"))));
                        return true;
                    }
                }
                Location clone = p.getLocation().clone();
                clone.setY(0);
                koth.setPositionOne(clone);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.setpos1")).replace("%koth%", koth.getName())));
                return true;
            }

            if (args[1].equalsIgnoreCase("setpos2")) {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
                    return true;
                }

                if (koth.getPositionOne() != null) {
                    if (!Objects.equals(koth.getPositionOne().getWorld(), koth.getPositionOne().getWorld())) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.world"))));
                        return true;
                    }
                }
                Location clone = p.getLocation().clone();
                clone.setY(0);
                koth.setPositionTwo(clone);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.setpos2")).replace("%koth%", koth.getName())));
                return true;
            }
            if (args[1].equalsIgnoreCase("settime")) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.settime.usage"))));
                return true;
            }
            if (args[1].equalsIgnoreCase("special")) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.special.usage"))));
                return true;
            }
            if (args[1].equalsIgnoreCase("pearlable")) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.pearlable.usage"))));
                return true;
            }
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.usage"))));
            return true;
        }

        KOTH koth = KOTH.get(args[0]);
        if (koth == null) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.doesnt-exist")).replace("%koth%", args[0])));
            return true;
        }

        if (args[1].equalsIgnoreCase("setfaction")) {

            Faction faction = Faction.get(args[2]);
            if (faction == null) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.faction-only"))));
                return true;
            }
            if (!faction.getType().equals(FactionType.KOTH)) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.not-koth"))));
                return true;
            }
            koth.setFaction(faction.getUUID());
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.setfaction")).replace("%faction%", faction.getName()).replace("%koth%", koth.getName())));
            return true;
        }

        if (args[1].equalsIgnoreCase("settime")) {
            try {
                int ignored = Integer.parseInt(args[2]);
            } catch (NumberFormatException ignored) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.settime.usage"))));
                return true;
            }
            int time = Integer.parseInt(args[2]);
            if (time <= 0) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.settime.usage"))));
                return true;
            }
            koth.setTime(time);
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.settime.set")).replace("%koth%", koth.getName()).replace("%time%", time + "")));
            return true;
        }

        if (args[1].equalsIgnoreCase("pearlable")) {
            boolean value = Boolean.parseBoolean(args[2]);
            koth.setPearlable(value);
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.pearlable.set")).replace("%status%", value ? Objects.requireNonNull(Locale.get().getString("primary.enabled")).toLowerCase() : Objects.requireNonNull(Locale.get().getString("primary.disabled")).toLowerCase()).replace("%koth%", koth.getName())));
            return true;
        }

        if (args[1].equalsIgnoreCase("special")) {
            boolean value = Boolean.parseBoolean(args[2]);
            koth.setSpecial(value);
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.koth.special.set")).replace("%status%", value ? Objects.requireNonNull(Locale.get().getString("primary.enabled")).toLowerCase() : Objects.requireNonNull(Locale.get().getString("primary.disabled")).toLowerCase()).replace("%koth%", koth.getName())));
            return true;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        List<String> subcommands = List.of(
                "create", "setfaction", "start", "stop", "setpos1", "setpos2", "settime", "special", "pearlable", "delete"
        );
        List<String> kothNames = Main.getInstance().kothNameHolder.keySet().stream().toList();

        if (args.length == 1) {
            // Suggest KOTH names for the first argument
            return kothNames.stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            // Suggest subcommands for the second argument
            return subcommands.stream()
                    .filter(sub -> sub.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }

        if (args.length == 3) {
            String sub = args[1].toLowerCase();
            String input = args[2].toLowerCase();

            switch (sub) {
                case "special", "pearlable" -> {
                    return Stream.of("true", "false")
                            .filter(val -> val.startsWith(input))
                            .toList();
                }
                case "settime" -> {
                    return "<seconds>".startsWith(input) ? List.of("<seconds>") : List.of();
                }
                case "setfaction" -> {
                    return Main.getInstance().factionNameHolder.values().stream()
                            .filter(faction -> faction.getType() == FactionType.KOTH)
                            .map(Faction::getName)
                            .filter(name -> name.toLowerCase().startsWith(input))
                            .toList();
                }
            }
        }

        return List.of();
    }
}
