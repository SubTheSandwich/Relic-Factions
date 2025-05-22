package me.sub.RelicFactions.Commands.User;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.Filter;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Files.Normal.Messages;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FilterCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }
        if (args.length == 0) {
            Messages.send(p, "help.filter");
            return true;
        }
        User user = User.get(p);
        Filter filter = user.getFilter();
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("clear")) {
                filter.getItems().clear();
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.filter.clear"))));
                return true;
            }
            if (args[0].equalsIgnoreCase("toggle")) {
                boolean enabled = !filter.isEnabled();
                filter.setEnabled(enabled);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.filter.toggle")).replace("%status%", Objects.requireNonNull(enabled ? Locale.get().getString("primary.enabled") : Locale.get().getString("primary.disabled")))));
                return true;
            }
            if (args[0].equalsIgnoreCase("list")) {
                if (filter.getItems().isEmpty()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.filter.list.none"))));
                    return true;
                }
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.filter.list.show")).replace("%items%", filter.getItems().stream().map(Material::name).collect(Collectors.joining(", ")))));
                return true;
            }
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                Material material = Material.matchMaterial(args[1]);
                if (material == null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-material"))));
                    return true;
                }
                if (filter.getItems().contains(material)) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.filter.add.already"))));
                    return true;
                }
                filter.getItems().add(material);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.filter.add.success")).replace("%material%", material.name())));
                return true;
            }
            if (args[0].equalsIgnoreCase("remove")) {
                Material material = Material.matchMaterial(args[1]);
                if (material == null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-material"))));
                    return true;
                }
                if (!filter.getItems().contains(material)) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.filter.remove.not"))));
                    return true;
                }
                filter.getItems().remove(material);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.filter.remove.success")).replace("%material%", material.name())));
                return true;
            }
        }
        Messages.send(p, "help.filter");
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) return List.of("toggle", "list", "add", "remove", "clear");
        if (args.length == 2) return Arrays.stream(Material.values()).map(Material::name).toList();
        return List.of();
    }
}
