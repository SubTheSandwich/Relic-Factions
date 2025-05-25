package me.sub.RelicFactions.Events.Player.Server;

import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.PlayerTimer;
import me.sub.RelicFactions.Files.Data.ServerTimer;
import me.sub.RelicFactions.Files.Data.UserData;
import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Files.Normal.Messages;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Calculate;
import me.sub.RelicFactions.Utils.Fastboard.FastBoard;
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
                ArrayList<String> lines = new ArrayList<>();
                FastBoard board = Main.getInstance().boards.getOrDefault(p.getUniqueId(), null);
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

                    lines.add(C.chat(s));
                }

                if (lines.size() <= Main.getInstance().getConfig().getInt("features.scoreboard.line-limit")) {
                    board.delete();
                    Main.getInstance().boards.remove(p.getUniqueId());
                } else {
                    board.updateLines(lines);
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }
}
