package me.sub.RelicFactions.Files.Data;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
                if (Bukkit.getPlayer(uuid) == null) {
                    cancel();
                    return;
                }
                Player player = Bukkit.getPlayer(uuid);
                if (paused) return;
                duration = duration.subtract(BigDecimal.valueOf(0.05));
                if (duration.doubleValue() > 0) return;
                Objects.requireNonNull(player).sendMessage(Objects.requireNonNull(Locale.get().getString("events.timer.expire." + timer.name().toLowerCase()) == null ? Objects.requireNonNull(Locale.get().getString("events.timer.expire.default")).replace("%timer%", timer.name()) : Locale.get().getString("events.timer.expire." + timer.name().toLowerCase())));
                User user = User.get(player);
                user.removeTimer(timer.name());
                cancel();
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }
}
