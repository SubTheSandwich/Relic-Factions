package me.sub.RelicFactions.Commands.User;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Enums.Color;
import me.sub.RelicFactions.Files.Normal.Inventories;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfileCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }
        if (args.length == 1) {
            User user = User.get(args[0]);
            if (user == null) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
                return true;
            }
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.profile.other")).replace("%player%", user.getName())));
            open(p, user);
            return true;
        }
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.profile.self"))));
        open(p, User.get(p));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) return List.of();
        if (args.length == 1) {
            return null;
        }
        return List.of();
    }

    private void open(Player p, User user) {

        Inventory inventory = Bukkit.createInventory(null, Inventories.get().getInt("profile.size"), C.chat(Objects.requireNonNull(Inventories.get().getString("profile.name"))));

        ItemStack kills = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(Inventories.get().getString("profile.items.kills.item")))));
        ItemMeta killsMeta = kills.getItemMeta();
        Objects.requireNonNull(killsMeta).setDisplayName(C.chat("&aKills&7: &r" + user.getKills()));
        killsMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        kills.setItemMeta(killsMeta);

        ItemStack deaths = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(Inventories.get().getString("profile.items.deaths.item")))));
        ItemMeta deathsMeta = deaths.getItemMeta();
        Objects.requireNonNull(deathsMeta).setDisplayName(C.chat("&cDeaths&7: &r" + user.getDeaths()));
        deathsMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        deaths.setItemMeta(deathsMeta);


        ItemStack player = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta playerMeta = (SkullMeta) player.getItemMeta();
        if (playerMeta == null) return;
        playerMeta.setOwningPlayer(Bukkit.getOfflinePlayer(user.getUUID()));
        playerMeta.setOwnerProfile(Bukkit.getOfflinePlayer(user.getUUID()).getPlayerProfile());
        playerMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        playerMeta.setDisplayName(C.chat("&b" + user.getName() + "&e's Profile"));
        player.setItemMeta(playerMeta);

        ItemStack ores = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(Inventories.get().getString("profile.items.ores.item")))));
        ItemMeta oresMeta = ores.getItemMeta();
        if (oresMeta == null) return;
        ArrayList<String> lore = getStrings(user);
        oresMeta.setLore(lore);
        oresMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        oresMeta.setDisplayName(C.chat(Objects.requireNonNull(Inventories.get().getString("profile.items.ores.name"))));
        ores.setItemMeta(oresMeta);

        inventory.setItem(Inventories.get().getInt("profile.items.kills.slot"), kills);
        inventory.setItem(Inventories.get().getInt("profile.items.deaths.slot"), deaths);
        inventory.setItem(Inventories.get().getInt("profile.items.ores.slot"), ores);
        inventory.setItem(Inventories.get().getInt("profile.items.player.slot"), player);

        ItemStack fill = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(Inventories.get().getString("filler.item")))));
        ItemMeta meta = fill.getItemMeta();
        if (meta == null) return;
        meta.setDisplayName(C.chat("&e"));
        fill.setItemMeta(meta);

        if (Inventories.get().getBoolean("filler.enabled")) {
            for (int i = 0; i < inventory.getSize(); i++) {
                if (inventory.getItem(i) != null) continue;
                inventory.setItem(i, fill);
            }
        }

        p.openInventory(inventory);
    }

    private @NotNull ArrayList<String> getStrings(User user) {
        ArrayList<String> lore = new ArrayList<>();
        lore.add(format(Color.GRAY, "Coal", user.getCoalMined()));
        lore.add(format(Color.WHITE, "Iron", user.getIronMined()));
        lore.add(format(Color.GOLD, "Copper", user.getCopperMined()));
        lore.add(format(Color.YELLOW, "Gold", user.getGoldMined()));
        lore.add(format(Color.LIGHT_RED, "Redstone", user.getRedstoneMined()));
        lore.add(format(Color.BLUE, "Lapis", user.getLapisMined()));
        lore.add(format(Color.LIGHT_BLUE, "Diamond(s)", user.getDiamondMined()));
        lore.add(format(Color.LIME, "Emerald(s)", user.getEmeraldMined()));
        lore.add(format(Color.WHITE, "Quartz", user.getQuartzMined()));
        lore.add(format(Color.RED, "Ancient Debris", user.getDebrisMined()));
        return lore;
    }

    private String format(Color color, String name, int what) {
        return C.chat(color.toColorCode() + name + "&7: " + what);
    }
}
