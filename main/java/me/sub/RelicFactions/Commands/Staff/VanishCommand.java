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

public class VanishCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }

        if (!Permission.has(p, "vanish", "staff")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }

        if (!ModModeFile.get().getBoolean("mod-mode.enabled")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.feature-disabled"))));
            return true;
        }

        User user = User.get(p);
        ModMode modMode = user.getModMode();
        if (modMode == null) {
            modMode = new ModMode();
            user.setLastInventoryContents(p.getInventory().getContents());
        }
        modMode.setInVanish(!modMode.isInVanish());
        String message = Locale.get().getString("commands.vanish");
        message = Objects.requireNonNull(message).replace("%status%", Objects.requireNonNull(modMode.isInVanish() ? Locale.get().getString("primary.enabled") : Locale.get().getString("primary.disabled")));
        p.sendMessage(C.chat(message));
        user.setModMode(modMode);
        if (modMode.isInVanish()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!Permission.has(p, "staff") && !Permission.has(p, "admin")) continue;
                player.hidePlayer(Main.getInstance(), p);
            }
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.showPlayer(Main.getInstance(), p);
            }
        }
        if (ModModeFile.has("vanish-hidden", p) || ModModeFile.has("vanish-visible", p)) {
            ModModeFile.setItems(p);
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }
}
