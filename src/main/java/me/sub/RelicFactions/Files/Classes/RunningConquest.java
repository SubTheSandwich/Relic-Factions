package me.sub.RelicFactions.Files.Classes;

import me.sub.RelicFactions.Files.Data.Zone;
import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class RunningConquest {

    private final Conquest conquest;
    private final HashMap<UUID, Integer> points;
    private final HashMap<Zone, BigDecimal> zoneTimers;
    private final HashMap<Zone, UUID> controllingPlayers;
    private int redLast = -1;
    private int greenLast = -1;
    private int blueLast = -1;
    private int yellowLast = -1;

    public RunningConquest(Conquest conquest) {
        this.conquest = conquest;
        this.points = new HashMap<>();
        HashMap<Zone, BigDecimal> zoneTimers = new HashMap<>();
        zoneTimers.put(conquest.getRed(), new BigDecimal(Main.getInstance().getConfig().getInt("conquest.time")));
        zoneTimers.put(conquest.getGreen(), new BigDecimal(Main.getInstance().getConfig().getInt("conquest.time")));
        zoneTimers.put(conquest.getBlue(), new BigDecimal(Main.getInstance().getConfig().getInt("conquest.time")));
        zoneTimers.put(conquest.getYellow(), new BigDecimal(Main.getInstance().getConfig().getInt("conquest.time")));
        this.zoneTimers = zoneTimers;
        controllingPlayers = new HashMap<>();
        start();
    }

    public Conquest getConquest() {
        return conquest;
    }

    public HashMap<UUID, Integer> getPoints() {
        return points;
    }

    public void setRedTime(BigDecimal time) {
        zoneTimers.put(conquest.getRed(), time);
    }

    public void setGreenTime(BigDecimal time) {
        zoneTimers.put(conquest.getGreen(), time);
    }

    public void setYellowTime(BigDecimal time) {
        zoneTimers.put(conquest.getYellow(), time);
    }

    public void setBlueTime(BigDecimal time) {
        zoneTimers.put(conquest.getBlue(), time);
    }

    public BigDecimal getRedTime() {
        return zoneTimers.get(conquest.getRed());
    }

    public BigDecimal getGreenTime() {
        return zoneTimers.get(conquest.getGreen());
    }

    public BigDecimal getYellowTime() {
        return zoneTimers.get(conquest.getYellow());
    }

    public BigDecimal getBlueTime() {
        return zoneTimers.get(conquest.getBlue());
    }

    public UUID getRedControllingPlayer() {
        return controllingPlayers.getOrDefault(conquest.getRed(), null);
    }

    public UUID getGreenControllingPlayer() {
        return controllingPlayers.getOrDefault(conquest.getGreen(), null);
    }

    public UUID getYellowControllingPlayer() {
        return controllingPlayers.getOrDefault(conquest.getYellow(), null);
    }

    public UUID getBlueControllingPlayer() {
        return controllingPlayers.getOrDefault(conquest.getBlue(), null);
    }


    public void setRedControllingPlayer(UUID uuid) {
        controllingPlayers.put(conquest.getRed(), uuid);
    }

    public void setGreenControllingPlayer(UUID uuid) {
        controllingPlayers.put(conquest.getGreen(), uuid);
    }

    public void setYellowControllingPlayer(UUID uuid) {
        controllingPlayers.put(conquest.getYellow(), uuid);
    }

    public void setBlueControllingPlayer(UUID uuid) {
        controllingPlayers.put(conquest.getBlue(), uuid);
    }

    private void start() {

        new BukkitRunnable() {
            @Override
            public void run() {

                if (Main.getInstance().getRunningConquest() == null || !Main.getInstance().getRunningConquest().getConquest().getUUID().equals(conquest.getUUID())) {
                    cancel();
                    return;
                }

                List<Map.Entry<UUID, Integer>> sorted =
                        getPoints().entrySet().stream()
                                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                                .toList();
                if (!sorted.isEmpty()) {
                    if (sorted.getFirst().getValue() >= Main.getInstance().getConfig().getInt("conquest.max-points")) {
                        Faction faction = Faction.get(sorted.getFirst().getKey());
                        if (faction == null) {
                            points.remove(sorted.getFirst().getKey());
                            return;
                        }
                        UUID capture = getCapturePlayer(Faction.get(sorted.getFirst().getKey()));
                        if (capture == null) {
                            points.remove(sorted.getFirst().getKey());
                            return;
                        }
                        Main.getInstance().sendGlobalMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.end.captured")).replace("%conquest%", conquest.getName())));
                        for (String s : Main.getInstance().getConfig().getStringList("conquest.on-capture")) {
                            s = s.replace("%player%", User.get(capture).getName());
                            s = s.replace("%faction%", faction.getName());
                            s = s.replace("%conquest%", conquest.getName());
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
                        }
                        Main.getInstance().setRunningConquest(null);
                        if (Main.getInstance().getConfig().getBoolean("elo.enable") && Main.getInstance().getConfig().getBoolean("elo.conquest.enable")) {
                            faction.setPoints(faction.getPoints() + Main.getInstance().getConfig().getInt("elo.conquest.points-on-win"));
                        }
                        cancel();
                        return;
                    }
                }

                if (getRedControllingPlayer() != null) {
                    User user = User.get(getRedControllingPlayer());
                    if (user == null) {
                        resetZone(null, conquest.getRed(), true);
                        return;
                    }
                    if (!user.hasFaction()) {
                        resetZone(user, conquest.getRed(), true);
                        return;
                    }
                    Faction faction = Faction.get(user.getFaction());
                    if (faction == null) {
                        resetZone(user, conquest.getRed(), true);
                        return;
                    }
                    if (user.hasTimer("pvp") || user.hasTimer("starting")) {
                        resetZone(user, conquest.getRed(), true);
                        return;
                    }
                    BigDecimal time = getRedTime();
                    time = time.subtract(BigDecimal.valueOf(0.05));
                    setRedTime(time);
                    if (time.doubleValue() < 0) {
                        zoneTimers.put(conquest.getRed(), new BigDecimal(Main.getInstance().getConfig().getInt("conquest.time")));
                        points.put(faction.getUUID(), points.getOrDefault(faction.getUUID(), 0) + Main.getInstance().getConfig().getInt("conquest.points-per-cap"));
                        Bukkit.broadcast(Component.text(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.zone-capped"))
                                .replace("%player%", user.getName())
                                .replace("%zone%", "&c" + conquest.getRed().getName()))));
                    }
                    int seconds = time.setScale(0, RoundingMode.FLOOR).intValue();
                    if (seconds != redLast) {
                        if (seconds > 10 && seconds % 5 == 0) {
                            for (Player player : faction.getOnlineMembers()) {
                                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.zone-capping"))
                                        .replace("%time%", Timer.getMessageFormat(time))
                                        .replace("%zone%", "&c" + conquest.getRed().getName())));
                            }
                            redLast = seconds;
                        } else if (seconds <= 10 && seconds > 0) {
                            for (Player player : faction.getOnlineMembers()) {
                                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.zone-capping"))
                                        .replace("%time%", Timer.getMessageFormat(time))
                                        .replace("%zone%", "&c" + conquest.getRed().getName())));
                            }
                            redLast = seconds;
                        }
                    }
                }

                if (getBlueControllingPlayer() != null) {
                    User user = User.get(getBlueControllingPlayer());
                    if (user == null) {
                        resetZone(null, conquest.getBlue(), true);
                        return;
                    }
                    if (!user.hasFaction()) {
                        resetZone(user, conquest.getBlue(), true);
                        return;
                    }
                    Faction faction = Faction.get(user.getFaction());
                    if (faction == null) {
                        resetZone(user, conquest.getBlue(), true);
                        return;
                    }
                    if (user.hasTimer("pvp") || user.hasTimer("starting")) {
                        resetZone(user, conquest.getBlue(), true);
                        return;
                    }
                    BigDecimal time = getBlueTime();
                    time = time.subtract(BigDecimal.valueOf(0.05));
                    setBlueTime(time);
                    if (time.doubleValue() < 0) {
                        zoneTimers.put(conquest.getBlue(), new BigDecimal(Main.getInstance().getConfig().getInt("conquest.time")));
                        points.put(faction.getUUID(), points.getOrDefault(faction.getUUID(), 0) + Main.getInstance().getConfig().getInt("conquest.points-per-cap"));
                        Bukkit.broadcast(Component.text(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.zone-capped"))
                                .replace("%player%", user.getName())
                                .replace("%zone%", "&9" + conquest.getBlue().getName()))));
                    }
                    int seconds = time.setScale(0, RoundingMode.FLOOR).intValue();
                    if (seconds != blueLast) {
                        if (seconds > 10 && seconds % 5 == 0) {
                            for (Player player : faction.getOnlineMembers()) {
                                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.zone-capping"))
                                        .replace("%time%", Timer.getMessageFormat(time))
                                        .replace("%zone%", "&9" + conquest.getBlue().getName())));
                            }
                            blueLast = seconds;
                        } else if (seconds <= 10 && seconds > 0) {
                            for (Player player : faction.getOnlineMembers()) {
                                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.zone-capping"))
                                        .replace("%time%", Timer.getMessageFormat(time))
                                        .replace("%zone%", "&9" + conquest.getBlue().getName())));
                            }
                            blueLast = seconds;
                        }
                    }
                }

                if (getGreenControllingPlayer() != null) {
                    User user = User.get(getGreenControllingPlayer());
                    if (user == null) {
                        resetZone(null, conquest.getGreen(), true);
                        return;
                    }
                    if (!user.hasFaction()) {
                        resetZone(user, conquest.getGreen(), true);
                        return;
                    }
                    Faction faction = Faction.get(user.getFaction());
                    if (faction == null) {
                        resetZone(user, conquest.getGreen(), true);
                        return;
                    }
                    if (user.hasTimer("pvp") || user.hasTimer("starting")) {
                        resetZone(user, conquest.getGreen(), true);
                        return;
                    }
                    BigDecimal time = getGreenTime();
                    time = time.subtract(BigDecimal.valueOf(0.05));
                    setGreenTime(time);
                    if (time.doubleValue() < 0) {
                        zoneTimers.put(conquest.getGreen(), new BigDecimal(Main.getInstance().getConfig().getInt("conquest.time")));
                        points.put(faction.getUUID(), points.getOrDefault(faction.getUUID(), 0) + Main.getInstance().getConfig().getInt("conquest.points-per-cap"));
                        Bukkit.broadcast(Component.text(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.zone-capped"))
                                .replace("%player%", user.getName())
                                .replace("%zone%", "&a" + conquest.getGreen().getName()))));
                    }
                    int seconds = time.setScale(0, RoundingMode.FLOOR).intValue();
                    if (seconds != greenLast) {
                        if (seconds > 10 && seconds % 5 == 0) {
                            for (Player player : faction.getOnlineMembers()) {
                                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.zone-capping"))
                                        .replace("%time%", Timer.getMessageFormat(time))
                                        .replace("%zone%", "&a" + conquest.getGreen().getName())));
                            }
                            greenLast = seconds;
                        } else if (seconds <= 10 && seconds > 0) {
                            for (Player player : faction.getOnlineMembers()) {
                                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.zone-capping"))
                                        .replace("%time%", Timer.getMessageFormat(time))
                                        .replace("%zone%", "&a" + conquest.getGreen().getName())));
                            }
                            greenLast = seconds;
                        }
                    }
                }

                if (getYellowControllingPlayer() != null) {
                    User user = User.get(getYellowControllingPlayer());
                    if (user == null) {
                        resetZone(null, conquest.getYellow(), true);
                        return;
                    }
                    if (!user.hasFaction()) {
                        resetZone(user, conquest.getYellow(), true);
                        return;
                    }
                    Faction faction = Faction.get(user.getFaction());
                    if (faction == null) {
                        resetZone(user, conquest.getYellow(), true);
                        return;
                    }
                    if (user.hasTimer("pvp") || user.hasTimer("starting")) {
                        resetZone(user, conquest.getYellow(), true);
                        return;
                    }
                    BigDecimal time = getYellowTime();
                    time = time.subtract(BigDecimal.valueOf(0.05));
                    setYellowTime(time);
                    if (time.doubleValue() < 0) {
                        zoneTimers.put(conquest.getYellow(), new BigDecimal(Main.getInstance().getConfig().getInt("conquest.time")));
                        points.put(faction.getUUID(), points.getOrDefault(faction.getUUID(), 0) + Main.getInstance().getConfig().getInt("conquest.points-per-cap"));
                        Bukkit.broadcast(Component.text(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.zone-capped"))
                                .replace("%player%", user.getName())
                                .replace("%zone%", "&e" + conquest.getYellow().getName()))));
                    }
                    int seconds = time.setScale(0, RoundingMode.FLOOR).intValue();
                    if (seconds != yellowLast) {
                        if (seconds > 10 && seconds % 5 == 0) {
                            for (Player player : faction.getOnlineMembers()) {
                                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.zone-capping"))
                                        .replace("%time%", Timer.getMessageFormat(time))
                                        .replace("%zone%", "&e" + conquest.getYellow().getName())));
                            }
                            yellowLast = seconds;
                        } else if (seconds <= 10 && seconds > 0) {
                            for (Player player : faction.getOnlineMembers()) {
                                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.zone-capping"))
                                        .replace("%time%", Timer.getMessageFormat(time))
                                        .replace("%zone%", "&e" + conquest.getYellow().getName())));
                            }
                            yellowLast = seconds;
                        }
                    }
                }

            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }

    public void resetZone(User user, Zone zone, boolean knock) {
        controllingPlayers.put(zone, null);
        zoneTimers.put(zone, new BigDecimal(Main.getInstance().getConfig().getInt("conquest.time")));
        if (user == null) return;
        if (!user.hasFaction()) return;
        Faction faction = Faction.get(user.getFaction());
        if (faction == null) return;
        if (knock) {
            Bukkit.broadcast(Component.text(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.knock")).replace("%faction%", faction.getName()).replace("%zone%", zone.getName()))));
        } else {
            Bukkit.broadcast(Component.text(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.zone-capped")).replace("%zone%", zone.getName()).replace("%player%", user.getName()))));
        }
    }

    private UUID getCapturePlayer(Faction faction) {
        if (faction.getOnlineMembers().stream()
                .map(Player::getUniqueId)
                .anyMatch(uuid -> uuid.equals(getRedControllingPlayer()))) {
            return getRedControllingPlayer();
        }
        if (faction.getOnlineMembers().stream()
                .map(Player::getUniqueId)
                .anyMatch(uuid -> uuid.equals(getYellowControllingPlayer()))) {
            return getYellowControllingPlayer();
        }
        if (faction.getOnlineMembers().stream()
                .map(Player::getUniqueId)
                .anyMatch(uuid -> uuid.equals(getGreenControllingPlayer()))) {
            return getGreenControllingPlayer();
        }
        if (faction.getOnlineMembers().stream()
                .map(Player::getUniqueId)
                .anyMatch(uuid -> uuid.equals(getBlueControllingPlayer()))) {
            return getBlueControllingPlayer();
        }
        List<UUID> uuids = faction.getOnlineMembers().stream()
                .map(Player::getUniqueId)
                .toList();

        if (uuids.isEmpty()) return null;
        return uuids.get(new Random().nextInt(uuids.size()));
    }
}
