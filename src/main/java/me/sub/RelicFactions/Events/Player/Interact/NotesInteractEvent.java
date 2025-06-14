package me.sub.RelicFactions.Events.Player.Interact;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Normal.Inventories;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.UUID;

public class NotesInteractEvent implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getClickedInventory() == null) return;
        if (!C.serialize(e.getView().title()).equalsIgnoreCase(C.chat(Objects.requireNonNull(Inventories.get().getString("notes-view.name")))))
            return;
        if (e.getCurrentItem() == null) return;
        if (!e.getCurrentItem().hasItemMeta()) return;
        e.setCancelled(true);
        ItemStack item = e.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        if (meta.getPersistentDataContainer().isEmpty()) return;
        NamespacedKey notesPageKey = new NamespacedKey(Main.getInstance(), "notesPage");
        NamespacedKey nextKey = new NamespacedKey(Main.getInstance(), "nextPage");
        NamespacedKey previousKey = new NamespacedKey(Main.getInstance(), "previousPage");
        NamespacedKey userKey = new NamespacedKey(Main.getInstance(), "userKey");

        if (meta.getPersistentDataContainer().has(notesPageKey, PersistentDataType.INTEGER) &&
                meta.getPersistentDataContainer().has(nextKey, PersistentDataType.BOOLEAN) &&
                meta.getPersistentDataContainer().has(userKey, PersistentDataType.STRING)) {

            Integer page = meta.getPersistentDataContainer().get(notesPageKey, PersistentDataType.INTEGER);
            String userString = meta.getPersistentDataContainer().get(userKey, PersistentDataType.STRING);

            if (page != null && userString != null) {
                int pageNumber = page;
                try {
                    UUID userUUID = UUID.fromString(userString);
                    User user = User.get(userUUID);
                    if (user == null) {
                        throw new RuntimeException("Invalid UUID string in userKey: " + userString);
                    }
                    p.performCommand("notes view " + user.getName() + " " + (pageNumber + 1));

                } catch (IllegalArgumentException ev) {
                    throw new RuntimeException("Invalid UUID string in userKey: " + userString, ev);
                }
            } else {
                throw new RuntimeException("Failed to find page number or user from notes menu.");
            }
        }

        if (meta.getPersistentDataContainer().has(notesPageKey, PersistentDataType.INTEGER) &&
                meta.getPersistentDataContainer().has(previousKey, PersistentDataType.BOOLEAN) &&
                meta.getPersistentDataContainer().has(userKey, PersistentDataType.STRING)) {

            Integer page = meta.getPersistentDataContainer().get(notesPageKey, PersistentDataType.INTEGER);
            String userString = meta.getPersistentDataContainer().get(userKey, PersistentDataType.STRING);

            if (page != null && userString != null) {
                int pageNumber = page;
                try {
                    UUID userUUID = UUID.fromString(userString);
                    User user = User.get(userUUID);
                    if (user == null) {
                        throw new RuntimeException("Invalid UUID string in userKey: " + userString);
                    }
                    p.performCommand("notes view " + user.getName() + " " + (pageNumber - 1));

                } catch (IllegalArgumentException ev) {
                    throw new RuntimeException("Invalid UUID string in userKey: " + userString, ev);
                }
            } else {
                throw new RuntimeException("Failed to find page number or user from notes menu.");
            }
        }
    }
}
