package me.sub.RelicFactions.Commands.Staff;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Files.Normal.Messages;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class PanicCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }

        if (!Permission.has(p, "panic", "staff")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }

        User user = User.get(p);
        user.setPanic(!user.isPanic());
        String message = user.isPanic() ? Locale.get().getString("commands.panic.initiated") : Locale.get().getString("commands.panic.fine");
        p.sendMessage(C.chat(Objects.requireNonNull(message)));
        if (user.isPanic()) {
            for (Player player : Main.getOnlineStaff()) {
                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.panic.broadcast")).replace("%player%", p.getName())));
            }
            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (!user.isPanic()) {
                        cancel();
                        return;
                    }
                    ticks++;
                    if (ticks == 100) {
                        Messages.send(p, "staff.panic");
                        ticks = 0;
                    }
                }
            }.runTaskTimer(Main.getInstance(), 0, 1);
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }
}
