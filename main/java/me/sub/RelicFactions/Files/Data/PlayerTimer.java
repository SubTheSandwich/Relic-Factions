package me.sub.RelicFactions.Files.Data;

import me.sub.RelicFactions.Commands.User.FactionCommand;
import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class PlayerTimer {

    private final UUID uuid;
    private final Timer timer;
    private BigDecimal duration;
    private boolean paused;

    public PlayerTimer(UUID uuid, Timer timer) {
        this.uuid = uuid;
        this.timer = timer;
        this.duration = BigDecimal.valueOf(timer.getDuration());
        paused = false;
        tick();
    }

    public PlayerTimer(UUID uuid, Timer timer, boolean paused) {
        this.uuid = uuid;
        this.timer = timer;
        this.duration = BigDecimal.valueOf(timer.getDuration());
        this.paused = paused;
        tick();
    }

    public PlayerTimer(UUID uuid, Timer timer, BigDecimal duration) {
        this.uuid = uuid;
        this.timer = timer;
        this.duration = duration;
        paused = false;
        tick();
    }

    public PlayerTimer(UUID uuid, Timer timer, BigDecimal duration, boolean paused) {
        this.uuid = uuid;
        this.timer = timer;
        this.duration = duration;
        this.paused = paused;
        tick();
    }


    public UUID getUUID() {
        return uuid;
    }

    public Timer getTimer() {
        return timer;
    }

    public BigDecimal getDuration() {
        return duration;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void setDuration(BigDecimal duration) {
        this.duration = duration;
    }

    public void setDuration(int duration) {
        this.duration = BigDecimal.valueOf(duration);
    }

    public void tick() {
        new BukkitRunnable() {
            @Override
            public void run() {
                User user = User.get(uuid);
                if (user == null) {
                    cancel();
                    return;
                }
                if (!user.hasTimer(timer.name())) {
                    cancel();
                    return;
                }
                if (Bukkit.getPlayer(uuid) == null) {
                    user.removeTimer("home");
                    user.removeTimer("logout");
                    user.removeTimer("stuck");
                    cancel();
                    return;
                }
                Player player = Bukkit.getPlayer(uuid);
                if (paused) return;
                duration = duration.subtract(BigDecimal.valueOf(0.05));
                if (duration.doubleValue() > 0) return;
                if (timer.name().equalsIgnoreCase("LOGOUT")) {
                    if (user.hasTimer("combat")) user.removeTimer("combat");
                    Objects.requireNonNull(player).kick(Component.text(C.chat(Objects.requireNonNull(Locale.get().getString("commands.logout.success")))));
                    user.removeTimer(timer.name());
                    cancel();
                    return;
                }
                if (timer.name().equalsIgnoreCase("STUCK")) {
                    if (user.getStuckLocation() == null) {
                        user.removeTimer(timer.name());
                        cancel();
                        return;
                    }
                    Objects.requireNonNull(player).sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.stuck.starting"))));
                    Location location = FactionCommand.findSafeLocation(player,20,player.getWorld().getMaxHeight() - 2, player.getWorld().getMinHeight() + 2);
                    if (location == null) {
                        player.sendMessage(Objects.requireNonNull(Locale.get().getString("commands.faction.stuck.none")));
                        cancel();
                        user.removeTimer(timer.name());
                        user.setStuckLocation(null);
                        return;
                    }
                    player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.faction.stuck.success"))));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
                        }
                    }.runTaskLater(Main.getInstance(), 1);
                    cancel();
                    user.removeTimer(timer.name());
                    user.setStuckLocation(null);
                    return;
                }
                if (timer.name().equalsIgnoreCase("HOME")) {
                    if (!user.hasFaction()) {
                        user.removeTimer("home");
                        cancel();
                        return;
                    }
                    Faction faction = Faction.get(user.getFaction());
                    if (faction.getHome() == null) {
                        user.removeTimer("home");
                        cancel();
                        return;
                    }
                    Location home = faction.getHome();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Objects.requireNonNull(player).teleport(home, PlayerTeleportEvent.TeleportCause.COMMAND);
                        }
                    }.runTaskLater(Main.getInstance(), 1);
                    Objects.requireNonNull(player).sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.warping")).replace("%faction%", faction.getName())));
                    user.removeTimer(timer.name());
                    cancel();
                    return;
                }
                Objects.requireNonNull(player).sendMessage(Objects.requireNonNull(C.chat(Locale.get().getString("events.timer.player.expire." + timer.name().toLowerCase()) == null ? Objects.requireNonNull(Locale.get().getString("events.timer.player.expire.default")).replace("%timer%", timer.name()) : Objects.requireNonNull(Locale.get().getString("events.timer.expire." + timer.name().toLowerCase())))));
                user.removeTimer(timer.name());
                cancel();
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }
}
