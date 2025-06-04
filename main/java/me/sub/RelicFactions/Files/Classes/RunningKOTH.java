package me.sub.RelicFactions.Files.Classes;

import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class RunningKOTH {

    private final KOTH koth;
    private UUID controllingPlayer;
    private BigDecimal timeLeft;

    public RunningKOTH(KOTH koth) {
        this.koth = koth;
        controllingPlayer = null;
        timeLeft = BigDecimal.valueOf(koth.getTime() * 60L);
        start();
    }

    public KOTH getKOTH() {
        return koth;
    }

    public UUID getControllingPlayer() {
        return controllingPlayer;
    }

    public void setControllingPlayer(UUID controllingPlayer) {
        this.controllingPlayer = controllingPlayer;
    }

    public BigDecimal getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(BigDecimal timeLeft) {
        this.timeLeft = timeLeft;
    }

    private void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (Main.getInstance().runningKOTHS.getOrDefault(koth.getUUID(), null) == null) {
                    cancel();
                    return;
                }
                if (controllingPlayer == null) return;
                User control = User.get(controllingPlayer);
                if (control == null) {
                    resetControllingPlayer();
                    return;
                }
                Player player = Bukkit.getPlayer(controllingPlayer);
                if (player == null) {
                    resetControllingPlayer();
                    return;
                }
                if (!control.hasFaction()) {
                    resetControllingPlayer();
                    return;
                }
                Faction faction = Faction.get(control.getFaction());
                if (faction == null) {
                    resetControllingPlayer();
                    return;
                }
                if (player.isDead()) {
                    resetControllingPlayer();
                    return;
                }
                if (control.isDeathBanned()) {
                    resetControllingPlayer();
                    return;
                }
                if (control.getModMode() != null) {
                    resetControllingPlayer();
                    return;
                }
                timeLeft = timeLeft.subtract(BigDecimal.valueOf(0.05));
                if (timeLeft.doubleValue() % 30 == 0 && timeLeft.doubleValue() > 1) {
                    Main.getInstance().sendGlobalMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.koth.controlling")).replace("%koth%", koth.getName()).replace("%time%", Timer.getMessageFormat(timeLeft))));
                    return;

                }
                if (timeLeft.doubleValue() > 0) return;
                Main.getInstance().sendGlobalMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.koth.end.captured")).replace("%koth%", koth.getName())));
                for (String s : Main.getInstance().getConfig().getStringList("koth.on-capture")) {
                    s = s.replace("%player%", User.get(controllingPlayer).getName());
                    s = s.replace("%faction%", Faction.get(User.get(controllingPlayer).getFaction()).getName());
                    s = s.replace("%koth%", koth.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
                }
                Main.getInstance().runningKOTHS.remove(koth.getUUID());
                faction.setKothCaptures(faction.getKothCaptures() + 1);
                cancel();
            }
        }.runTaskTimer(Main.getInstance(), 0,1);
    }

    public void resetControllingPlayer() {
        if (controllingPlayer == null) return;
        String fac = "A faction";
        User control = User.get(controllingPlayer);
        if (control == null) {
            Main.getInstance().sendGlobalMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.koth.knock")).replace("%faction%", fac).replace("%koth%", koth.getName())));
            controllingPlayer = null;
            setTimeLeft(BigDecimal.valueOf(koth.getTime() * 60L));
            return;
        }
        if (!control.hasFaction()) {
            Main.getInstance().sendGlobalMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.koth.knock")).replace("%faction%", fac).replace("%koth%", koth.getName())));
            controllingPlayer = null;
            setTimeLeft(BigDecimal.valueOf(koth.getTime()  * 60L));
            return;
        }
        Faction faction = Faction.get(control.getFaction());
        if (faction == null) {
            Main.getInstance().sendGlobalMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.koth.knock")).replace("%faction%", fac).replace("%koth%", koth.getName())));
            controllingPlayer = null;
            setTimeLeft(BigDecimal.valueOf(koth.getTime() * 60L));
            return;
        }
        fac = faction.getName();
        Main.getInstance().sendGlobalMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.koth.knock")).replace("%faction%", fac).replace("%koth%", koth.getName())));
        controllingPlayer = null;
        setTimeLeft(BigDecimal.valueOf(koth.getTime() * 60L));
    }
}
