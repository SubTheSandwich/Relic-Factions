package me.sub.RelicFactions.Events.Player.Interact;

import me.sub.RelicFactions.Commands.User.ResearchCommand;
import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.TreeHandler;
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
        TreeHandler treeHandler = user.getTreeHandler();
        if (treeHandler.getFaction() == null) {
            p.closeInventory();
            return;
        }
        Faction faction = Faction.get(treeHandler.getFaction());
        if (faction == null) {
            p.closeInventory();
            return;
        }
        if (user.getFaction() == null && !p.hasPermission("relic.command.research.others")) {
            p.closeInventory();
            return;
        }
        if (user.getFaction() != null && !user.getFaction().equals(treeHandler.getFaction()) && !p.hasPermission("relic.command.research.others")) {
            p.closeInventory();
            return;
        }
        NamespacedKey name = new NamespacedKey(Main.getInstance(), "nodeName");
        NamespacedKey remove = new NamespacedKey(Main.getInstance(), "nodeRemoveName");
        if (!meta.getPersistentDataContainer().has(name) && !meta.getPersistentDataContainer().has(remove)) return;
        String nodeName = meta.getPersistentDataContainer().get(name, PersistentDataType.STRING);
        String nodeRemoveName = meta.getPersistentDataContainer().get(remove, PersistentDataType.STRING);
        ClickType type = e.getClick();
        if (!type.equals(ClickType.LEFT) && !type.equals(ClickType.RIGHT) && !type.equals(ClickType.MIDDLE)) return;
        if (type.equals(ClickType.MIDDLE)) {
            if (!meta.getPersistentDataContainer().has(remove)) return;
            if (!p.hasPermission("relic.command.research.others")) return;
            Tree treeNode = faction.getTree().get(nodeRemoveName);
            if (treeNode == null) return;
            if (!treeHandler.isTreeRemoveOne()) {
                treeHandler.setTreeRemoveOne(true);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.research.remove.confirm"))));
                return;
            }
            for (Tree tree : faction.getTree().values()) {
                if (!tree.getGroup().equals(treeNode.getGroup())) continue;
                if (tree.getLevel() < treeNode.getLevel()) continue;
                tree.setUnlocked(false);
            }
            faction.setTree(faction.getTree()); // Just to update modified
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.research.remove.success")).replace("%node%", treeNode.getName()).replace("%faction%", faction.getName())));
            p.closeInventory();
            p.openInventory(ResearchCommand.getTree(faction));
            treeHandler = new TreeHandler();
            treeHandler.setFaction(faction.getUUID());
            user.setTreeHandler(treeHandler);
            return;
        }
        if (!meta.getPersistentDataContainer().has(name)) return;
        Tree treeNode = faction.getTree().get(nodeName);
        if (treeNode == null) return;
        if (type.equals(ClickType.RIGHT) || (p.hasPermission("relic.command.research.others") && (user.getFaction() == null || !user.getFaction().equals(faction.getUUID())))) {
            if (p.hasPermission("relic.command.research.others")) {
                if (!treeHandler.isTreeForceOnce()) {
                    treeHandler.setTreeForceOnce(true);
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
                treeHandler = new TreeHandler();
                treeHandler.setFaction(faction.getUUID());
                user.setTreeHandler(treeHandler);
                return;
            }
        }

        if (!p.hasPermission("relic.command.research.others") && (user.getFaction() == null || !user.getFaction().equals(faction.getUUID()))) {
            p.closeInventory();
            return;
        }

        if (faction.getPoints() < treeNode.getPoints()) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.research.normal.not-points"))));
            return;
        }

        int highestUnlockedLevel = 0;
        for (Tree tree : faction.getTree().values()) {
            if (!tree.getGroup().equals(treeNode.getGroup())) continue; // Only process same group
            if (!tree.isUnlocked()) continue;
            if (tree.getLevel() > highestUnlockedLevel) highestUnlockedLevel = tree.getLevel();
        }

        if (Math.abs(highestUnlockedLevel - treeNode.getLevel()) > 1) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.research.normal.cannot-skip"))));
            return;
        }

        if (!treeHandler.isTreeOnce()) {
            treeHandler.setTreeOnce(true);
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.research.normal.confirm"))));
            return;
        }


        treeHandler.setTreeOnce(false);
        treeNode.setUnlocked(true);
        HashMap<String, Tree> tree = faction.getTree();
        tree.put(treeNode.getName(), treeNode);
        faction.setTree(tree);
        if (Main.getInstance().getConfig().getBoolean("research.lose-points-on.unlock")) faction.setPoints(faction.getPoints() - treeNode.getPoints());
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.research.normal.success")).replace("%node%", treeNode.getName()).replace("%points%", treeNode.getPoints() + "")));
        p.closeInventory();
        p.openInventory(ResearchCommand.getTree(faction));
        treeHandler = new TreeHandler();
        treeHandler.setFaction(faction.getUUID());
        user.setTreeHandler(treeHandler);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        User user = User.get(p);
        if (!C.serialize(e.getView().title()).equalsIgnoreCase(C.chat(Objects.requireNonNull(Inventories.get().getString("tree.name"))))) return;
        user.setTreeHandler(new TreeHandler());
    }
}
