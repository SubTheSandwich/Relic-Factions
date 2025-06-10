package me.sub.RelicFactions.Commands.Staff;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class StaffChatCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }

        if (!Permission.has(p, "staffchat", "staff")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }

        if (!Main.getInstance().getConfig().getBoolean("features.staff-chat")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.feature-disabled"))));
            return true;
        }

        User user = User.get(p);

        if (args.length == 0) {
            user.setStaffChat(!user.isStaffChat());
            String message = Locale.get().getString("commands.staffchat.toggle");
            message = Objects.requireNonNull(message).replace("%status%", Objects.requireNonNull(user.isStaffChat() ? Locale.get().getString("primary.enabled") : Locale.get().getString("primary.disabled")));
            p.sendMessage(C.chat(message));
            return true;
        }

        for (Player player : Main.getOnlineStaff()) {
            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.staffchat.message")).replace("%player%", p.getName()).replace("%message%", C.strip(String.join(" ", args)))));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
