package me.sub.RelicFactions.Commands.Staff;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.ModMode;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Files.Normal.ModModeFile;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InvseeCommand implements TabExecutor {

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }

        if (!Permission.has(p, "invsee", "staff")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }

        if (!ModModeFile.get().getBoolean("mod-mode.enabled")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.feature-disabled"))));
            return true;
        }

        if (args.length != 1) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.invsee.usage")), label));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.invsee.usage")), label));
            return true;
        }

        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.invsee.success")).replace("%player%", target.getName())));

        User user = User.get(p);
        ModMode modMode = user.getModMode();

        if ((modMode == null || modMode.isInBypass()) && Permission.has(p, "admin")) {
            p.openInventory(target.getInventory());
        } else {
            Inventory viewInv = Bukkit.createInventory(
                    null,
                    36, // 4 rows of 9
                    C.chat("&eInventory Inspector")
            );

            ItemStack[] contents = target.getInventory().getContents();
            ItemStack[] mainOnly = new ItemStack[36];
            System.arraycopy(contents, 0, mainOnly, 0, 36);

            viewInv.setContents(mainOnly);
            p.openInventory(viewInv);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!Permission.has(sender, "invsee", "staff")) return List.of();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
