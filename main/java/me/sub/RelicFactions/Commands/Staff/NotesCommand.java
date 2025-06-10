package me.sub.RelicFactions.Commands.Staff;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.Note;
import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Files.Normal.Inventories;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Permission;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class NotesCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }

        if (!Permission.has(p, "notes", "staff")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
            return true;
        }

        if (!Main.getInstance().getConfig().getBoolean("features.notes")) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.feature-disabled"))));
            return true;
        }

        if (args.length < 2) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.notes.usage"))));
            return true;
        }

        if (!args[0].equalsIgnoreCase("view") && !args[0].equalsIgnoreCase("add") && !args[0].equalsIgnoreCase("remove")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.notes.usage"))));
            return true;
        }

        User user = User.get(args[1]);
        if (user == null) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
            return true;
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("view")) {
                if (user.getNotes().isEmpty()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.notes.none"))));
                    return true;
                }
                p.openInventory(getInventory(user, 1));
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.notes.view.success")).replace("%page%", "1").replace("%player%", user.getName())));
                return true;
            }
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.notes.usage"))));
            return true;
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("view")) {
                int page;
                try {
                    page = Integer.parseInt(args[2]);
                } catch (NumberFormatException ignored) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.notes.view.invalid-page"))));
                    return true;
                }
                if (page < 1 || user.getNotesOnPage(page).isEmpty()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.notes.view.invalid-page"))));
                    return true;
                }
                p.openInventory(getInventory(user, page));
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.notes.view.success")).replace("%page%", page + "").replace("%player%", user.getName())));
                return true;
            }
            if (args[0].equalsIgnoreCase("remove")) {
                int id;
                try {
                    id = Integer.parseInt(args[2]);
                } catch (NumberFormatException ignored) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.notes.remove.invalid-id"))));
                    return true;
                }
                if (id < 1 || id > user.getNotes().size()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.notes.remove.invalid-id"))));
                    return true;
                }

                ArrayList<Note> notes = user.getNotes();
                notes.removeIf(note -> note.getID() == id);

                ArrayList<Note> newNotes = new ArrayList<>();

                for (int i = 0; i < notes.size(); i++) {
                    notes.get(i).setID(i + 1);
                    newNotes.add(notes.get(i));
                }

                user.setNotes(newNotes);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.notes.remove.success")).replace("%id%", id + "").replace("%player%", user.getName())));
                return true;
            }
        }

        if (!args[0].equalsIgnoreCase("add")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.notes.usage"))));
            return true;
        }

        String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        text = C.strip(text);
        Note note = new Note(user.getNotes().size() + 1, p.getUniqueId(), text);
        ArrayList<Note> notes = user.getNotes();
        notes.add(note);
        user.setNotes(notes);
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.notes.add.success")).replace("%player%", user.getName()).replace("%text%", text)));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String alias,
            @NotNull String @NotNull [] args
    ) {
        if (!(sender instanceof Player p)) return List.of();

        if (!Permission.has(p, "notes", "staff")) {
            return List.of();
        }

        List<String> subcommands = List.of("view", "add", "remove");

        if (args.length == 1) {
            return subcommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }

        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            User user = User.get(args[1]);
            if (user == null) return List.of();

            if (sub.equals("view")) {
                int maxPages = Math.max(1, (user.getNotes().size() + 35) / 36);
                List<String> pages = new ArrayList<>();
                for (int i = 1; i <= maxPages; i++) {
                    pages.add(String.valueOf(i));
                }
                return pages.stream()
                        .filter(page -> page.startsWith(args[2]))
                        .toList();
            } else if (sub.equals("remove")) {
                List<String> ids = new ArrayList<>();
                for (Note note : user.getNotes()) {
                    ids.add(String.valueOf(note.getID()));
                }
                return ids.stream()
                        .filter(id -> id.startsWith(args[2]))
                        .toList();
            }
        }
        return List.of();
    }

    public static Inventory getInventory(User user, int page) {
        Inventory inventory = Bukkit.createInventory(
                null,
                54,
                Component.text(C.chat(Objects.requireNonNull(Inventories.get().getString("notes-view.name"))))
        );

        ArrayList<Note> notes = user.getNotes();
        int maxPages = (notes.size() + 35) / 36;

        if (page < 1 || page > maxPages) {
            throw new RuntimeException("Specified amount of pages is invalid.");
        }

        ItemStack pageItem = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(Inventories.get().getString("notes-view.items.page.item")))));
        ItemMeta pageMeta = pageItem.getItemMeta();
        pageMeta.displayName(Component.text(C.chat(Objects.requireNonNull(Inventories.get().getString("notes-view.items.page.name")).replace("%page%", page + ""))));
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "notesPage");
        NamespacedKey userKey = new NamespacedKey(Main.getInstance(), "userKey");
        pageMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, page);
        pageItem.setItemMeta(pageMeta);
        inventory.setItem(Inventories.get().getInt("notes-view.items.page.slot"), pageItem);

        if (page != maxPages) {
            ItemStack next = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(Inventories.get().getString("notes-view.items.next.item")))));
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.displayName(Component.text(C.chat(Objects.requireNonNull(Inventories.get().getString("notes-view.items.next.name")))));
            NamespacedKey nextKey = new NamespacedKey(Main.getInstance(), "nextPage");
            nextMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, page);
            nextMeta.getPersistentDataContainer().set(nextKey, PersistentDataType.BOOLEAN, true);
            nextMeta.getPersistentDataContainer().set(userKey, PersistentDataType.STRING, user.getUUID().toString());
            next.setItemMeta(nextMeta);
            inventory.setItem(Inventories.get().getInt("notes-view.items.next.slot"), next);
        }

        if (page > 1) {
            ItemStack previous = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(Inventories.get().getString("notes-view.items.previous.item")))));
            ItemMeta previousMeta = previous.getItemMeta();
            previousMeta.displayName(Component.text(C.chat(Objects.requireNonNull(Inventories.get().getString("notes-view.items.previous.name")))));
            NamespacedKey nextKey = new NamespacedKey(Main.getInstance(), "previousPage");
            previousMeta.getPersistentDataContainer().set(nextKey, PersistentDataType.BOOLEAN, true);
            previousMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, page);
            previousMeta.getPersistentDataContainer().set(userKey, PersistentDataType.STRING, user.getUUID().toString());
            previous.setItemMeta(previousMeta);
            inventory.setItem(Inventories.get().getInt("notes-view.items.previous.slot"), previous);
        }

        List<Note> fit = user.getNotesOnPage(page);
        for (int i = 0; i < fit.size(); i++) {
            Note note = fit.get(i);
            ItemStack noteItem = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(Inventories.get().getString("notes-view.items.note.item")))));
            ItemMeta noteMeta = noteItem.getItemMeta();
            ArrayList<Component> lore = new ArrayList<>();
            for (String s : Inventories.get().getStringList("notes-view.items.note.lore")){
                if (s.contains("%id%")) s = s.replace("%id%", note.getID() + 1 + "");
                if (s.contains("%timestamp%")) s = s.replace("%timestamp%", Timer.formatTimestamp(note.getTimeStamp()));
                if (s.contains("%text%")) s = s.replace("%text%", note.getText());
                if (s.contains("%giver%")) {
                    User giver = User.get(note.getNoteGiver());
                    s = s.replace("%giver%", giver != null ? giver.getName() : "Unknown");
                }
                lore.add(Component.text(C.chat(s)));
            }
            noteMeta.lore(lore);
            noteMeta.displayName(Component.text(C.chat(Objects.requireNonNull(Inventories.get().getString("notes-view.items.note.name")).replace("%id%", note.getID() + ""))));
            noteItem.setItemMeta(noteMeta);
            inventory.setItem(i, noteItem);
        }

        ItemStack fill = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(Inventories.get().getString("filler.item")))));
        ItemMeta meta = fill.getItemMeta();
        if (meta == null) return inventory;
        meta.displayName(Component.text(C.chat("&e")));
        fill.setItemMeta(meta);

        if (Inventories.get().getBoolean("filler.enabled")) {
            for (int i = 0; i < inventory.getSize(); i++) {
                if (i < 36) continue;
                if (inventory.getItem(i) != null) continue;
                inventory.setItem(i, fill);
            }
        }

        return inventory;
    }
}
