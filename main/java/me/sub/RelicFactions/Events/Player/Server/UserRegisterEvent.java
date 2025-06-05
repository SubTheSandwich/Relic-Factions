package me.sub.RelicFactions.Events.Player.Server;

import me.sub.RelicFactions.Files.Classes.*;
import me.sub.RelicFactions.Files.Data.*;
import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Files.Normal.Messages;
import me.sub.RelicFactions.Files.Normal.ModModeFile;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Calculate;
import me.sub.RelicFactions.Utils.Fastboard.FastBoard;
import me.sub.RelicFactions.Utils.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class UserRegisterEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage(null);
        Player p = e.getPlayer();
        User user = User.get(p);
        if (user == null) {
            UserData userData = new UserData(p.getUniqueId());
            userData.setup();
            userData.get().set("uuid", p.getUniqueId().toString());
            userData.get().set("name", p.getName());
            userData.get().set("balance", Main.getInstance().getConfig().getDouble("economy.default-balance"));
            userData.get().set("settings.messages.enabled", true);
            userData.get().set("settings.messages.sounds", true);
            userData.save();
            user = new User(userData);
            user.addTimer(new PlayerTimer(p.getUniqueId(), Timer.STARTING));
            Main.getInstance().users.put(p.getUniqueId(), user);
            Main.getInstance().userNameHolder.put(p.getName().toLowerCase(), user);
            Messages.send(p, "first-join-message");
        }
        if (user.getLoggerUUID() != null) {
            Villager entity = (Villager) Bukkit.getServer().getEntity(user.getLoggerUUID());
            if (entity == null || entity.isDead()) {
                user.setDeaths(user.getDeaths() + 1);
                // TODO: EOTW
                int time = User.getDeathbanTime(p);
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MINUTE, time);
                long deathban = calendar.getTimeInMillis();
                user.setDeathbannedTill(deathban);
                user.setDeathBanned(true);
                p.kickPlayer(C.chat(Objects.requireNonNull(Locale.get().getString("events.deathban.kick")).replace("%time%", Timer.getMessageFormat(deathban - System.currentTimeMillis()))));
                user.getTimers().clear();
                if (user.hasFaction()) {
                    Faction faction = Faction.get(user.getFaction());
                    faction.setDTR(BigDecimal.valueOf(Calculate.round(Math.max(-0.99, faction.getDTR().doubleValue() - Main.getInstance().getConfig().getDouble("factions.dtr.death")), 2)));
                    faction.setRegening(false);
                    Calendar regen = Calendar.getInstance();
                    regen.add(Calendar.MINUTE, Main.getInstance().getConfig().getInt("factions.dtr.regen.start-delay"));
                    faction.setTimeTilRegen(regen.getTimeInMillis());
                }
                return;
            }
            p.getInventory().setContents(entity.getInventory().getContents());
            p.setHealth(entity.getHealth());
            entity.remove();
        }
        if (user.isDeathBanned()) {
            long time = user.getDeathbannedTill();
            if (time - System.currentTimeMillis() <= 0) {
                user.setDeathbannedTill(0);
                user.setDeathBanned(false);
            } else {
                if (user.getLives() > 0) {
                    user.setLives(user.getLives() - 1);
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.deathban.used-life"))));
                    user.setDeathbannedTill(0);
                    user.setDeathBanned(false);
                } else {
                    p.kickPlayer(C.chat(Objects.requireNonNull(Locale.get().getString("events.deathban.kick")).replace("%time%", Timer.getMessageFormat(user.getDeathbannedTill() - System.currentTimeMillis()))));
                    return;
                }
            }
        }

        if (user.isRevived()) {
            user.setRevived(false);
            if (user.getLastInventoryContents() != null) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.revive.items"))));
                p.getInventory().setContents(user.getLastInventoryContents());
                user.setLastInventoryContents(null);
            } else {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.revive.no-items"))));
            }
        }

        if (user.hasFaction()) {
            Faction faction = Faction.get(user.getFaction());
            for (Player player : faction.getOnlineMembers()) {
                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.member.online")).replace("%player%", p.getName())));
            }
        }

        if (user.getCustomTimers() != null) {
            user.getCustomTimers().values().forEach(CustomTimer::tick);
        }

        user.setLastLoginTimestamp(System.currentTimeMillis());

        if (!Permission.has(p, "staff") && !Permission.has(p, "admin")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                User play = User.get(player);
                if (play.getModMode() != null && play.getModMode().isInVanish()) {
                    p.hidePlayer(Main.getInstance(), player);
                }
            }
        } else {
            if (ModModeFile.get().getBoolean("mod-mode.enabled") && ModModeFile.get().getBoolean("mod-mode.on-join-enabled")) {
                p.performCommand("h");
            }
        }


        for (String s : Messages.get().getStringList("join-message")) {
            if (s.contains("<display=%has_faction%")) {
                if (!user.hasFaction()) continue;
                s = s.replace("<display=%has_faction%", "");
                Faction faction = Faction.get(user.getFaction());
                s = s.replace("%faction-name%", faction.getName());
                s = s.replace("%faction-dtr%", Faction.formatDTR(faction.getDTR()));
                s = s.replace("%faction-online-count%", faction.getOnlineMembers().size() + "");
                s = s.replace("%faction-total-count%", faction.getMembers().size() + "");
            }
            p.sendMessage(C.chat(s));
        }
        user.setDisconnected(false);
        if (!p.getName().equals(user.getName())) {
            Main.getInstance().userNameHolder.remove(user.getName().toLowerCase());
            user.setName(p.getName());
            Main.getInstance().userNameHolder.put(user.getName().toLowerCase(), user);
        }
        User finalUser = user;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!Main.getInstance().getConfig().getBoolean("features.scoreboard.enabled")) return;
                FastBoard board = Main.getInstance().boards.getOrDefault(p.getUniqueId(), null);
                ArrayList<String> lines = new ArrayList<>();
                if (board == null) {
                    board = new FastBoard(p);
                    board.updateTitle(C.chat(Objects.requireNonNull(Main.getInstance().getConfig().getString("scoreboard.title"))));
                }
                if (!p.isOnline()) {
                    cancel();
                    board.delete();
                    return;
                }
                for (String s : Main.getInstance().getConfig().getStringList("scoreboard.lines")) {
                    if (s.contains("%focus-lines%")) {
                        if (!finalUser.hasFaction()) continue;
                        Faction faction = Faction.get(finalUser.getFaction());
                        if (faction.getFocusedFaction() == null) continue;
                        Faction focus = Faction.get(faction.getFocusedFaction());
                        if (focus == null) {
                            faction.setFocusedFaction(null);
                            continue;
                        }
                        for (String line : Main.getInstance().getConfig().getStringList("scoreboard.focus")) {
                            line = line.replace("%faction%", focus.getValidName(p, false));
                            if (focus.getHome() == null) {
                                line = line.replace("%home%", Objects.requireNonNull(Locale.get().getString("primary.none")));
                            } else {
                                line = line.replace("%home%", focus.getHome().getBlockX() + ", " + focus.getHome().getBlockZ());
                            }
                            lines.add(C.chat(line));
                        }
                        continue;
                    }
                    if (s.contains("%mountain-lines%")) {
                        if (!finalUser.isMountains()) continue;
                        for (Mountain mountain : Main.getInstance().mountains.values()) {
                            if (!mountain.isSetup()) continue;
                            BigDecimal time = mountain.getTime();
                            BigDecimal defaultTime = BigDecimal.valueOf(mountain.getDefaultTime() * 60L);

                            BigDecimal diff = defaultTime.subtract(time);
                            String display = Objects.requireNonNull(Main.getInstance().getConfig().getString("scoreboard.mountain.line"));
                            display = display.replace("%type%", C.capitalizeWord(mountain.getType().name()));
                            display = display.replace("%time%", Timer.format(diff));
                            lines.add(C.chat(display));
                        }
                        continue;
                    }
                    if (s.contains("%koth-lines%")) {
                        for (RunningKOTH runningKOTH : Main.getInstance().runningKOTHS.values()) {

                            KOTH koth = runningKOTH.getKOTH();
                            String display;
                            if (koth.isSpecial()) {
                                display = Main.getInstance().getConfig().getString("scoreboard.koth.special");
                            } else {
                                display = Main.getInstance().getConfig().getString("scoreboard.koth.normal");
                            }
                            display = Objects.requireNonNull(display).replace("%koth%", koth.getName());
                            display = display.replace("%time%", Timer.format(runningKOTH.getTimeLeft()));
                            lines.add(C.chat(display));
                        }
                        continue;
                    }
                    if (s.contains("%customtimers%")) {
                        if (finalUser.getCustomTimers().isEmpty()) continue;
                        for (CustomTimer timer : finalUser.getCustomTimers().values()) {
                            lines.add(C.chat(C.bold(timer.getName()) + "&7: &c" + Timer.format(timer.getDuration())));
                        }
                        continue;
                    }
                    if (s.contains("<display=%has_player")) {
                        String[] split = s.split("<display=%has_player_");
                        String timer = split[1];
                        timer = timer.replace("_timer%", "");
                        if (!finalUser.hasTimer(timer)) continue;
                        PlayerTimer time = finalUser.getTimer(timer);
                        s = s.replace("<display=%has_player_" + timer + "_timer%", "");
                        s = s.replace("%player_" + timer + "_timer%", Timer.format(time.getDuration()));
                    }

                    if (s.contains("<display=%has_server")) {
                        String[] split = s.split("<display=%has_server_");
                        String timer = split[1];
                        timer = timer.replace("_timer%", "");
                        if (!ServerTimer.has(timer)) continue;
                        ServerTimer time = ServerTimer.get(timer);
                        s = s.replace("<display=%has_server_" + timer + "_timer%", "");
                        s = s.replace("%server_" + timer + "_timer%", Timer.format(time.getDuration()));
                        if (timer.equalsIgnoreCase("sotw") && Main.getInstance().sotwEnabled.contains(p.getUniqueId())) {
                            s = C.strikethrough(s);
                        }
                    }
                    if (s.contains("%mod-lines%")) {
                        ModMode modMode = finalUser.getModMode();
                        if (modMode == null) continue;
                        for (String line : Main.getInstance().getConfig().getStringList("scoreboard.mod-mode")) {
                            if (line.contains("%vanished%")) {
                                line = Objects.requireNonNull(line).replace("%vanished%", Objects.requireNonNull(modMode.isInVanish() ? Locale.get().getString("primary.enabled") : Locale.get().getString("primary.disabled")));
                            }
                            if (line.contains("%gamemode%")) {
                                line = line.replace("%gamemode%", p.getGameMode().name().toUpperCase().charAt(0) + p.getGameMode().name().toLowerCase().substring(1));
                            }
                            if (line.contains("<display=%has_permission")) {
                                String[] split = line.split("<display=%has_permission_");
                                String permission = split[1];
                                permission = permission.replace("%", "");
                                if (!p.hasPermission(permission)) continue;
                                line = line.replace("<display=%has_permission_" + permission + "%", "");
                                if (line.contains("%modmode-bypass%")) {
                                    line = Objects.requireNonNull(line).replace("%modmode-bypass%", Objects.requireNonNull(modMode.isInBypass() ? Locale.get().getString("primary.enabled") : Locale.get().getString("primary.disabled")));
                                }
                            }
                            if (line.contains("%online%")) {
                                line = line.replace("%online%", Bukkit.getOnlinePlayers().size() + "");
                            }
                            lines.add(C.chat(line));
                        }
                        continue;
                    }

                    lines.add(C.chat(s));
                }

                if (lines.size() <= Main.getInstance().getConfig().getInt("features.scoreboard.line-limit") || !finalUser.isScoreboard()) {
                    board.delete();
                    Main.getInstance().boards.remove(p.getUniqueId());
                } else {
                    board.updateLines(lines);
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }
}
