package me.sub.RelicFactions.Files.Classes;

import me.sub.RelicFactions.Files.Data.Cuboid;
import me.sub.RelicFactions.Files.Data.MountainData;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

public class Mountain {

    private final MountainData mountainData;
    private final UUID uuid;
    private String name;
    private int defaultTime;
    private BigDecimal time;
    private Material type;
    private Location positionOne;
    private Location positionTwo;
    private boolean modified;

    public Mountain(MountainData mountainData) {
        this.mountainData = mountainData;
        this.uuid = mountainData.getUUID();
        this.name = mountainData.getName();
        this.defaultTime = mountainData.get().getInt("time");
        time = BigDecimal.valueOf(mountainData.get().getInt("time"));
        type = mountainData.get().getString("type") == null ? null : Material.matchMaterial(Objects.requireNonNull(mountainData.get().getString("type")));
        positionOne = mountainData.get().getLocation("positionOne") == null ? null : mountainData.get().getLocation("positionOne");
        positionTwo = mountainData.get().getLocation("positionTwo") == null ? null : mountainData.get().getLocation("positionTwo");
        modified = false;
        tick();
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        modified = true;
        this.name = name;
    }

    public int getDefaultTime() {
        return defaultTime;
    }

    public void setDefaultTime(int defaultTime) {
        modified = true;
        this.defaultTime = defaultTime;
    }

    public BigDecimal getTime() {
        return time;
    }

    public void setTime(BigDecimal time) {
        this.time = time;
    }

    public MountainData getMountainData() {
        return mountainData;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public Material getType() {
        return type;
    }

    public void setType(Material type) {
        modified = true;
        this.type = type;
    }

    public Location getPositionOne() {
        return positionOne;
    }

    public void setPositionOne(Location positionOne) {
        modified = true;
        this.positionOne = positionOne;
    }

    public Location getPositionTwo() {
        return positionTwo;
    }

    public void setPositionTwo(Location positionTwo) {
        modified = true;
        this.positionTwo = positionTwo;
    }

    public Cuboid getCuboid() {
        if (isSetup()) return new Cuboid(positionOne, positionTwo);
        return null;
    }

    public boolean isSetup() {
        return positionOne != null && positionTwo != null && type != null;
    }

    private void tick() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isSetup()) return;
                if (!Main.getInstance().mountains.containsKey(uuid)) {
                    cancel();
                    return;
                }
                if (time.doubleValue() >= defaultTime * 60) {
                    time = BigDecimal.ZERO;
                    Cuboid cuboid = new Cuboid(positionOne, positionTwo);
                    for (Iterator<Block> it = cuboid.blockList(); it.hasNext(); ) {
                        Block block = it.next();
                        block.setType(type);
                    }
                    Main.getInstance().sendGlobalMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.mountain.reset")).replace("%type%", C.capitalizeWord(type.name()))));
                    return;
                }
                time = time.add(new BigDecimal("0.05"));
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }

    public static Mountain get(UUID uuid) {
        return Main.getInstance().mountains.getOrDefault(uuid, null);
    }

    public static Mountain get(String name) {
        return Main.getInstance().mountainNameHolder.getOrDefault(name.toLowerCase(), null);
    }
}
