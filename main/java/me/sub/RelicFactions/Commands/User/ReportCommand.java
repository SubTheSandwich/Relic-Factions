package me.sub.RelicFactions.Commands.User;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.PlayerTimer;
import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ReportCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }

        User user = User.get(p);
        if (user.hasTimer("report")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.report.cooldown"))));
            return true;
        }

        if (args.length < 2) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.report.usage"))));
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
            return true;
        }

        if (player.getUniqueId().equals(p.getUniqueId())) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.report.self"))));
            return true;
        }

        String message = C.strip(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.report.success"))));
        for (Player staff : Main.getOnlineStaff()) {
            staff.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.report.broadcast")).replace("%reporter%", p.getName()).replace("%reported%", player.getName()).replace("%reason%", message)));
        }
        PlayerTimer timer = new PlayerTimer(p.getUniqueId(), Timer.REPORT);
        user.addTimer(timer);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            String current = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(current))
                    .toList();
        }
        return List.of();
    }
}
