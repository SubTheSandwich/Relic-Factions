package me.sub.RelicFactions.Commands.Staff;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Files.Normal.Messages;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ChatCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!Permission.has(sender, "staff", "chat")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }

        if (args.length == 0 || args.length > 2) {
            Messages.send(sender, "help.chat");
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("mute")) {
                boolean muted = !Main.getInstance().getChat().isMuted();
                Main.getInstance().getChat().setMuted(muted);
                String message = muted ? Locale.get().getString("commands.chat.muted") : Locale.get().getString("commands.chat.unmuted");
                Bukkit.broadcast(Component.text(C.chat(Objects.requireNonNull(message))));
                return true;
            }
            if (args[0].equalsIgnoreCase("clear")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.chat.cleared"))));
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("relic.bypass.chat")) {
                        player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.chat.cleared"))));
                        continue;
                    }
                    for (int i = 0; i < 100; i++) {
                        player.sendMessage("");
                    }
                    player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.chat.cleared"))));
                }
                return true;
            }
            Messages.send(sender, "help.chat");
            return true;
        }

        if (!args[0].equalsIgnoreCase("slow")) {
            Messages.send(sender, "help.chat");
            return true;
        }

        int slow;
        try {
            slow = Integer.parseInt(args[1]);
        } catch (NumberFormatException ignored) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
            return true;
        }
        if (slow <= 0) {
            Main.getInstance().getChat().setSlowMode(0);
            Bukkit.broadcast(Component.text(C.chat(Objects.requireNonNull(Locale.get().getString("commands.chat.unslowed")))));
            for (Player player : Bukkit.getOnlinePlayers()) {
                User user = User.get(player);
                if (user.hasTimer("chat")) user.removeTimer("chat");
            }
            return true;
        }
        Main.getInstance().getChat().setSlowMode(slow);
        Bukkit.broadcast(Component.text(C.chat(Objects.requireNonNull(Locale.get().getString("commands.chat.slowed")).replace("%time%", slow + ""))));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String s,
            @NotNull String @NotNull[] args
    ) {
        if (!Permission.has(sender, "staff", "chat")) {
            return List.of();
        }

        if (args.length == 1) {
            return Stream.of("mute", "clear", "slow")
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("slow")) {
            return Stream.of("3", "5", "10", "15", "30", "60")
                    .filter(val -> val.startsWith(args[1]))
                    .toList();
        }

        return List.of();
    }
}
