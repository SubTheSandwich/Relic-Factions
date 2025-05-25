package me.sub.RelicFactions.Files.Data;

import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.util.Objects;

public class ServerTimer {

    private String name;
    private BigDecimal duration;
    private boolean paused;
    private final BukkitTask task;

    public ServerTimer(String name, BigDecimal duration) {
        this.name = name;
        this.duration = duration;
        this.paused = false;
        task = tick();
        Main.getInstance().serverTimers.put(name.toUpperCase(), this);
    }

    public ServerTimer(String name, BigDecimal duration, boolean paused) {
        this.name = name;
        this.duration = duration;
        this.paused = paused;
        task = tick();
        Main.getInstance().serverTimers.put(name.toUpperCase(), this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ServerTimer that = (ServerTimer) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    public static boolean has(String name) {
        return Main.getInstance().serverTimers.containsKey(name.toUpperCase());
    }

    public static ServerTimer get(String name) {
        return Main.getInstance().serverTimers.getOrDefault(name.toUpperCase(), null);
    }

    public static ServerTimer remove(String name) {
        return Main.getInstance().serverTimers.remove(name.toUpperCase());
    }

    public static ServerTimer add(ServerTimer serverTimer) {
        return Main.getInstance().serverTimers.put(serverTimer.getName().toUpperCase(), serverTimer);
    }

    public void cancel() {
        task.cancel();
        Main.getInstance().serverTimers.remove(name.toUpperCase());
    }

    private BukkitTask tick() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (paused) return;
                duration = duration.subtract(BigDecimal.valueOf(0.05));
                if (duration.compareTo(BigDecimal.ZERO) <= 0) {
                    Bukkit.broadcastMessage(C.chat(Locale.get().getString("events.timer.server.expire." + name) == null ? Objects.requireNonNull(Locale.get().getString("events.timer.server.expire.default")).replace("%name%", name) : Objects.requireNonNull(Locale.get().getString("events.timer.server.expire." + name))));
                    cancel();
                    Main.getInstance().serverTimers.remove(name.toUpperCase());
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }
}
