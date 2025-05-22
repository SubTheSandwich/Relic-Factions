package me.sub.RelicFactions.Commands.User;

import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.Claim;
import me.sub.RelicFactions.Files.Data.Cuboid;
import me.sub.RelicFactions.Files.Data.FactionData;
import me.sub.RelicFactions.Files.Data.PlayerTimer;
import me.sub.RelicFactions.Files.Enums.Color;
import me.sub.RelicFactions.Files.Enums.FactionType;
import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Files.Normal.Messages;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Calculate;
import me.sub.RelicFactions.Utils.Maps;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.*;

public class FactionCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.not-player"))));
            return true;
        }
        User user = User.get(p);
        if (args.length == 0) {
            Messages.send(p, "faction.help.main", s);
            return true;
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("announcement")) {
            if (!user.hasFaction()) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                return true;
            }
            Faction faction = Faction.get(user.getFaction());
            if (faction.getRoleID(p.getUniqueId()) < 2) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.coleader-above"))));
                return true;
            }
            String announcement = C.strip(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
            faction.setAnnouncement(announcement);
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.announcement")).replace("%announcement%", announcement)));
            return true;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("lives")) {
                Messages.send(p, "faction.help.lives", s);
                return true;
            }
            if (args[0].equalsIgnoreCase("map")) {
                if (user.getMap() == null) {
                    Map<Faction, List<Cuboid>> nearbyClaims = Faction.getNearbyClaims(
                            p.getLocation(),
                            Main.getInstance().getConfig().getInt("factions.map-radius")
                    );
                    Map<Faction, Material> pillars = new HashMap<>();
                    for (Map.Entry<Faction, List<Cuboid>> entry : nearbyClaims.entrySet()) {
                        Faction faction = entry.getKey();
                        List<Cuboid> claims = entry.getValue();
                        if (claims.isEmpty()) continue;
                        Material material = Claim.getPillarMaterial();
                        pillars.put(faction, material);
                    }
                    if (pillars.isEmpty()) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.map.none"))));
                        return true;
                    }
                    showMapPillars(p, nearbyClaims, pillars);
                    for (Map.Entry<Faction,Material> entry : pillars.entrySet()) {
                        Faction faction = entry.getKey();
                        Material material = entry.getValue();
                        p.sendMessage(C.chat(Objects.requireNonNull(Objects.requireNonNull(Locale.get().getString("commands.faction.map.format")).replace("%faction%", faction.getName()).replace("%material%", material.name()))));
                    }
                    user.setMap((HashMap<Faction, List<Cuboid>>) nearbyClaims);
                } else {
                    clearMapPillars(p, user.getMap());
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.map.disable"))));
                    user.setMap(null);
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("disband")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getRoleID(p.getUniqueId()) != 3) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.leader"))));
                    return true;
                }
                if (faction.getDTR().doubleValue() <= 0) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.disband.raidable"))));
                    return true;
                }
                for (Player player : faction.getOnlineMembers()) {
                    player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.disband.success")).replace("%player%", p.getName())));
                }
                for (UUID uuid : faction.getMembers().keySet()) {
                    User member = User.get(uuid);
                    member.setFaction(null);
                }
                faction.getFactionData().delete();
                Main.getInstance().factionNameHolder.remove(faction.getName().toLowerCase());
                Main.getInstance().factions.remove(faction.getUUID());
                return true;
            }
            if (args[0].equalsIgnoreCase("list")) {
                ArrayList<Faction> factions = Faction.getPage(1);
                if (factions.isEmpty()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.list.none"))));
                    return true;
                }
                int page = 1;
                int maxPages = Faction.getMaxPages();
                for (String message : Messages.get().getStringList("faction.list.show")) {
                    if (message.contains("%page%")) message = message.replace("%page%", page + "");
                    if (message.contains("%max-pages%")) message = message.replace("%max-pages%", maxPages + "");
                    if (message.contains("%teams%")) {
                        for (int i = 0; i < factions.size(); i++) {
                            Faction faction = factions.get(i);
                            String format = Messages.get().getString("faction.list.team-format");
                            format = Objects.requireNonNull(format).replace("%number%", (i + 1) + "");
                            format = format.replace("%faction-name%", faction.getName());
                            format = format.replace("%online-members%", faction.getOnlineMembers().size() + "");
                            format = format.replace("%members%", faction.getMembers().size() + "");
                            p.sendMessage(C.chat(format, s));
                        }
                        continue;
                    }
                    p.sendMessage(C.chat(message, s));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("leave")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (user.getClaim() != null && user.getClaim().getUUID().equals(faction.getUUID())) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.leave.claiming"))));
                    return true;
                }
                if (faction.getLeader().equals(p.getUniqueId())) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.leave.leader")).replace("%alias%", s)));
                    return true;
                }
                if (faction.getDTR().doubleValue() <= 0) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.leave.raidable"))));
                    return true;
                }
                user.setFaction(null);
                HashMap<UUID, Integer> members = faction.getMembers();
                members.remove(p.getUniqueId());
                faction.setMembers(members);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.leave.success"))));
                for (Player player : faction.getOnlineMembers()) {
                    player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.leave.bc")).replace("%player%", p.getName())));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("subclaim")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Messages.send(p, "faction.help.subclaim", s);
                return true;
            }
            if (args[0].equalsIgnoreCase("who") || args[0].equalsIgnoreCase("show")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                show(p, Faction.get(user.getFaction()));
                return true;
            }
            if (args[0].equalsIgnoreCase("bypass")) {
                if (!Permission.has(p, "faction.bypass")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                    return true;
                }
                user.setFactionBypass(!user.isFactionBypass());
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.bypass")).replace("%status%", Objects.requireNonNull(user.isFactionBypass() ? Locale.get().getString("primary.enabled") : Locale.get().getString("primary.disabled")))));
                return true;
            }
            if (args[0].equalsIgnoreCase("open")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getRoleID(p.getUniqueId()) < 2) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.coleader-above"))));
                    return true;
                }
                boolean open = !faction.isOpen();
                faction.setOpen(open);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.open.set")).replace("%status%", open ? Objects.requireNonNull(Locale.get().getString("commands.faction.open.open")).toLowerCase() : Objects.requireNonNull(Locale.get().getString("commands.faction.open.closed")).toLowerCase())));
                for (Player player : faction.getOnlineMembers()) {
                    player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.open.bc")).replace("%player%", p.getName()).replace("%status%", open ? Objects.requireNonNull(Locale.get().getString("commands.faction.open.open")).toLowerCase() : Objects.requireNonNull(Locale.get().getString("commands.faction.open.closed")).toLowerCase())));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("unclaim")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getRoleID(p.getUniqueId()) < 2) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.coleader-above"))));
                    return true;
                }
                if (faction.getClaims().isEmpty()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.unclaim.none"))));
                    return true;
                }
                for (Player player : faction.getOnlineMembers()) {
                    player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.unclaim.success")).replace("%player%", p.getName())));
                }
                faction.setClaims(new ArrayList<>());
                return true;
            }
            if (args[0].equalsIgnoreCase("ff")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getRoleID(p.getUniqueId()) < 2) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.coleader-above"))));
                    return true;
                }
                faction.setFF(!faction.isFF());
                String status = faction.isFF() ? Objects.requireNonNull(Locale.get().getString("primary.enabled")).toLowerCase() : Objects.requireNonNull(Locale.get().getString("primary.disabled")).toLowerCase();
                for (Player player : faction.getOnlineMembers()) {
                    player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.ff")).replace("%player%", p.getName()).replace("%status%", status)));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("captain")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getRoleID(p.getUniqueId()) < 2) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.coleader-above"))));
                    return true;
                }
                Messages.send(p, "faction.help.captain", s);
                return true;
            }
            if (args[0].equalsIgnoreCase("coleader")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getRoleID(p.getUniqueId()) != 3) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.leader"))));
                    return true;
                }
                Messages.send(p, "faction.help.coleader", s);
                return true;
            }
            if (args[0].equalsIgnoreCase("invites")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getRoleID(p.getUniqueId()) == 0) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.captain-above"))));
                    return true;
                }
                String message = Locale.get().getString("commands.faction.invites");
                ArrayList<String> names = new ArrayList<>();
                for (UUID uuid : faction.getInvites()) {
                    names.add(User.get(uuid).getName());
                }
                if (names.isEmpty()) {
                    message = Objects.requireNonNull(message).replace("%invites%", Objects.requireNonNull(Locale.get().getString("primary.none")));
                } else {
                    message = Objects.requireNonNull(message).replace("%invites%", String.join("&e, ", names));
                }
                p.sendMessage(C.chat(message));
                return true;
            }
            if (args[0].equalsIgnoreCase("sethome")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getRoleID(p.getUniqueId()) == 0) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.captain-above"))));
                    return true;
                }
                if (Faction.getAt(p.getLocation()) == null || !Objects.requireNonNull(Faction.getAt(p.getLocation())).getUUID().equals(faction.getUUID())) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.sethome.claim"))));
                    return true;
                }
                faction.setHome(p.getLocation());
                for (Player player : faction.getOnlineMembers()) {
                    player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.sethome.success")).replace("%player%", p.getName())));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("claim")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                if (user.hasTimer("pvp") || user.hasTimer("starting")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.timer.cannot-claim"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getRoleID(p.getUniqueId()) == 0) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.captain-above"))));
                    return true;
                }
                if (!faction.getClaims().isEmpty()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.have"))));
                    return true;
                }
                if (user.getClaim() != null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.already"))));
                    return true;
                }
                if (p.getInventory().firstEmpty() == -1) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.inventory-full"))));
                    return true;
                }
                Claim claim = new Claim(faction.getUUID());
                user.setClaim(claim);
                p.getInventory().addItem(Claim.getWand());
                Messages.send(p, "faction.help.claiming");
                return true;
            }
            if (args[0].equalsIgnoreCase("home")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getHome() == null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.home.none"))));
                    return true;
                }

                if (user.hasTimer("pvp") || user.hasTimer("starting")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.home.pvp-timer"))));
                    return true;
                }

                if (user.hasTimer("combat")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.home.spawn-timer"))));
                    return true;
                }

                int duration = Main.getInstance().getConfig().getInt("timers.home");
                if (p.getWorld().getEnvironment().equals(World.Environment.NETHER)) {
                    duration = Main.getInstance().getConfig().getInt("factions.home.nether");
                } else if (p.getWorld().getEnvironment().equals(World.Environment.THE_END)) {
                    if (Main.getInstance().getConfig().getBoolean("factions.home.disable-in-end")) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.home.end"))));
                        return true;
                    }
                    duration = Main.getInstance().getConfig().getInt("factions.home.end");
                }
                PlayerTimer timer = new PlayerTimer(p.getUniqueId(), Timer.HOME, BigDecimal.valueOf(duration));
                user.addTimer(timer);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.home.waiting")).replace("%time%", duration + "")));
                return true;
            }

            Messages.send(p, "faction.help.main", s);
            return true;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("lives")) {
                Messages.send(p, "faction.help.lives", s);
                return true;
            }
            if (args[0].equalsIgnoreCase("revive")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getLives() == 0) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.lives.not-enough"))));
                    return true;
                }
                User revived = User.get(args[1]);
                if (!revived.isDeathBanned()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.lives.not-deathbanned"))));
                    return true;
                }
                if (!revived.hasFaction() || !revived.getFaction().equals(user.getFaction())) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.revive.not-in"))));
                    return true;
                }
                faction.setLives(faction.getLives() - 1);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.revive.success")).replace("%player%", revived.getName())));
                revived.setDeathBanned(false);
                return true;
            }
            if (args[0].equalsIgnoreCase("rename")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getRoleID(p.getUniqueId()) != 3) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.leader"))));
                    return true;
                }
                Faction other = Faction.get(args[1]);
                if (other != null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.exists"))));
                    return true;
                }
                if (!args[1].matches("^[a-zA-Z0-9]+$")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.not-alphanumeric"))));
                    return true;
                }

                if (Main.getInstance().getConfig().getStringList("factions.name.blocked").contains(args[1].toUpperCase())) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.blocked"))));
                    return true;
                }
                if (args[1].length() < Main.getInstance().getConfig().getInt("factions.name.min") || args[1].length() > Main.getInstance().getConfig().getInt("factions.name.max")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.length")).replace("%min%",  Main.getInstance().getConfig().getInt("factions.name.min") + "").replace("%max%",  Main.getInstance().getConfig().getInt("factions.name.max") + "")));
                    return true;
                }
                Main.getInstance().factionNameHolder.remove(faction.getName().toLowerCase());
                faction.setName(args[1]);
                Main.getInstance().factionNameHolder.put(args[1].toLowerCase(), faction);
                for (Player player : faction.getOnlineMembers()) {
                    player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.rename")).replace("%player%", p.getName()).replace("%faction%", args[1])));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("list")) {
                try {
                    int ignored = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                    return true;
                }
                int page = Integer.parseInt(args[1]);
                ArrayList<Faction> factions = Faction.getPage(page);
                if (factions.isEmpty()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.list.none"))));
                    return true;
                }
                int maxPages = Faction.getMaxPages();
                for (String message : Messages.get().getStringList("faction.list.show")) {
                    if (message.contains("%page%")) message = message.replace("%page%", page + "");
                    if (message.contains("%max-pages%")) message = message.replace("%max-pages%", maxPages + "");
                    if (message.contains("%teams%")) {
                        for (int i = 0; i < factions.size(); i++) {
                            Faction faction = factions.get(i);
                            String format = Messages.get().getString("faction.list.team-format");
                            format = Objects.requireNonNull(format).replace("%number%", (i + 1) + "");
                            format = format.replace("%faction-name%", faction.getName());
                            format = format.replace("%online-members%", faction.getOnlineMembers().size() + "");
                            format = format.replace("%members%", faction.getMembers().size() + "");
                            p.sendMessage(C.chat(format, s));
                        }
                        continue;
                    }
                    p.sendMessage(C.chat(message, s));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("claimfor")) {
                if (!Permission.has(p, "faction.claimfor")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                    return true;
                }
                Faction faction = Faction.get(args[1]);
                if (faction == null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.faction-ony"))));
                    return true;
                }
                if (faction.getType().equals(FactionType.PLAYER)) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.wrong-type"))));
                    return true;
                }
                if (user.getClaim() != null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.already"))));
                    return true;
                }
                if (p.getInventory().firstEmpty() == -1) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.inventory-full"))));
                    return true;
                }
                Claim claim = new Claim(faction.getUUID());
                user.setClaim(claim);
                p.getInventory().addItem(Claim.getWand());
                Messages.send(p, "faction.help.claiming");
                return true;
            }
            if (args[0].equalsIgnoreCase("captain")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getRoleID(p.getUniqueId()) < 2) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.coleader-above"))));
                    return true;
                }
                if (args[1].equalsIgnoreCase("list")) {
                    String message = Locale.get().getString("commands.faction.captain.list");
                    ArrayList<String> names = new ArrayList<>();
                    for (Map.Entry<UUID, Integer> entry : faction.getMembers().entrySet()) {
                        UUID uuid = entry.getKey();
                        int role = entry.getValue();
                        if (role != 1) continue;
                        names.add(User.get(uuid).getName());
                    }
                    if (names.isEmpty()) {
                        message = Objects.requireNonNull(message).replace("%captains%", Objects.requireNonNull(Locale.get().getString("primary.none")));
                    } else {
                        message = Objects.requireNonNull(message).replace("%captains%", String.join("&e, ", names));
                    }
                    p.sendMessage(C.chat(message));
                    return true;
                }
                Messages.send(p, "faction.help.captain", s);
                return true;
            }
            if (args[0].equalsIgnoreCase("coleader")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getRoleID(p.getUniqueId()) < 2) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.leader"))));
                    return true;
                }
                if (args[1].equalsIgnoreCase("list")) {
                    String message = Locale.get().getString("commands.faction.coleader.list");
                    ArrayList<String> names = new ArrayList<>();
                    for (Map.Entry<UUID, Integer> entry : faction.getMembers().entrySet()) {
                        UUID uuid = entry.getKey();
                        int role = entry.getValue();
                        if (role != 2) continue;
                        names.add(User.get(uuid).getName());
                    }
                    if (names.isEmpty()) {
                        message = Objects.requireNonNull(message).replace("%coleaders%", Objects.requireNonNull(Locale.get().getString("primary.none")));
                    } else {
                        message = Objects.requireNonNull(message).replace("%coleaders%", String.join("&e, ", names));
                    }
                    p.sendMessage(C.chat(message));
                    return true;
                }
                Messages.send(p, "faction.help.coleader", s);
                return true;
            }
            if (args[0].equalsIgnoreCase("createsystem")) {
                if (!Permission.has(p, "faction.createsystem")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                    return true;
                }
                Faction faction = Faction.get(args[1]);
                if (faction != null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.exists"))));
                    return true;
                }
                if (!args[1].matches("^[a-zA-Z0-9]+$")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.not-alphanumeric"))));
                    return true;
                }

                if (Main.getInstance().getConfig().getStringList("factions.name.blocked").contains(args[1].toUpperCase())) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.blocked"))));
                    return true;
                }
                if (args[1].length() < Main.getInstance().getConfig().getInt("factions.name.min") || args[1].length() > Main.getInstance().getConfig().getInt("factions.name.max")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.length")).replace("%min%",  Main.getInstance().getConfig().getInt("factions.name.min") + "").replace("%max%",  Main.getInstance().getConfig().getInt("factions.name.max") + "")));
                    return true;
                }
                UUID uuid = UUID.randomUUID();
                FactionData factionData = new FactionData(uuid);
                factionData.setup();
                factionData.get().set("uuid", uuid.toString());
                factionData.get().set("name", args[1]);
                factionData.get().set("type", FactionType.SYSTEM.name());
                factionData.get().set("color", Color.WHITE.name());
                factionData.get().set("deathban", false);
                factionData.save();
                faction = new Faction(factionData);
                Main.getInstance().factions.put(uuid, faction);
                Main.getInstance().factionNameHolder.put(args[1].toLowerCase(), faction);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.createsystem.success")).replace("%faction%", args[1])));
                Bukkit.broadcastMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.createsystem.bc")).replace("%faction%", args[1]).replace("%player%", p.getName())));
                return true;
            }
            if (args[0].equalsIgnoreCase("who") || args[0].equalsIgnoreCase("show")) {
                Faction faction = Faction.get(args[1].toLowerCase());
                if (faction != null) {
                    show(p, faction);
                    return true;
                }
                User play = User.get(args[1].toLowerCase());
                if (play != null) {
                    if (!play.hasFaction()) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.show.none"))));
                        return true;
                    }
                    show(p, Faction.get(play.getFaction()));
                    return true;
                }
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.doesnt-exist"))));
                return true;
            }
            if (args[0].equalsIgnoreCase("join")) {
                if (user.getFaction() != null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.have"))));
                    return true;
                }
                Faction faction = Faction.get(args[1]);
                if (faction == null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.doesnt-exist"))));
                    return true;
                }
                if (faction.getMembers().size() >= Main.getInstance().getConfig().getInt("factions.members.max")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.full"))));
                    return true;
                }
                if (!faction.isOpen()) {
                    if (!faction.getInvites().contains(user.getUUID())) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.join.not-invited"))));
                        return true;
                    }
                    faction.getInvites().remove(user.getUUID());
                }
                for (Player player : faction.getOnlineMembers()) {
                    player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.join.bc")).replace("%player%", p.getName())));
                }
                HashMap<UUID, Integer> members = faction.getMembers();
                members.put(p.getUniqueId(), 0);
                faction.setMembers(members);
                user.setFaction(faction.getUUID());
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.join.success")).replace("%faction%", faction.getName())));
                return true;
            }
            if (args[0].equalsIgnoreCase("create")) {
                if (user.getFaction() != null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.have"))));
                    return true;
                }
                Faction faction = Faction.get(args[1]);
                if (faction != null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.exists"))));
                    return true;
                }
                if (!args[1].matches("^[a-zA-Z0-9]+$")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.not-alphanumeric"))));
                    return true;
                }

                if (Main.getInstance().getConfig().getStringList("factions.name.blocked").contains(args[1].toUpperCase())) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.blocked"))));
                    return true;
                }
                if (args[1].length() < Main.getInstance().getConfig().getInt("factions.name.min") || args[1].length() > Main.getInstance().getConfig().getInt("factions.name.max")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.length")).replace("%min%",  Main.getInstance().getConfig().getInt("factions.name.min") + "").replace("%max%",  Main.getInstance().getConfig().getInt("factions.name.max") + "")));
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
                factionData.get().set("balance", 0);
                factionData.get().set("dtr", 1.01);
                factionData.get().set("open", false);
                factionData.get().set("koth-captures", 0);
                factionData.get().set("points", 0);
                factionData.get().set("invites", new ArrayList<>());
                factionData.get().set("lives", 0);
                factionData.get().set("deathban", true);
                factionData.save();
                user.setFaction(uuid);
                faction = new Faction(factionData);
                Main.getInstance().factions.put(uuid, faction);
                Main.getInstance().factionNameHolder.put(args[1].toLowerCase(), faction);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.create.success")).replace("%faction%", args[1])));
                Bukkit.broadcastMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.create.bc")).replace("%faction%", args[1]).replace("%player%", p.getName())));
                return true;
            }
            if (args[0].equalsIgnoreCase("d") || args[0].equalsIgnoreCase("deposit")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (args[1].equalsIgnoreCase("all")) {
                    double amount = user.getBalance().doubleValue();
                    if (amount <= 0.0099) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                        return true;
                    }

                    if (!Main.getEconomy().has(p, amount)) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.deposit.not-enough"))));
                        return true;
                    }

                    Main.getEconomy().withdrawPlayer(p, amount);
                    faction.setBalance(faction.getBalance().add(BigDecimal.valueOf(amount)));
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.deposit.success")).replace("%amount%", Main.getEconomy().format(amount))));
                    for (Player player : faction.getOnlineMembers()) {
                        player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.deposit.bc")).replace("%player%", p.getName()).replace("%amount%", Main.getEconomy().format(amount))));
                    }
                    return true;
                }
                try {
                    double ignored = Double.parseDouble(args[1]);
                } catch (NumberFormatException ignored) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                    return true;
                }

                double amount = Double.parseDouble(args[1]);
                if (amount <= 0.0099) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                    return true;
                }

                if (!Main.getEconomy().has(p, amount)) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.deposit.not-enough"))));
                    return true;
                }

                Main.getEconomy().withdrawPlayer(p, amount);
                faction.setBalance(faction.getBalance().add(BigDecimal.valueOf(amount)));
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.deposit.success")).replace("%amount%", Main.getEconomy().format(amount))));
                for (Player player : faction.getOnlineMembers()) {
                    player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.deposit.bc")).replace("%player%", p.getName()).replace("%amount%", Main.getEconomy().format(amount))));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("withdraw") || args[0].equalsIgnoreCase("w")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getRoleID(p.getUniqueId()) < 1) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.captain-above"))));
                    return true;
                }
                if (args[1].equalsIgnoreCase("all")) {
                    double balance = faction.getBalance().doubleValue();
                    if (balance <= 0.0099) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                        return true;
                    }

                    user.setBalance(user.getBalance().add(faction.getBalance()));
                    faction.setBalance(BigDecimal.valueOf(0));
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.withdraw.success")).replace("%amount%", Main.getEconomy().format(balance))));
                    for (Player player : faction.getOnlineMembers()) {
                        player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.withdraw.bc")).replace("%player%", p.getName()).replace("%amount%", Main.getEconomy().format(balance))));
                    }
                    return true;
                }

                try {
                    double ignored = Double.parseDouble(args[1]);
                } catch (NumberFormatException ignored) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                    return true;
                }

                double balance = Double.parseDouble(args[1]);
                if (balance <= 0.0099) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                    return true;
                }

                if (faction.getBalance().doubleValue() < balance) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.withdraw.not-enough"))));
                    return true;
                }

                user.setBalance(user.getBalance().add(BigDecimal.valueOf(balance)));
                faction.setBalance(faction.getBalance().subtract(BigDecimal.valueOf(balance)));
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.withdraw.success")).replace("%amount%", Main.getEconomy().format(balance))));
                for (Player player : faction.getOnlineMembers()) {
                    player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.withdraw.bc")).replace("%player%", p.getName()).replace("%amount%", Main.getEconomy().format(balance))));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("invite")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getRoleID(p.getUniqueId()) < 1) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.captain-above"))));
                    return true;
                }
                if (faction.isOpen()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.invite.open"))));
                    return true;
                }
                String name = args[1].toLowerCase();
                User player = User.get(name);
                if (player == null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
                    return true;
                }
                if (player.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.invite.has-already"))));
                    return true;
                }
                if (faction.getInvites().contains(player.getUUID())) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.invite.already-invited"))));
                    return true;
                }
                faction.getInvites().add(player.getUUID());
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.invite.success")).replace("%player%", player.getName())));
                for (Player play : faction.getOnlineMembers()) {
                    play.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.invite.bc")).replace("%player%", p.getName()).replace("%invited%", player.getName())));
                }
                if (Bukkit.getPlayer(player.getUUID()) != null) {
                    Objects.requireNonNull(Bukkit.getPlayer(player.getUUID())).sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.invite.message")).replace("%player%", p.getName()).replace("%faction%", faction.getName())));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("uninvite")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getRoleID(p.getUniqueId()) < 1) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.captain-above"))));
                    return true;
                }
                if (faction.isOpen()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.uninvite.open"))));
                    return true;
                }
                String name = args[1].toLowerCase();
                User player = User.get(name);
                if (player == null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
                    return true;
                }
                if (!faction.getInvites().contains(player.getUUID())) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.uninvite.not-invited"))));
                    return true;
                }
                faction.getInvites().remove(player.getUUID());
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.uninvite.success")).replace("%player%", player.getName())));
                for (Player play : faction.getOnlineMembers()) {
                    play.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.uninvite.bc")).replace("%player%", p.getName()).replace("%invited%", player.getName())));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("setlocation")) {
                if (!Permission.has(p, "faction.setlocation")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                    return true;
                }
                Faction faction = Faction.get(args[1]);
                if (faction == null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.faction-ony"))));
                    return true;
                }
                if (faction.getType().equals(FactionType.PLAYER)) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.wrong-type"))));
                    return true;
                }
                faction.setHome(p.getLocation());
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.setlocation.success")).replace("%faction%", faction.getName())));
                return true;
            }
            if (args[0].equalsIgnoreCase("kick")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getRoleID(p.getUniqueId()) < 1) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.captain-above"))));
                    return true;
                }
                User play = User.get(args[1]);
                if (play == null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
                    return true;
                }
                if (!play.hasFaction() || !play.getFaction().equals(user.getFaction())) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.not-in"))));
                    return true;
                }
                if (play.getClaim() != null && play.getClaim().getUUID().equals(faction.getUUID())) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.kick.claiming"))));
                    return true;
                }
                if (faction.getRoleID(play.getUUID()) >= faction.getRoleID(p.getUniqueId())) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.kick.cannot")).replace("%player%", play.getName())));
                    return true;
                }
                HashMap<UUID, Integer> members = faction.getMembers();
                members.remove(play.getUUID());
                play.setFaction(null);
                faction.setMembers(members);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.kick.success")).replace("%player%", play.getName())));
                if (Bukkit.getPlayer(play.getUUID()) != null) {
                    Objects.requireNonNull(Bukkit.getPlayer(play.getUUID())).sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.kick.message")).replace("%player%", p.getName())));
                }
                for (Player player : faction.getOnlineMembers()) {
                    player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.kick.bc")).replace("%player%", p.getName()).replace("%kicked%", play.getName())));
                }
                return true;
            }
            Messages.send(p, "faction.help.main", s);
            return true;
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("lives")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (args[1].equalsIgnoreCase("add")) {
                    try {
                        int ignored = Integer.parseInt(args[2]);
                    } catch (NumberFormatException ignored) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                        return true;
                    }
                    int amount = Integer.parseInt(args[2]);
                    if (amount <= 0) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                        return true;
                    }
                    if (user.getLives() < amount) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.lives.not-enough"))));
                        return true;
                    }
                    faction.setLives(faction.getLives() + amount);
                    user.setLives(user.getLives() - amount);
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.lives.add")).replace("%amount%", amount + "")));
                    return true;
                }
                Messages.send(p, "faction.help.lives", s);
                return true;
            }
            if (args[0].equalsIgnoreCase("captain")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getRoleID(p.getUniqueId()) < 2) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.coleader-above"))));
                    return true;
                }
                User play = User.get(args[2]);
                if (play == null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
                    return true;
                }
                if (!play.hasFaction() || !play.getFaction().equals(user.getFaction())) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.not-in"))));
                    return true;
                }

                if (args[1].equalsIgnoreCase("add")) {
                    if (faction.getRoleID(play.getUUID()) >= 1) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.captain.already"))));
                        return true;
                    }
                    HashMap<UUID, Integer> members = faction.getMembers();
                    members.put(play.getUUID(), 1);
                    faction.setMembers(members);
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.captain.add.success")).replace("%player%", play.getName())));
                    return true;
                }
                if (args[1].equalsIgnoreCase("remove")) {
                    if (faction.getRoleID(play.getUUID()) == 0 || faction.getRoleID(play.getUUID()) > 1) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.captain.not"))));
                        return true;
                    }
                    HashMap<UUID, Integer> members = faction.getMembers();
                    members.put(play.getUUID(), 0);
                    faction.setMembers(members);
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.captain.remove.success")).replace("%player%", play.getName())));
                    return true;
                }
                Messages.send(p, "faction.help.captain", s);
                return true;
            }
            if (args[0].equalsIgnoreCase("coleader")) {
                if (!user.hasFaction()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.none"))));
                    return true;
                }
                Faction faction = Faction.get(user.getFaction());
                if (faction.getRoleID(p.getUniqueId()) != 3) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.leader"))));
                    return true;
                }
                User play = User.get(args[2]);
                if (play == null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-player"))));
                    return true;
                }
                if (!play.hasFaction() || !play.getFaction().equals(user.getFaction())) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.not-in"))));
                    return true;
                }

                if (args[1].equalsIgnoreCase("add")) {
                    if (faction.getRoleID(play.getUUID()) >= 2) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.coleader.already"))));
                        return true;
                    }
                    HashMap<UUID, Integer> members = faction.getMembers();
                    members.put(play.getUUID(), 2);
                    faction.setMembers(members);
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.coleader.add.success")).replace("%player%", play.getName())));
                    return true;
                }
                if (args[1].equalsIgnoreCase("remove")) {
                    if (faction.getRoleID(play.getUUID()) <= 1 || faction.getRoleID(play.getUUID()) == 3) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.coleader.not"))));
                        return true;
                    }
                    HashMap<UUID, Integer> members = faction.getMembers();
                    members.put(play.getUUID(), 1);
                    faction.setMembers(members);
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.coleader.remove.success")).replace("%player%", play.getName())));
                    return true;
                }

                Messages.send(p, "faction.help.coleader", s);
                return true;
            }
            if (args[0].equalsIgnoreCase("setbalance")) {
                if (!Permission.has(p, "faction.setcolor")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                    return true;
                }
                Faction faction = Faction.get(args[1]);
                if (faction == null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.faction-ony"))));
                    return true;
                }
                if (!faction.getType().equals(FactionType.PLAYER)) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.player-only"))));
                    return true;
                }
                try {
                    double ignored = Double.parseDouble(args[2]);
                } catch (NumberFormatException ignored) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                    return true;
                }
                double balance = Double.parseDouble(args[2]);
                if (balance < 0) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.invalid-number"))));
                    return true;
                }
                faction.setBalance(BigDecimal.valueOf(balance));
                p.sendMessage(C.chat(Objects.requireNonNull(Objects.requireNonNull(Locale.get().getString("commands.faction.setbalance")).replace("%amount%", Main.getEconomy().format(balance)).replace("%faction%", faction.getName()))));
                return true;
            }
            if (args[0].equalsIgnoreCase("setcolor")) {
                if (!Permission.has(p, "faction.setcolor")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                    return true;
                }
                Faction faction = Faction.get(args[1]);
                if (faction == null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.faction-ony"))));
                    return true;
                }
                if (faction.getType().equals(FactionType.PLAYER)) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.wrong-type"))));
                    return true;
                }
                if (!Color.isValid(args[2])) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.setcolor.invalid"))));
                    return true;
                }
                Color color = Color.valueOf(args[2].toUpperCase());
                faction.setColor(color);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.setcolor.success")).replace("%faction%", faction.getName()).replace("%color%", color.toColorCode() + color.name().toUpperCase())));
                return true;
            }
            if (args[0].equalsIgnoreCase("settype")) {
                if (!Permission.has(p, "faction.settype")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-permission"))));
                    return true;
                }
                Faction faction = Faction.get(args[1]);
                if (faction == null) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.faction-ony"))));
                    return true;
                }
                if (faction.getType().equals(FactionType.PLAYER)) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.faction.wrong-type"))));
                    return true;
                }
                if (!FactionType.isValid(args[2])) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.settype.invalid"))));
                    return true;
                }
                FactionType type = FactionType.valueOf(args[2].toUpperCase());
                faction.setType(type);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.settype.success")).replace("%faction%", faction.getName()).replace("%type%", type.name().toUpperCase())));
                return true;
            }
            Messages.send(p, "faction.help.main", s);
            return true;
        }
        Messages.send(p, "faction.help.main", s);
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) return new ArrayList<>();
        List<String> values = new ArrayList<>();
        if (args.length == 1) {
            values.addAll(List.of("create", "open", "show", "deposit", "invite", "join", "withdraw",
                    "subclaim", "captain", "coleader", "invites", "announcement", "uninvite", "leave", "kick",
                    "sethome", "claim", "home", "list", "unclaim", "rename", "disband", "ff"));
            if (Permission.has(p, "faction.createsystem")) values.add("createsystem");
            if (Permission.has(p, "faction.setcolor")) values.add("setcolor");
            if (Permission.has(p, "faction.settype")) values.add("settype");
            if (Permission.has(p, "faction.setlocation")) values.add("setlocation");
            if (Permission.has(p, "faction.bypass")) values.add("bypass");
            if (Permission.has(p, "faction.setbalance")) values.add("setbalance");
        }
        if (args.length == 2) {
            if ((args[0].equalsIgnoreCase("claimfor") && Permission.has(p, "faction.claimfor"))
                    || (args[0].equalsIgnoreCase("setcolor") && Permission.has(p, "faction.setcolor"))
                    || (args[0].equalsIgnoreCase("settype") && Permission.has(p, "faction.settype"))
                    || (args[0].equalsIgnoreCase("setlocation") && Permission.has(p, "faction.setlocation"))) {
                values.addAll(Main.getInstance().factionNameHolder.values().stream()
                        .filter(faction -> faction.getType() != FactionType.PLAYER)
                        .map(Faction::getName)
                        .toList());
            }
            if ((args[0].equalsIgnoreCase("setbalance") && Permission.has(p, "faction.setbalance"))) {
                values.addAll(Main.getInstance().factionNameHolder.values().stream()
                        .filter(faction -> faction.getType() == FactionType.PLAYER)
                        .map(Faction::getName)
                        .toList());
            }
        }
        return values;
    }

    private void show(Player p, Faction faction) {
        User user = User.get(p);
        if (faction.getType().equals(FactionType.PLAYER)) {
            for (String s : Messages.get().getStringList("faction.show.player")) {
                if (s.contains("%name%")) s = s.replace("%name%", faction.getValidName(p, false));
                if (s.contains("%online%")) s = s.replace("%online%", faction.getOnlineMembers().size() + "");
                if (s.contains("%players%")) s = s.replace("%players%", faction.getMembers().size() + "");
                if (s.contains("%home%")) {
                    if (faction.getHome() == null) {
                        s = s.replace("%home%", Objects.requireNonNull(Locale.get().getString("primary.none")));
                    } else {
                        s = s.replace("%home%", faction.getHome().getBlockX() + ", " + faction.getHome().getBlockZ());
                    }
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
                    s = s.replace("%open%", Objects.requireNonNull(Locale.get().getString("commands.faction.open.open")));
                }
                if (s.contains("%lives%")) {
                    if (!user.hasFaction() || !user.getFaction().equals(faction.getUUID())) continue;
                    s = s.replace("%lives%", faction.getLives() + "");
                }
                if (s.contains("%announcement%")) {
                    if (!user.hasFaction() || !user.getFaction().equals(faction.getUUID())) continue;
                    s = s.replace("%announcement%", faction.getAnnouncement() == null ? Objects.requireNonNull(Locale.get().getString("primary.none")) : faction.getAnnouncement());
                }
                p.sendMessage(C.chat(s));
            }
        } else {
            for (String s : Messages.get().getStringList("faction.show.system")) {
                if (s.contains("%name%")) s = s.replace("%name%", faction.getValidName(p, false));
                if (s.contains("%home%")) {
                    if (faction.getHome() == null) {
                        s = s.replace("%home%", Objects.requireNonNull(Locale.get().getString("primary.none")));
                    } else {
                        s = s.replace("%home%", "X: " + faction.getHome().getBlockX() + ", Z: " + faction.getHome().getBlockZ());
                    }
                }
                p.sendMessage(C.chat(s));
            }
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

    private void showMapPillars(Player player, Map<Faction, List<Cuboid>> nearbyClaims, Map<Faction, Material> pillars) {
        for (Map.Entry<Faction, List<Cuboid>> entry : nearbyClaims.entrySet()) {
            Faction faction = entry.getKey();
            List<Cuboid> claims = entry.getValue();
            Material pillarMaterial = pillars.getOrDefault(faction, Material.WHITE_WOOL);

            for (Cuboid cuboid : claims) {
                List<Location> corners = getCuboidCorners(cuboid);
                for (Location corner : corners) {
                    showPillar(player, corner, pillarMaterial);
                }
            }
        }
    }

    private void clearMapPillars(Player player, Map<Faction, List<Cuboid>> nearbyClaims) {
        for (Map.Entry<Faction, List<Cuboid>> entry : nearbyClaims.entrySet()) {
            List<Cuboid> claims = entry.getValue();
            for (Cuboid cuboid : claims) {
                List<Location> corners = getCuboidCorners(cuboid);
                for (Location corner : corners) {
                    clearPillar(player, corner);
                }
            }
        }
    }

    private void clearPillar(Player player, Location base) {
        World world = base.getWorld();
        int minY = Objects.requireNonNull(world).getMinHeight();
        int maxY = world.getMaxHeight();

        for (int y = minY; y < maxY; y++) {
            Location blockLoc = new Location(world, base.getBlockX(), y, base.getBlockZ());
            Block block = world.getBlockAt(blockLoc);
            if (!block.isEmpty()) continue;
            player.sendBlockChange(blockLoc, Material.AIR.createBlockData());
        }
    }

    private void showPillar(Player player, Location base, Material pillarMaterial) {
        World world = base.getWorld();
        int minY = Objects.requireNonNull(world).getMinHeight();
        int maxY = world.getMaxHeight();

        for (int y = minY; y < maxY; y++) {
            Location blockLoc = new Location(world, base.getBlockX(), y, base.getBlockZ());
            Block block = world.getBlockAt(blockLoc);
            if (!block.isEmpty()) continue;
            if (y % 4 == 0) {
                player.sendBlockChange(blockLoc, pillarMaterial.createBlockData());
            } else {
                player.sendBlockChange(blockLoc, Material.GLASS.createBlockData());
            }
        }
    }

    private List<Location> getCuboidCorners(Cuboid cuboid) {
        World world = cuboid.getPoint1().getWorld();
        int x1 = cuboid.getXMin(), x2 = cuboid.getXMax();
        int z1 = cuboid.getZMin(), z2 = cuboid.getZMax();
        int y = cuboid.getYMin();

        List<Location> corners = new ArrayList<>();
        corners.add(new Location(world, x1, y, z1));
        corners.add(new Location(world, x1, y, z2));
        corners.add(new Location(world, x2, y, z1));
        corners.add(new Location(world, x2, y, z2));
        return corners;
    }
}
