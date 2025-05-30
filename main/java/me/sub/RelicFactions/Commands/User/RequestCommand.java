package me.sub.RelicFactions.Commands.User;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.PlayerTimer;
import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RequestCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }

        User user = User.get(p);
        if (user.hasTimer("request")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.request.cooldown"))));
            return true;
        }

        if (args.length < 1) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.request.usage"))));
            return true;
        }

        String message = C.strip(String.join(" ", Arrays.copyOfRange(args, 0, args.length)));
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.request.success"))));
        for (Player staff : Main.getOnlineStaff()) {
            staff.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.request.broadcast")).replace("%sender%", p.getName()).replace("%message%", message)));
        }
        PlayerTimer timer = new PlayerTimer(p.getUniqueId(), Timer.REQUEST);
        user.addTimer(timer);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }
}
