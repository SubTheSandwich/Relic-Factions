package me.sub.RelicFactions.Events.Player.Interact;

import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.Claim;
import me.sub.RelicFactions.Files.Data.Cuboid;
import me.sub.RelicFactions.Files.Enums.FactionType;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class UserClaimEvents implements Listener {

    @EventHandler
    public void onClaim(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Action a = e.getAction();
        User user = User.get(p);
        if (a.equals(Action.PHYSICAL)) return;
        if (user.getClaim() == null) return;
        if (e.getItem() == null) return;
        if (!e.getItem().hasItemMeta()) return;
        if (!e.getItem().getType().equals(Material.DIAMOND_HOE)) return;
        if (!C.serialize(Objects.requireNonNull(e.getItem().getItemMeta()).displayName()).equalsIgnoreCase(C.chat("&bClaiming Wand"))) return;
        e.setCancelled(true);
        Claim claim = user.getClaim();
        Faction faction = Faction.get(claim.getUUID());
        switch (a) {
            case LEFT_CLICK_AIR -> {
                if (!p.isSneaking()) return;
                if (claim.isAttempting()) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.trying"))));
                    return;
                }
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.attempting"))));
                claim.setAttempting(true);
                handleExecuteClaim(p);
            }
            case RIGHT_CLICK_AIR -> {
                if (!p.isSneaking()) return;
                resetPillars(p);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.cancelled"))));
                p.getInventory().remove(Claim.getWand());
                user.setClaim(null);
            }
            case LEFT_CLICK_BLOCK -> {
                if (isPastBorder(Objects.requireNonNull(e.getClickedBlock()).getLocation())) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.error.border"))));
                    return;
                }
                if (faction.getType().equals(FactionType.PLAYER)) {
                    if (isInvalid(Objects.requireNonNull(e.getClickedBlock()).getLocation())) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.error.location"))));
                        return;
                    }
                }
                if (claim.getCornerTwo() != null) {
                    if (!Objects.equals(claim.getCornerTwo().getWorld(), Objects.requireNonNull(e.getClickedBlock()).getWorld())) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.world"))));
                        return;
                    }
                }
                resetPillars(p);
                claim.setCornerOne(Objects.requireNonNull(e.getClickedBlock()).getLocation());
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.set.one"))));
                handlePillars(p);
                claim.getCornerOne().setY(0);
                sendMessage(p);
            }
            case RIGHT_CLICK_BLOCK -> {
                if (isPastBorder(Objects.requireNonNull(e.getClickedBlock()).getLocation())) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.error.border"))));
                    return;
                }
                if (faction.getType().equals(FactionType.PLAYER)) {
                    if (isInvalid(Objects.requireNonNull(e.getClickedBlock()).getLocation())) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.error.location"))));
                        return;
                    }
                }
                if (claim.getCornerOne() != null) {
                    if (!Objects.equals(Objects.requireNonNull(e.getClickedBlock()).getWorld(), claim.getCornerOne().getWorld())) {
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.set.fail.world"))));
                        return;
                    }
                }
                resetPillars(p);
                claim.setCornerTwo(Objects.requireNonNull(e.getClickedBlock()).getLocation());
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.set.two"))));
                handlePillars(p);
                claim.getCornerTwo().setY(0);
                sendMessage(p);
            }
        }
    }

    private void handleExecuteClaim(Player p) {
        User user = User.get(p);
        Claim claim = user.getClaim();
        claim.setAttempting(true);
        if (Faction.get(claim.getUUID()) == null) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.error"))));
            user.setClaim(null);
            p.getInventory().remove(Claim.getWand());
            return;
        }
        if (claim.getCornerOne() == null || claim.getCornerTwo() == null) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.error.not-selected"))));
            claim.setAttempting(false);
            return;
        }
        Faction faction = Faction.get(claim.getUUID());
        Cuboid area = new Cuboid(claim.getCornerOne(), claim.getCornerTwo());
        if (area.isPastBorder()) {
            claim.setAttempting(false);
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.error.border"))));
            return;
        }
        for (Faction tested : Main.getInstance().factions.values()) {
            if (tested.getClaims() == null) continue;
            if (tested.getClaims().isEmpty()) continue;
            for (Cuboid cuboid : tested.getClaims()) {
                if (cuboid.isPastBorder()) {
                    claim.setAttempting(false);
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.error.border"))));
                    return;
                }
                if (cuboid.collidesWith(area)) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.error.in-claim"))));
                    claim.setAttempting(false);
                    return;
                }
            }
        }
        if (!faction.getType().equals(FactionType.PLAYER)) {
            ArrayList<Cuboid> claims = new ArrayList<>(faction.getClaims());
            claims.add(area);
            faction.setClaims(claims);
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.success"))));
            user.setClaim(null);
            HashMap<Faction, List<Cuboid>> map = new HashMap<>();
            List<Cuboid> c = new ArrayList<>();
            c.add(area);
            map.put(faction, c);
            user.setMap(map);
            p.getInventory().remove(Claim.getWand());
            return;
        }
        if (!faction.getClaims().isEmpty()) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.have"))));
            claim.setAttempting(false);
            return;
        }

        if (area.getXWidth() < 5 || area.getZWidth() < 5 || area.getXWidth() > 256 || area.getZWidth() > 256) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.error.invalid-size"))));
            claim.setAttempting(false);
            return;
        }

        double cost = getPrice(area);
        if (cost > faction.getBalance().doubleValue()) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.error.cost"))));
            claim.setAttempting(false);
            return;
        }
        ArrayList<Cuboid> claims = new ArrayList<>(faction.getClaims());
        claims.add(area);
        faction.setClaims(claims);
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.success"))));
        user.setClaim(null);

        HashMap<Faction, List<Cuboid>> map = new HashMap<>();
        List<Cuboid> c = new ArrayList<>();
        c.add(area);
        map.put(faction, c);
        user.setMap(map);

        p.getInventory().remove(Claim.getWand());
        faction.setBalance(faction.getBalance().subtract(BigDecimal.valueOf(cost)));
    }

    private void resetPillars(Player p) {
        User user = User.get(p);
        Claim claim = user.getClaim();
        if (Faction.get(claim.getUUID()) == null) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.error"))));
            user.setClaim(null);
            p.getInventory().remove(Claim.getWand());
            return;
        }
        if (claim.getCornerOne() != null) {
            for (int i = Objects.requireNonNull(claim.getCornerOne().getWorld()).getMinHeight(); i < claim.getCornerOne().getWorld().getMaxHeight(); i++) {
                Block block = claim.getCornerOne().getWorld().getBlockAt(claim.getCornerOne().getBlockX(), i, claim.getCornerOne().getBlockZ());
                if (!block.isEmpty()) continue;
                p.sendBlockChange(block.getLocation(), Material.AIR.createBlockData());
            }
        }
        if (claim.getCornerTwo() != null) {
            for (int i = Objects.requireNonNull(claim.getCornerTwo().getWorld()).getMinHeight(); i < claim.getCornerTwo().getWorld().getMaxHeight(); i++) {
                Block block = claim.getCornerTwo().getWorld().getBlockAt(claim.getCornerTwo().getBlockX(), i, claim.getCornerTwo().getBlockZ());
                if (!block.isEmpty()) continue;
                p.sendBlockChange(block.getLocation(), Material.AIR.createBlockData());
            }
        }
        if (claim.getCornerOne() != null && claim.getCornerTwo() != null) {
            World world = claim.getCornerOne().getWorld();
            int minY = Objects.requireNonNull(world).getMinHeight();
            int maxY = world.getMaxHeight();

            for (int i = minY; i < maxY; i++) {
                Block block = world.getBlockAt(
                        claim.getCornerOne().getBlockX(), i, claim.getCornerTwo().getBlockZ());
                if (!block.isEmpty()) continue;
                p.sendBlockChange(block.getLocation(), Material.AIR.createBlockData());
            }

            for (int i = minY; i < maxY; i++) {
                Block block = world.getBlockAt(
                        claim.getCornerTwo().getBlockX(), i, claim.getCornerOne().getBlockZ());
                if (!block.isEmpty()) continue;
                p.sendBlockChange(block.getLocation(), Material.AIR.createBlockData());
            }
        }
    }

    private void handlePillars(Player p) {
        User user = User.get(p);
        Claim claim = user.getClaim();
        if (Faction.get(claim.getUUID()) == null) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.error"))));
            user.setClaim(null);
            p.getInventory().remove(Claim.getWand());
            return;
        }
        if (claim.getCornerOne() != null) {
            for (int i = Objects.requireNonNull(claim.getCornerOne().getWorld()).getMinHeight(); i < claim.getCornerOne().getWorld().getMaxHeight(); i++) {
                Block block = claim.getCornerOne().getWorld().getBlockAt(claim.getCornerOne().getBlockX(), i, claim.getCornerOne().getBlockZ());
                if (!block.isEmpty()) continue;
                if (i % 4 == 0) {
                    p.sendBlockChange(block.getLocation(), claim.getPillarType().createBlockData());
                } else {
                    p.sendBlockChange(block.getLocation(), Material.GLASS.createBlockData());
                }
            }
        }
        if (claim.getCornerTwo() != null) {
            for (int i = Objects.requireNonNull(claim.getCornerTwo().getWorld()).getMinHeight(); i < claim.getCornerTwo().getWorld().getMaxHeight(); i++) {
                Block block = claim.getCornerTwo().getWorld().getBlockAt(claim.getCornerTwo().getBlockX(), i, claim.getCornerTwo().getBlockZ());
                if (!block.isEmpty()) continue;
                if (i % 4 == 0) {
                    p.sendBlockChange(block.getLocation(), claim.getPillarType().createBlockData());
                } else {
                    p.sendBlockChange(block.getLocation(), Material.GLASS.createBlockData());
                }
            }
        }
        if (claim.getCornerOne() != null && claim.getCornerTwo() != null) {
            World world = claim.getCornerOne().getWorld();
            int minY = Objects.requireNonNull(world).getMinHeight();
            int maxY = world.getMaxHeight();

            for (int i = minY; i < maxY; i++) {
                Block block = world.getBlockAt(
                        claim.getCornerOne().getBlockX(),
                        i,
                        claim.getCornerTwo().getBlockZ()
                );
                if (!block.isEmpty()) continue;
                if (i % 4 == 0) {
                    p.sendBlockChange(block.getLocation(), claim.getPillarType().createBlockData());
                } else {
                    p.sendBlockChange(block.getLocation(), Material.GLASS.createBlockData());
                }
            }

            for (int i = minY; i < maxY; i++) {
                Block block = world.getBlockAt(
                        claim.getCornerTwo().getBlockX(),
                        i,
                        claim.getCornerOne().getBlockZ()
                );
                if (!block.isEmpty()) continue;
                if (i % 4 == 0) {
                    p.sendBlockChange(block.getLocation(), claim.getPillarType().createBlockData());
                } else {
                    p.sendBlockChange(block.getLocation(), Material.GLASS.createBlockData());
                }
            }
        }
    }

    private void sendMessage(Player p) {
        User user = User.get(p);
        Claim claim = user.getClaim();
        if (Faction.get(claim.getUUID()) == null) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.error"))));
            user.setClaim(null);
            p.getInventory().remove(Claim.getWand());
            return;
        }
        if (claim.getCornerOne() == null || claim.getCornerTwo() == null) return;
        Cuboid area = new Cuboid(claim.getCornerOne(), claim.getCornerTwo());
        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("claiming.cost")).replace("%cost%", Main.getEconomy().format(getPrice(area))).replace("%sizeX%", area.getXWidth() + "").replace("%sizeZ%", area.getZWidth() + "")));
    }

    private boolean isInvalid(Location location) {
        return Math.abs(location.getBlockX()) < Main.getInstance().getConfig().getInt("factions.sizes.worlds.default.warzone") && Math.abs(location.getBlockZ()) < Main.getInstance().getConfig().getInt("factions.sizes.worlds.default.warzone");
    }

    public static boolean isPastBorder(Location location) {
        World.Environment environment = Objects.requireNonNull(location.getWorld()).getEnvironment();
        int border;
        switch (environment) {
            case CUSTOM, NORMAL -> border = Main.getInstance().getConfig().getInt("limiters.world-border");
            case NETHER -> border = Main.getInstance().getConfig().getInt("limiters.nether-border");
            case THE_END -> border = Main.getInstance().getConfig().getInt("limiters.end-border");
            default -> throw new IllegalArgumentException("An unknown argument occurred.");
        }
        if (border < 0) return false;

        int x = location.getBlockX();
        int z = location.getBlockZ();
        return Math.abs(x) > border || Math.abs(z) > border;
    }

    public double getPrice(Cuboid cuboid) {
        int minX = Math.min(cuboid.getPoint1().getBlockX(), cuboid.getPoint2().getBlockX());
        int maxX = Math.max(cuboid.getPoint1().getBlockX(), cuboid.getPoint2().getBlockX());
        int minZ = Math.min(cuboid.getPoint1().getBlockZ(), cuboid.getPoint2().getBlockZ());
        int maxZ = Math.max(cuboid.getPoint1().getBlockZ(), cuboid.getPoint2().getBlockZ());

        double BASE_PRICE = Main.getInstance().getConfig().getDouble("factions.claim.price.multiplier");
        int MIN_SIZE = 5;
        double PRICE_PER_BLOCK = Main.getInstance().getConfig().getDouble("factions.claim.price.per-block");

        int width = maxX - minX + 1;
        int depth = maxZ - minZ + 1;

        int totalBlocks = width * depth;
        double price = BASE_PRICE + ((totalBlocks - (MIN_SIZE * MIN_SIZE)) * PRICE_PER_BLOCK);
        return Math.max(price, BASE_PRICE);
    }
}
