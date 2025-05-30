package me.sub.RelicFactions.Commands.Staff;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.ModMode;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Files.Normal.ModModeFile;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ModModeCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }

        if (!Permission.has(p, "modmode", "staff")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }

        if (!ModModeFile.get().getBoolean("mod-mode.enabled")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.feature-disabled"))));
            return true;
        }

        User user = User.get(p);

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("bypass")) {
                if (!Permission.has(p, "modmode.bypass", "staff")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                    return true;
                }
                ModMode modMode = user.getModMode();
                if (modMode != null) {
                    modMode.setHasBypass(!modMode.isInBypass());
                    String message = Locale.get().getString("commands.mod-mode.bypass");
                    message = Objects.requireNonNull(message).replace("%status%", modMode.isInBypass() ? Objects.requireNonNull(Locale.get().getString("primary.enabled")) : Objects.requireNonNull(Locale.get().getString("primary.disabled")));
                    p.sendMessage(C.chat(message));
                    return true;
                }
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.mod-mode.not-in"))));
                return true;
            }
        }


        ModMode modMode = user.getModMode();
        if (modMode == null) {
            String message = Locale.get().getString("commands.mod-mode.toggle");
            message = Objects.requireNonNull(message).replace("%status%", Objects.requireNonNull(Locale.get().getString("primary.enabled")));
            p.sendMessage(C.chat(message));
            modMode = new ModMode();
            user.setLastInventoryContents(p.getInventory().getContents());
            user.setModMode(modMode);
            modMode.setInMode(true);
            ModModeFile.setItems(p);
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.showPlayer(Main.getInstance(), p);
            }
            p.getInventory().clear();
            p.getInventory().setContents(user.getLastInventoryContents());
            user.setModMode(null);
            String message = Locale.get().getString("commands.mod-mode.toggle");
            message = Objects.requireNonNull(message).replace("%status%", Objects.requireNonNull(Locale.get().getString("primary.disabled")));
            p.sendMessage(C.chat(message));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }
}
