package me.sub.RelicFactions.Commands.Admin;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ReviveCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!Permission.has(sender, "revive", "admin")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }

        if (!Main.getInstance().getConfig().getBoolean("features.deathban")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.feature-disabled"))));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.revive.usage"))));
            return true;
        }
        User user = User.get(args[0]);
        if (user == null) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
            return true;
        }
        if (!user.isDeathBanned()) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.lives.not-deathbanned"))));
            return true;
        }
        user.setDeathbannedTill(0);
        user.setDeathBanned(false);
        user.setRevived(true);
        sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.revive.success")).replace("%player%", user.getName())));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!Permission.has(sender, "revive", "admin")) {
            return List.of();
        }
        if (args.length == 1) return null;
        return List.of();
    }
}
