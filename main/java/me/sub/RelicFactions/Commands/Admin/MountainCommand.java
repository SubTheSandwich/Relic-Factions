package me.sub.RelicFactions.Commands.Admin;

import me.sub.RelicFactions.Files.Classes.Mountain;
import me.sub.RelicFactions.Files.Data.MountainData;
import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.Material;
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
import java.util.UUID;
import java.util.stream.Stream;

public class MountainCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        if (!Permission.has(sender, "mountain", "admin")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }

        if (args.length < 2 || args.length > 3) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.mountain.usage"))));
            return true;
        }

        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("create")) {
                Mountain mountain = Mountain.get(args[0]);
                if (mountain != null) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.mountain.exists"))));
                    return true;
                }
                UUID uuid = UUID.randomUUID();
                MountainData mountainData = new MountainData(uuid);
                mountainData.get().set("uuid", uuid.toString());
                mountainData.get().set("name", args[0]);
                mountainData.get().set("time", Main.getInstance().getConfig().getInt("mountain.reset-time"));
                mountainData.save();
                mountain = new Mountain(mountainData);
                Main.getInstance().mountains.put(uuid, mountain);
                Main.getInstance().mountainNameHolder.put(args[0].toLowerCase(), mountain);
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.mountain.created")).replace("%mountain%", mountain.getName())));
                return true;
            }

            Mountain mountain = Mountain.get(args[0]);
            if (mountain == null) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.mountain.doesnt-exist")).replace("%mountain%", args[0])));
                return true;
            }

            if (args[1].equalsIgnoreCase("delete")) {
                String name = mountain.getName();
                UUID uuid = mountain.getUUID();
                boolean del = mountain.getMountainData().delete();
                if (del) {
                    Main.getInstance().mountains.remove(uuid);
                    Main.getInstance().mountainNameHolder.remove(name.toLowerCase());
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.mountain.deleted")).replace("%mountain%", name)));
                } else {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.error"))));
                }
                return true;
            }

            if (args[1].equalsIgnoreCase("reset")) {
                if (!mountain.isSetup()) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.mountain.not-setup")).replace("%mountain%", mountain.getName())));
                    return true;
                }
                mountain.setTime(BigDecimal.valueOf(mountain.getDefaultTime() * 60L));
                return true;
            }
            if (args[1].equalsIgnoreCase("setpos1")) {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
                    return true;
                }

                if (mountain.getPositionTwo() != null) {
                    if (!Objects.equals(mountain.getPositionTwo().getWorld(), mountain.getPositionOne().getWorld())) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.world"))));
                        return true;
                    }
                }
                mountain.setPositionOne(p.getLocation());
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.mountain.setpos1")).replace("%mountain%", mountain.getName())));
                return true;
            }

            if (args[1].equalsIgnoreCase("setpos2")) {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
                    return true;
                }

                if (mountain.getPositionOne() != null) {
                    if (!Objects.equals(mountain.getPositionOne().getWorld(), mountain.getPositionOne().getWorld())) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.world"))));
                        return true;
                    }
                }
                mountain.setPositionTwo(p.getLocation());
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.mountain.setpos2")).replace("%mountain%", mountain.getName())));
                return true;
            }
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.mountain.usage"))));
            return true;
        }

        Mountain mountain = Mountain.get(args[0]);
        if (mountain == null) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.mountain.doesnt-exist")).replace("%mountain%", args[0])));
            return true;
        }

        if (args[1].equalsIgnoreCase("setreset")) {
            try {
                int ignored = Integer.parseInt(args[2]);
            } catch (NumberFormatException ignored) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                return true;
            }
            int time = Integer.parseInt(args[2]);
            if (time <= 0) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                return true;
            }
            mountain.setDefaultTime(time);
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.mountain.setreset")).replace("%mountain%", mountain.getName()).replace("%time%", Timer.getMessageFormat(time * 60000L))));
            return true;
        }

        if (args[1].equalsIgnoreCase("settype")) {
            Material material = Material.matchMaterial(args[2]);
            if (material == null) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-material"))));
                return true;
            }
            if (!Main.getInstance().isValidMaterial(material)) {
                sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-material"))));
                return true;
            }
            mountain.setType(material);
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.mountain.settype")).replace("%mountain%", mountain.getName()).replace("%material%", material.name())));
            return true;
        }
        sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.mountain.usage"))));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        List<String> subcommands = List.of(
                "create", "delete", "reset", "setpos1", "setpos2", "setreset", "settype"
        );
        List<String> mountainNames = Main.getInstance().mountainNameHolder.keySet().stream().toList();

        if (args.length == 1) {
            // Suggest all mountain names (for existing) and "create" (for new)
            List<String> options = new ArrayList<>(mountainNames);
            return options.stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            String first = args[0].toLowerCase();
            if (first.equals("create")) return List.of();
            return subcommands.stream()
                    .filter(sub -> sub.startsWith(args[1].toLowerCase()))
                    .toList();
        }

        if (args.length == 3) {
            String sub = args[1].toLowerCase();
            String input = args[2].toLowerCase();
            if (sub.equals("setreset")) {
                return Stream.of("<minutes>")
                        .filter(val -> val.startsWith(input))
                        .toList();
            }
            if (sub.equals("settype")) {
                return java.util.Arrays.stream(Material.values())
                        .filter(Material::isBlock)
                        .map(Material::name)
                        .map(String::toLowerCase)
                        .filter(name -> name.startsWith(input))
                        .limit(20) // Limit to 20 suggestions for performance
                        .toList();
            }
        }

        return List.of();
    }
}
