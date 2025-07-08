package me.sub.RelicFactions.Events.Player.Movement;

import me.sub.RelicFactions.Commands.User.FactionCommand;
import me.sub.RelicFactions.Events.Player.Interact.UserClaimEvents;
import me.sub.RelicFactions.Files.Classes.*;
import me.sub.RelicFactions.Files.Data.ClaimBufferManager;
import me.sub.RelicFactions.Files.Data.Cuboid;
import me.sub.RelicFactions.Files.Data.PlayerTimer;
import me.sub.RelicFactions.Files.Enums.FactionType;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Files.Normal.Locations;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class UserMoveEvent implements Listener {

    private final ClaimBufferManager bufferManager = new ClaimBufferManager();

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        user.updateClass();

        bufferManager.updateBufferWall(p);

        if (Faction.getAt(e.getTo()) != null) {
            Faction faction = Faction.getAt(e.getTo());
            if (faction == null) return;
            if (faction.getType().equals(FactionType.SAFEZONE) && user.hasTimer("combat")) {
                if (Faction.getAt(e.getFrom()) == null || !Objects.requireNonNull(Faction.getAt(e.getFrom())).getType().equals(FactionType.SAFEZONE)) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            p.teleport(e.getFrom(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                        }
                    }.runTaskLater(Main.getInstance(), 1);
                } else {
                    Location location = FactionCommand.findSafeLocation(p,20,p.getWorld().getMaxHeight() - 2, p.getWorld().getMinHeight() + 2);
                    if (location == null) return;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            p.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
                        }
                    }.runTaskLater(Main.getInstance(), 1);
                }

            }
            if (faction.getType().equals(FactionType.PLAYER) && (user.hasTimer("pvp") || user.hasTimer("starting"))) {
                if (Faction.getAt(e.getFrom()) == null || !Objects.requireNonNull(Faction.getAt(e.getFrom())).getType().equals(FactionType.PLAYER)) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            p.teleport(e.getFrom(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                        }
                    }.runTaskLater(Main.getInstance(), 1);
                } else {
                    Location location = FactionCommand.findSafeLocation(p,20,p.getWorld().getMaxHeight() - 2, p.getWorld().getMinHeight() + 2);
                    if (location == null) return;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            p.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
                        }
                    }.runTaskLater(Main.getInstance(), 1);
                }
            }
        }

        // Only run these on block-to-block movement
        if (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {
            processHome(p);
            processLogout(p);
            if (notAllowed(p, e.getTo(), e.getFrom())) {
                e.setCancelled(true);
                return;
            }
        }



        if (user.getModMode() != null) return;
        if (user.hasTimer("pvp") || user.hasTimer("starting")) return;
        if (!user.hasFaction()) return;
        Faction faction = Faction.get(user.getFaction());
        if (faction == null) return;

        if (user.getStuckLocation() != null && user.hasTimer("stuck")) {
            if (user.getStuckLocation().distance(p.getLocation()) > 5) {
                user.setStuckLocation(null);
                user.removeTimer("stuck");
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.stuck.cancelled"))));
                return;
            }
        }

        if (Main.getInstance().getRunningConquest() != null) {
            RunningConquest runningConquest = Main.getInstance().getRunningConquest();
            Conquest conquest = runningConquest.getConquest();
            Location location = p.getLocation().clone();
            location.setY(0);

            if (conquest.getRed().getCuboid().isIn(location)) {
                if (runningConquest.getRedControllingPlayer() == null) {
                    runningConquest.setRedControllingPlayer(p.getUniqueId());
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        User play = User.get(player);
                        if (!play.hasFaction()) {
                            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.control")).replace("%faction%", faction.getName()).replace("%zone%", "&c" + conquest.getRed().getName())));
                            continue;
                        }
                        if (!play.getFaction().equals(user.getFaction())) {
                            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.control")).replace("%faction%", faction.getName()).replace("%zone%", "&c" + conquest.getRed().getName())));
                            continue;
                        }
                        if (!play.getUUID().equals(p.getUniqueId())) {
                            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.control-team")).replace("%zone%", "&c" + conquest.getRed().getName())));
                            continue;
                        }
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.control-player")).replace("%zone%", "&c" + conquest.getRed().getName())));
                    }
                }
            } else {
                if (runningConquest.getRedControllingPlayer() != null && runningConquest.getRedControllingPlayer().equals(p.getUniqueId())) {
                    runningConquest.resetZone(user, conquest.getRed(), true);
                }
            }

            if (conquest.getGreen().getCuboid().isIn(location)) {
                if (runningConquest.getGreenControllingPlayer() == null) {
                    runningConquest.setGreenControllingPlayer(p.getUniqueId());
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        User play = User.get(player);
                        if (!play.hasFaction()) {
                            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.control")).replace("%faction%", faction.getName()).replace("%zone%", "&a" + conquest.getGreen().getName())));
                            continue;
                        }
                        if (!play.getFaction().equals(user.getFaction())) {
                            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.control")).replace("%faction%", faction.getName()).replace("%zone%", "&a" + conquest.getGreen().getName())));
                            continue;
                        }
                        if (!play.getUUID().equals(p.getUniqueId())) {
                            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.control-team")).replace("%zone%", "&a" + conquest.getGreen().getName())));
                            continue;
                        }
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.control-player")).replace("%zone%", "&a" + conquest.getGreen().getName())));
                    }
                }
            } else {
                if (runningConquest.getGreenControllingPlayer() != null && runningConquest.getGreenControllingPlayer().equals(p.getUniqueId())) {
                    runningConquest.resetZone(user, conquest.getGreen(), true);
                }
            }


            if (conquest.getBlue().getCuboid().isIn(location)) {
                if (runningConquest.getBlueControllingPlayer() == null) {
                    runningConquest.setBlueControllingPlayer(p.getUniqueId());
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        User play = User.get(player);
                        if (!play.hasFaction()) {
                            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.control")).replace("%faction%", faction.getName()).replace("%zone%", "&9" + conquest.getBlue().getName())));
                            continue;
                        }
                        if (!play.getFaction().equals(user.getFaction())) {
                            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.control")).replace("%faction%", faction.getName()).replace("%zone%", "&9" + conquest.getBlue().getName())));
                            continue;
                        }
                        if (!play.getUUID().equals(p.getUniqueId())) {
                            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.control-team")).replace("%zone%", "&9" + conquest.getBlue().getName())));
                            continue;
                        }
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.control-player")).replace("%zone%", "&9" + conquest.getBlue().getName())));
                    }
                }
            } else {
                if (runningConquest.getBlueControllingPlayer() != null && runningConquest.getBlueControllingPlayer().equals(p.getUniqueId())) {
                    runningConquest.resetZone(user, conquest.getBlue(), true);
                }
            }


            if (conquest.getYellow().getCuboid().isIn(location)) {
                if (runningConquest.getYellowControllingPlayer() == null) {
                    runningConquest.setYellowControllingPlayer(p.getUniqueId());
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        User play = User.get(player);
                        if (!play.hasFaction()) {
                            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.control")).replace("%faction%", faction.getName()).replace("%zone%", "&e" + conquest.getYellow().getName())));
                            continue;
                        }
                        if (!play.getFaction().equals(user.getFaction())) {
                            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.control")).replace("%faction%", faction.getName()).replace("%zone%", "&e" + conquest.getYellow().getName())));
                            continue;
                        }
                        if (!play.getUUID().equals(p.getUniqueId())) {
                            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.control-team")).replace("%zone%", "&e" + conquest.getYellow().getName())));
                            continue;
                        }
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.conquest.control-player")).replace("%zone%", "&e" + conquest.getYellow().getName())));
                    }
                }
            } else {
                if (runningConquest.getYellowControllingPlayer() != null && runningConquest.getYellowControllingPlayer().equals(p.getUniqueId())) {
                    runningConquest.resetZone(user, conquest.getYellow(), true);
                }
            }
        }

        if (!Main.getInstance().runningKOTHS.isEmpty()) {
            for (RunningKOTH runningKOTH : Main.getInstance().runningKOTHS.values()) {
                KOTH koth = runningKOTH.getKOTH();
                Location location = p.getLocation().clone();
                location.setY(0);
                if (koth.getCuboid().isIn(location)) {
                    if (runningKOTH.getControllingPlayer() == null) {
                        runningKOTH.setControllingPlayer(p.getUniqueId());
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            User play = User.get(player);
                            if (!play.hasFaction()) {
                                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.koth.control")).replace("%faction%", faction.getName()).replace("%koth%", koth.getName())));
                                continue;
                            }
                            if (!play.getFaction().equals(user.getFaction())) {
                                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.koth.control")).replace("%faction%", faction.getName()).replace("%koth%", koth.getName())));
                                continue;
                            }
                            if (!play.getUUID().equals(p.getUniqueId())) {
                                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.koth.control-team")).replace("%koth%", koth.getName())));
                                continue;
                            }
                            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.koth.control-player")).replace("%koth%", koth.getName())));
                        }
                    }
                } else {
                    if (runningKOTH.getControllingPlayer() != null && runningKOTH.getControllingPlayer().equals(p.getUniqueId())) {
                        runningKOTH.resetControllingPlayer();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        user.updateClass();
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;
        processHome(e.getPlayer());
        processLogout(e.getPlayer());
        if (notAllowed(e.getPlayer(), e.getTo(), e.getFrom())) e.setCancelled(true);

        if (user.hasTimer("combat") && !Main.getInstance().getConfig().getBoolean("combat.allow-end-portal-enter")) {
            if (!e.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) return;
            e.setCancelled(true);
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.timer.player.end"))));
            return;
        }

        if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            Faction faction = Faction.getAt(e.getTo());
            if (faction == null) return;
            if (faction.getType().equals(FactionType.CONQUEST)) {
                e.setCancelled(true);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.enderpearl.conquest"))));
                if (!p.getGameMode().equals(GameMode.CREATIVE)) p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                user.removeTimer("ENDERPEARL");
                return;
            }
            if (faction.getType().equals(FactionType.SAFEZONE)) {
                e.setCancelled(true);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.enderpearl.safezone"))));
                if (!p.getGameMode().equals(GameMode.CREATIVE)) p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                user.removeTimer("ENDERPEARL");
                return;
            }
            if (faction.getType().equals(FactionType.KOTH)) {
                for (KOTH koth : Main.getInstance().koths.values()) {
                    if (koth.getFaction() == null) continue;
                    if (koth.getFaction().equals(faction.getUUID()) && (!koth.isPearlable())) {
                        e.setCancelled(true);
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.enderpearl.koth"))));
                        if (!p.getGameMode().equals(GameMode.CREATIVE)) p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                        user.removeTimer("ENDERPEARL");
                        return;
                    }
                }
            }
            return;
        }

        if (!e.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL) && !e.getCause().equals(PlayerTeleportEvent.TeleportCause.UNKNOWN)) return;
        if (Objects.requireNonNull(e.getFrom().getWorld()).getEnvironment() == World.Environment.THE_END &&
                Objects.requireNonNull(e.getTo().getWorld()).getEnvironment() == World.Environment.NORMAL) {
            Location endExit = Locations.get().getLocation("end.exit");
            if (endExit != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (p.getWorld().getEnvironment() == World.Environment.NORMAL) {
                            p.teleport(endExit, PlayerTeleportEvent.TeleportCause.END_PORTAL);
                        }
                    }
                }.runTaskLater(Main.getInstance(), 1);
            }
        }
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        user.updateClass();
        if (user.hasTimer("combat") && !Main.getInstance().getConfig().getBoolean("combat.allow-end-portal-enter")) {
            e.setCancelled(true);
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.timer.player.end"))));
            return;
        }
        if (!e.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) return;


        if (Objects.requireNonNull(Objects.requireNonNull(e.getTo()).getWorld()).getEnvironment() == World.Environment.THE_END) {
            Location endSpawn = Locations.get().getLocation("end.spawn");
            if (endSpawn != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.teleport(endSpawn, PlayerTeleportEvent.TeleportCause.END_PORTAL);
                    }
                }.runTaskLater(Main.getInstance(), 1);
            }
        }
    }

    private void processHome(Player p) {
        User user = User.get(p);
        if (user.hasTimer("home")) {
            if (user.getTimer("home").getDuration().doubleValue() <= 0.01) return;
            user.removeTimer("home");
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.teleport-cancelled"))));
        }
    }

    private void processLogout(Player p) {
        User user = User.get(p);
        if (user.hasTimer("logout")) {
            if (user.getTimer("logout").getDuration().doubleValue() <= 0.01) return;
            user.removeTimer("logout");
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.logout.cancelled"))));
        }
    }

    private PlayerTimer getActiveTimer(User user) {
        if (user.hasTimer("pvp")) return user.getTimer("pvp");
        if (user.hasTimer("starting")) return user.getTimer("starting");
        return null;
    }

    private void setTimerPaused(User user, boolean paused) {
        PlayerTimer timer = getActiveTimer(user);
        if (timer != null) timer.setPaused(paused);
    }

    private void sendLeaveEnter(Player p, String leave, String enter) {
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.leaving")).replace("%faction%", leave)));
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.entering")).replace("%faction%", enter)));
    }

    private boolean notAllowed(Player p, Location to, Location from) {
        User user = User.get(p);
        final int WARZONE_OVERWORLD = Main.getInstance().getConfig().getInt("factions.sizes.worlds.default.warzone");
        final int WARZONE_NETHER = Main.getInstance().getConfig().getInt("factions.sizes.worlds.nether.warzone");
        Faction factionFrom = Faction.getAt(from);
        Faction factionTo = Faction.getAt(to);

        if (UserClaimEvents.isPastBorder(to)) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.border.reached"))));
            return true;
        }

        int used = (p.getWorld().getEnvironment() == World.Environment.CUSTOM ||
                p.getWorld().getEnvironment() == World.Environment.NORMAL)
                ? WARZONE_OVERWORLD : WARZONE_NETHER;

        if (factionFrom == null && factionTo == null) {
            setTimerPaused(user, false);

            if (p.getWorld().getEnvironment() == World.Environment.THE_END) return false;
            String areaChange = checkAreaChange(from, to, used);
            if (areaChange == null) return false;

            if (areaChange.equalsIgnoreCase("WARZONE")) {
                sendLeaveEnter(p, Locale.get().getString("faction.wilderness") + " " + Locale.get().getString("faction.deathban"), Locale.get().getString("faction.warzone") + " " + Locale.get().getString("faction.deathban"));
            } else if (areaChange.equalsIgnoreCase("wilderness")) {
                sendLeaveEnter(p, Locale.get().getString("faction.warzone") + " " + Locale.get().getString("faction.deathban"), Locale.get().getString("faction.wilderness") + " " + Locale.get().getString("faction.deathban"));
            }
        } else if (factionFrom == null) {
            if (factionTo.getType().equals(FactionType.PLAYER)) {
                if (user.hasTimer("pvp") || user.hasTimer("starting")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.movement.deny.pvp"))));
                    return true;
                }
            } else if (factionTo.getType().equals(FactionType.SAFEZONE)) {
                if (user.hasTimer("combat")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.movement.deny.combat"))));
                    return true;
                }
                setTimerPaused(user, true);
            }
            String leave = isWilderness(from, used) ? Locale.get().getString("faction.wilderness") + " " + Locale.get().getString("faction.deathban") : Locale.get().getString("faction.warzone") + " " + Locale.get().getString("faction.deathban");
            sendLeaveEnter(p, leave, factionTo.getValidName(p, true));
        } else if (factionTo == null) {
            sendLeaveEnter(p, factionFrom.getValidName(p, true),
                    isWilderness(from, used) ? Locale.get().getString("faction.wilderness") + " " + Locale.get().getString("faction.deathban") : Locale.get().getString("faction.warzone") + " " + Locale.get().getString("faction.deathban"));

            if (factionFrom.getType().equals(FactionType.SAFEZONE)) setTimerPaused(user, false);
        } else {
            if (factionFrom == factionTo) {
                return false;
            }
            if (factionTo.getType().equals(FactionType.PLAYER)) {
                if (user.hasTimer("pvp") || user.hasTimer("starting")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.movement.deny.pvp"))));
                    return true;
                }
            } else if (factionTo.getType().equals(FactionType.SAFEZONE)) {
                if (user.hasTimer("combat")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.movement.deny.combat"))));
                    return true;
                }
            }
            sendLeaveEnter(p, factionFrom.getValidName(p, true), factionTo.getValidName(p, true));
            setTimerPaused(user, factionTo.getType().equals(FactionType.SAFEZONE));
        }

        // --- Safezone Hide/Show Logic ---
        boolean hideSafezonePlayers = Main.getInstance().getConfig().getBoolean("features.hidden-safezone-players");
        boolean notInModMode = user.getModMode() == null;

        // Hide players when entering a safezone
        if (factionTo != null && factionTo.getType().equals(FactionType.SAFEZONE) && hideSafezonePlayers && notInModMode) {
            Cuboid cuboid = factionTo.getCuboidAtLocation(to);
            if (cuboid != null) {
                for (Player other : cuboid.getPlayersCurrentlyIn()) {
                    if (!other.equals(p)) {
                        p.hidePlayer(Main.getInstance(), other);
                        other.hidePlayer(Main.getInstance(), p);
                    }
                }
            }
        }

        // Show players when leaving a safezone
        if (factionFrom != null && factionFrom.getType().equals(FactionType.SAFEZONE) && hideSafezonePlayers && notInModMode) {
            Cuboid cuboid = factionFrom.getCuboidAtLocation(from);
            if (cuboid != null) {
                for (Player other : cuboid.getPlayersCurrentlyIn()) {
                    if (!other.equals(p)) {
                        p.showPlayer(Main.getInstance(), other);
                        other.showPlayer(Main.getInstance(), p);
                    }
                }
            }
        }

        return false;
    }

    private String checkAreaChange(Location from, Location to, int used) {
        boolean wasInWilderness = isWilderness(from, used);
        boolean isInWilderness = isWilderness(to, used);

        if (wasInWilderness && !isInWilderness) {
            return "warzone";
        } else if (!wasInWilderness && isInWilderness) {
            return "wilderness";
        }
        return null;
    }

    private boolean isWilderness(Location loc, int AREA) {
        if (Objects.requireNonNull(loc.getWorld()).getEnvironment().equals(World.Environment.THE_END)) return true;
        int absX = Math.abs(loc.getBlockX());
        int absZ = Math.abs(loc.getBlockZ());
        return absX >= AREA || absZ >= AREA;
    }
}
