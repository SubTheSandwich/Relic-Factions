package me.sub.RelicFactions.Commands.Admin;

import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HCFCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!Permission.has(sender, "hcf", "admin")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.hcf.usage"))));
            return true;
        }
        if (args[0].equalsIgnoreCase("save")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.hcf.save.attempt"))));
            Main.getInstance().saveFiles();
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.hcf.save.success"))));
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.hcf.reload.attempt"))));
            Main.getInstance().saveFiles();
            Main.getInstance().factions.clear();
            Main.getInstance().factionNameHolder.clear();
            Main.getInstance().users.clear();
            Main.getInstance().userNameHolder.clear();
            Main.getInstance().loadFiles();
            Locale.load();
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.hcf.reload.success"))));
            return true;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!Permission.has(sender, "hcf", "admin")) return new ArrayList<>();
        if (args.length == 1) return List.of("save", "reload");
        return new ArrayList<>();
    }
}
