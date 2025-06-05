package me.sub.RelicFactions.Events.Player.Movement;

import me.sub.RelicFactions.Events.Player.Interact.UserClaimEvents;
import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.KOTH;
import me.sub.RelicFactions.Files.Classes.RunningKOTH;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.PlayerTimer;
import me.sub.RelicFactions.Files.Enums.FactionType;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Files.Normal.Locations;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.*;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class UserMoveEvent implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (e.getTo() == null) return;

        // Only run these on block-to-block movement
        if (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {
            processHome(p);
            processLogout(p);
            if (notAllowed(p, e.getTo(), e.getFrom())) {
                e.setCancelled(true);
                return;
            }
        }

        // KOTH logic runs on every movement (even within the same block)
        User user = User.get(p);
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

        if (Main.getInstance().runningKOTHS.isEmpty()) return;
        for (RunningKOTH runningKOTH : Main.getInstance().runningKOTHS.values()) {
            KOTH koth = runningKOTH.getKOTH();
            Location location = p.getLocation().clone();
            location.setY(0);
            if (koth.getCuboid().isIn(location)) {
                if (runningKOTH.getControllingPlayer() == null) {
                    runningKOTH.setControllingPlayer(p.getUniqueId());
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        User play = User.get(player);
                        if (!play.isGlobalChat()) continue;
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

    @EventHandler
    public void onMove(PlayerTeleportEvent e) {
        if (e.getTo() == null) return;
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;
        processHome(e.getPlayer());
        processLogout(e.getPlayer());
        if (notAllowed(e.getPlayer(), e.getTo(), e.getFrom())) e.setCancelled(true);

        Player p = e.getPlayer();
        User user = User.get(p);
        if (user.hasTimer("combat") && !Main.getInstance().getConfig().getBoolean("combat.allow-end-portal-enter")) {
            e.setCancelled(true);
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.timer.player.end"))));
            return;
        }

        if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            Faction faction = Faction.getAt(e.getTo());
            if (faction == null) return;
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

        Locations locations = new Locations();
        if (!e.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL) && !e.getCause().equals(PlayerTeleportEvent.TeleportCause.UNKNOWN)) return;
        if (Objects.requireNonNull(e.getFrom().getWorld()).getEnvironment() == World.Environment.THE_END &&
                Objects.requireNonNull(e.getTo().getWorld()).getEnvironment() == World.Environment.NORMAL) {
            Location endExit = locations.get().getLocation("end.exit");
            if (endExit != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (p.getWorld().getEnvironment() == World.Environment.NORMAL) {
                            p.teleport(endExit);
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
        if (user.hasTimer("combat") && !Main.getInstance().getConfig().getBoolean("combat.allow-end-portal-enter")) {
            e.setCancelled(true);
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.timer.player.end"))));
            return;
        }
        Locations locations = new Locations();
        if (!e.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) return;


        if (Objects.requireNonNull(Objects.requireNonNull(e.getTo()).getWorld()).getEnvironment() == World.Environment.THE_END) {
            Location endSpawn = locations.get().getLocation("end.spawn");
            if (endSpawn != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.teleport(endSpawn);
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
            if (factionFrom == factionTo) return false;
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
