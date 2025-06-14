package me.sub.RelicFactions.Commands.User;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.PlayerTimer;
import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Files.Normal.Messages;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class LFFCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }
        User user = User.get(p);
        if (user.hasTimer("lff")) {
            String message = Locale.get().getString("events.timer.player.cooldown.lff") == null ? Locale.get().getString("events.timer.player.cooldown.default") : Locale.get().getString("events.timer.cooldown.lff");
            message = Objects.requireNonNull(message).replace("%time%", Timer.format(user.getTimer("lff").getDuration()));
            p.sendMessage(C.chat(message));
            return true;
        }
        PlayerTimer timer = new PlayerTimer(p.getUniqueId(), Timer.LFF);
        user.addTimer(timer);
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String line : Messages.get().getStringList("other.lff")) {
                if (line.contains("%player%")) line = line.replace("%player%", p.getName());
                player.sendMessage(C.chat(line));
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        return List.of();
    }
}
