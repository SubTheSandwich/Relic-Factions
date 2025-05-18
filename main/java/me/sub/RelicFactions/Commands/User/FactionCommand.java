package me.sub.RelicFactions.Commands.User;

import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.FactionData;
import me.sub.RelicFactions.Files.Enums.FactionType;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Files.Normal.Messages;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Calculate;
import me.sub.RelicFactions.Utils.Maps;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FactionCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Locale.get().getString("primary.not-player")));
            return true;
        }
        User user = User.get(p);
        if (args.length == 0) {
            Messages.send(p, "faction.help.main", s);
            return true;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("who") || args[0].equalsIgnoreCase("show")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Locale.get().getString("primary.faction.none")));
                    return true;
                }
                show(p, Faction.get(user.getFaction()));
                return true;
            }
            if (args[0].equalsIgnoreCase("open")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Locale.get().getString("primary.faction.none")));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getRoleID(p.getUniqueId()) < 2) {
                    p.sendMessage(C.chat(Locale.get().getString("primary.faction.coleader-above")));
                    return true;
                }
                boolean open = !faction.isOpen();
                faction.setOpen(open);
                p.sendMessage(C.chat(Locale.get().getString("commands.faction.open.set").replace("%status%", open ? Locale.get().getString("commands.faction.open.open").toLowerCase() : Locale.get().getString("commands.faction.open.closed").toLowerCase())));
                for (Player player : faction.getOnlineMembers()) {
                    player.sendMessage(C.chat(Locale.get().getString("commands.faction.open.bc").replace("%player%", p.getName()).replace("%status%", open ? Locale.get().getString("commands.faction.open.open").toLowerCase() : Locale.get().getString("commands.faction.open.closed").toLowerCase())));
                }
                return true;
            }
            Messages.send(p, "faction.help.main", s);
            return true;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) {
                if (user.getFaction() != null) {
                    p.sendMessage(C.chat(Locale.get().getString("commands.faction.have")));
                    return true;
                }
                Faction faction = Faction.get(args[1]);
                if (faction != null) {
                    p.sendMessage(C.chat(Locale.get().getString("commands.faction.exists")));
                    return true;
                }
                if (!args[1].matches("^[a-zA-Z0-9]+$")) {
                    p.sendMessage(C.chat(Locale.get().getString("commands.faction.not-alphanumeric")));
                    return true;
                }

                if (Main.getInstance().getConfig().getStringList("factions.name.blocked").contains(args[1].toUpperCase())) {
                    p.sendMessage(C.chat(Locale.get().getString("commands.faction.blocked")));
                    return true;
                }
                if (args[1].length() < Main.getInstance().getConfig().getInt("factions.name.min") || args[1].length() > Main.getInstance().getConfig().getInt("factions.name.max")) {
                    p.sendMessage(C.chat(Locale.get().getString("commands.faction.length").replace("%min%",  Main.getInstance().getConfig().getInt("factions.name.min") + "").replace("%max%",  Main.getInstance().getConfig().getInt("factions.name.max") + "")));
                    return true;
                }
                UUID uuid = UUID.randomUUID();
                HashMap<UUID, Integer> members = new HashMap<>();
                members.put(p.getUniqueId(), 3);
                FactionData factionData = new FactionData(uuid);
                factionData.setup();
                factionData.get().set("uuid", uuid.toString());
                factionData.get().set("name", args[1]);
                factionData.get().set("leader", p.getUniqueId().toString());
                factionData.get().set("type", "PLAYER");
                factionData.get().set("members", Maps.serialize(members));
                factionData.save();
                user.setFaction(uuid);
                faction = new Faction(factionData);
                Main.getInstance().factions.put(uuid, faction);
                Main.getInstance().factionNameHolder.put(args[1].toLowerCase(), faction);
                p.sendMessage(C.chat(Locale.get().getString("commands.faction.create.success").replace("%faction%", args[1])));
                Bukkit.broadcastMessage(C.chat(Locale.get().getString("commands.faction.create.bc").replace("%faction%", args[1]).replace("%player%", p.getName())));
                return true;
            }
            Messages.send(p, "faction.help.main", s);
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        List<String> values = new ArrayList<>();
        if (args.length == 1) {
            values.addAll(List.of("create", "open", "show"));
        }
        return values;
    }

    private void show(Player p, Faction faction) {
        User user = User.get(p);
        if (faction.getType().equals(FactionType.PLAYER)) {
            for (String s : Messages.get().getStringList("faction.show.player")) {
                if (s.contains("%name%")) s = s.replace("%name%", faction.getValidName(p));
                if (s.contains("%online%")) s = s.replace("%online%", faction.getOnlineMembers().size() + "");
                if (s.contains("%players%")) s = s.replace("%players%", faction.getMembers().size() + "");
                if (s.contains("%home%")) {
                    // TODO: Implement
                }
                if (s.contains("%leader%")) s = s.replace("%leader%", getName(faction.getLeader()));
                if (s.contains("%coleaders%")) {
                    ArrayList<String> names = new ArrayList<>();
                    for (Map.Entry<UUID, Integer> entry : faction.getMembers().entrySet()) {
                        UUID uuid = entry.getKey();
                        int role = entry.getValue();
                        if (role != 2) continue;
                        names.add(getName(uuid));
                    }
                    if (names.isEmpty()) continue;
                    s = s.replace("%coleaders%", String.join("&e, ", names));
                }
                if (s.contains("%captains%")) {
                    ArrayList<String> names = new ArrayList<>();
                    for (Map.Entry<UUID, Integer> entry : faction.getMembers().entrySet()) {
                        UUID uuid = entry.getKey();
                        int role = entry.getValue();
                        if (role != 1) continue;
                        names.add(getName(uuid));
                    }
                    if (names.isEmpty()) continue;
                    s = s.replace("%captains%", String.join("&e, ", names));
                }
                if (s.contains("%members%")) {
                    ArrayList<String> names = new ArrayList<>();
                    for (Map.Entry<UUID, Integer> entry : faction.getMembers().entrySet()) {
                        UUID uuid = entry.getKey();
                        int role = entry.getValue();
                        if (role != 0) continue;
                        names.add(getName(uuid));
                    }
                    if (names.isEmpty()) continue;
                    s = s.replace("%members%", String.join("&e, ", names));
                }
                if (s.contains("%balance%")) s = s.replace("%balance%", Calculate.formatMoney(faction.getBalance().doubleValue()));
                if (s.contains("%points%")) s = s.replace("%points%", faction.getPoints() + "");
                if (s.contains("%kothcaptures%")) s = s.replace("%kothcaptures%", faction.getKothCaptures() + "");
                if (s.contains("%dtr%")) {
                    // TODO: Implement
                }
                if (s.contains("%regen%")) {
                    // TODO: Implement
                }
                if (s.contains("%open%")) {
                    if (!faction.isOpen()) continue;
                    s = s.replace("%open%", Locale.get().getString("commands.faction.open.open"));
                }
                if (s.contains("%lives%")) {
                    if (!user.hasFaction() || !user.getFaction().equals(faction.getUUID())) continue;
                    s = s.replace("%lives%", faction.getLives() + "");
                }
                if (s.contains("%announcement%")) {
                    if (!user.hasFaction() || !user.getFaction().equals(faction.getUUID())) continue;
                    s = s.replace("%announcement%", faction.getAnnouncement() == null ? Locale.get().getString("primary.none") : faction.getAnnouncement());
                }
                p.sendMessage(C.chat(s));
            }
        } else {

        }
    }

    private String getName(UUID uuid) {
        User user = User.get(uuid);
        if (user.isDeathBanned()) {
            return C.chat("&c" + user.getName() + "&7[&c" + user.getKills() + "&7]");
        } else {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                return C.chat("&7" + user.getName() + "&7[&c" + user.getKills() + "&7]");
            } else {
                return C.chat("&a" + user.getName() + "&7[&c" + user.getKills() + "&7]");
            }
        }
    }
}
