package me.sub.RelicFactions.Files.Data;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class CustomTimer {

    private final UUID uuid;
    private final String name;
    private BigDecimal duration;
    private boolean paused;

    public CustomTimer(UUID uuid, String name, BigDecimal duration) {
        this.uuid = uuid;
        this.name = name;
        this.duration = duration;
        this.paused = false;
        tick();
    }

    public CustomTimer(UUID uuid, String name, BigDecimal duration, boolean paused) {
        this.uuid = uuid;
        this.name = name;
        this.duration = duration;
        this.paused = paused;
        tick();
    }

    @Override
    public String toString() {
        return "CustomTimer{" +
                "name='" + name + '\'' +
                '}';
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getDuration() {
        return duration;
    }

    public void setDuration(BigDecimal duration) {
        this.duration = duration;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
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
                if (!user.hasCustomTimer(name)) {
                    cancel();
                    return;
                }
                if (Bukkit.getPlayer(uuid) == null) {
                    cancel();
                    return;
                }
                Player player = Bukkit.getPlayer(uuid);
                if (paused) return;
                duration = duration.subtract(BigDecimal.valueOf(0.05));
                if (duration.doubleValue() > 0) return;
                Objects.requireNonNull(player).sendMessage(Objects.requireNonNull(C.chat(Objects.requireNonNull(Locale.get().getString("events.timer.player.expire.default")).replace("%timer%", name))));
                user.removeCustomTimer(name);
                cancel();
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }

    public String serialize() {
        return uuid + "," + name.replace(",", "\\,") + "," + duration + "," + paused;
    }

    public static CustomTimer deserialize(String s) {
        String[] parts = s.split("(?<!\\\\),");
        if (parts.length != 4) throw new IllegalArgumentException("Invalid format");
        UUID uuid = UUID.fromString(parts[0]);
        String name = parts[1].replace("\\,", ",");
        BigDecimal duration = new BigDecimal(parts[2]);
        boolean paused = Boolean.parseBoolean(parts[3]);
        return new CustomTimer(uuid, name, duration, paused);
    }
}
