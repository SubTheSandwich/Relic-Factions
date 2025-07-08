package me.sub.RelicFactions.Events.Player.Interact;

import me.sub.RelicFactions.Commands.User.ResearchCommand;
import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Enums.Tree;
import me.sub.RelicFactions.Files.Normal.Inventories;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Objects;

public class ResearchClickEvent implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getClickedInventory() == null) return;
        if (!C.serialize(e.getView().title()).equalsIgnoreCase(C.chat(Objects.requireNonNull(Inventories.get().getString("tree.name"))))) return;
        if (e.getCurrentItem() == null) return;
        if (!e.getCurrentItem().hasItemMeta()) return;
        e.setCancelled(true);
        ItemStack item = e.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        User user = User.get(p);
        if (user.getOpenedTreeFaction() == null) {
            p.closeInventory();
            return;
        }
        Faction faction = Faction.get(user.getOpenedTreeFaction());
        if (faction == null) {
            p.closeInventory();
            return;
        }
        if (user.getFaction() == null && !p.hasPermission("relic-factions.command.research.others")) {
            p.closeInventory();
            return;
        }
        if (user.getFaction() != null && !user.getFaction().equals(user.getOpenedTreeFaction()) && !p.hasPermission("relic-factions.command.research.others")) {
            p.closeInventory();
            return;
        }
        NamespacedKey name = new NamespacedKey(Main.getInstance(), "nodeName");
        if (!meta.getPersistentDataContainer().has(name)) return;
        String nodeName = meta.getPersistentDataContainer().get(name, PersistentDataType.STRING);
        Tree treeNode = faction.getTree().get(nodeName);
        if (treeNode == null) return;
        ClickType type = e.getClick();
        if (!type.equals(ClickType.LEFT) && !type.equals(ClickType.RIGHT)) return;
        if (type.equals(ClickType.RIGHT) || (p.hasPermission("relic-factions.command.research.others") && (user.getFaction() == null || !user.getFaction().equals(faction.getUUID())))) {
            if (p.hasPermission("relic-factions.command.research.others")) {
                if (!user.isTreeForceOnce()) {
                    user.setTreeForceOnce(true);
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.research.force.confirm"))));
                    return;
                }
                for (Tree tree : faction.getTree().values()) {
                    if (!tree.getGroup().equals(treeNode.getGroup())) continue;
                    if (tree.getLevel() > treeNode.getLevel()) continue;
                    tree.setUnlocked(true);
                }
                faction.setTree(faction.getTree()); // Just to update modified
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.research.force.success")).replace("%node%", treeNode.getName()).replace("%faction%", faction.getName())));
                p.closeInventory();
                p.openInventory(ResearchCommand.getTree(faction));
                user.setTreeForceOnce(false);
                user.setOpenedTreeFaction(faction.getUUID());
                return;
            }
        }

        if (!p.hasPermission("relic-factions.command.research.others") && (user.getFaction() == null || !user.getFaction().equals(faction.getUUID()))) {
            p.closeInventory();
            return;
        }

        if (faction.getPoints() < treeNode.getPoints()) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.research.normal.not-points"))));
            return;
        }

        int lowestLevel = 0;
        for (Tree tree : faction.getTree().values()) {
            if (!tree.getGroup().equals(treeNode.getGroup())) return;
            if (tree.getLevel() < treeNode.getLevel()) continue;
            if (!tree.isUnlocked()) continue;
            if (tree.getLevel() > lowestLevel) lowestLevel = tree.getLevel();
        }

        if (Math.abs(lowestLevel - treeNode.getLevel()) > 1) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.research.normal.cannot-skip"))));
            return;
        }

        if (!user.isTreeOnce()) {
            user.setTreeOnce(true);
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.research.normal.confirm"))));
            return;
        }


        user.setTreeOnce(false);
        treeNode.setUnlocked(true);
        HashMap<String, Tree> tree = faction.getTree();
        tree.put(treeNode.getName(), treeNode);
        faction.setTree(tree);
        if (Main.getInstance().getConfig().getBoolean("research.lose-points-on.unlock")) faction.setPoints(faction.getPoints() - treeNode.getPoints());
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.research.normal.success")).replace("%node%", treeNode.getName()).replace("%points%", treeNode.getPoints() + "")));
        p.closeInventory();
        p.openInventory(ResearchCommand.getTree(faction));
        user.setOpenedTreeFaction(faction.getUUID());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        User user = User.get(p);
        if (!C.serialize(e.getView().title()).equalsIgnoreCase(C.chat(Objects.requireNonNull(Inventories.get().getString("tree.name"))))) return;
        user.setOpenedTreeFaction(null);
        user.setTreeOnce(false);
        user.setTreeForceOnce(false);
    }
}
