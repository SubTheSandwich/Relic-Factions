package me.sub.RelicFactions.Commands.Admin;

import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SpawnerCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }

        if (!Permission.has(p, "spawner", "admin")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }

        if (args.length != 1 && args.length != 2) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.spawner.usage"))));
            return true;
        }

        EntityType type;
        try {
            type = EntityType.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.spawner.usage"))));
            return true;
        }

        if (!type.isSpawnable() || !type.isAlive()) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.spawner.usage"))));
            return true;
        }

        Player target = p;
        if (args.length == 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
                return true;
            }
        }

        ItemStack spawner = new ItemStack(Material.SPAWNER, 1);
        BlockStateMeta meta = (BlockStateMeta) spawner.getItemMeta();
        CreatureSpawner cs = (CreatureSpawner) Objects.requireNonNull(meta).getBlockState();
        cs.setSpawnedType(type);
        meta.setBlockState(cs);
        spawner.setItemMeta(meta);
        Map<Integer, ItemStack> leftovers = target.getInventory().addItem(spawner);
        for (ItemStack leftover : leftovers.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), leftover);
        }
        target.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.spawner.success")).replace("%entity%", type.name())));
        if (!target.equals(p)) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.spawner.success")).replace("%entity%", type.name())));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        if (args.length == 1) {
            String input = args[0].toUpperCase();
            List<String> suggestions = new ArrayList<>();
            for (EntityType type : EntityType.values()) {
                if (type.isSpawnable() && type.isAlive()) {
                    String name = type.name();
                    if (name.startsWith(input)) {
                        suggestions.add(name);
                    }
                }
            }
            Collections.sort(suggestions);
            return suggestions;
        }
        if (args.length == 2) return null;
        return Collections.emptyList();
    }
}
