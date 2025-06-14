package me.sub.RelicFactions.Files.Data;

import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Enums.FactionType;
import me.sub.RelicFactions.Main.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class ClaimBufferManager {
    private final Map<UUID, Set<Location>> playerWalls = new HashMap<>();

    public void updateBufferWall(Player player) {
        removeBufferWall(player);

        User user = User.get(player);
        Location playerLoc = player.getLocation();
        int bufferDistance = Main.getInstance().getConfig().getInt("factions.claim.buffer.distance", 3);
        String wallType = Main.getInstance().getConfig().getString("factions.claim.buffer.type", "RED_STAINED_GLASS");
        Material wallMaterial = Material.matchMaterial(wallType);
        if (wallMaterial == null) wallMaterial = Material.RED_STAINED_GLASS;

        Set<Location> wallBlocks = new HashSet<>();

        // Find all claims near the player
        for (Faction claimFaction : Main.getInstance().factions.values()) {
            for (Cuboid claim : claimFaction.getClaims()) {
                // Only show wall if the player is not inside the claim, but is within buffer distance
                if (claim.isIn(playerLoc)) continue;

                // Only show wall for correct timer/faction type
                boolean showWall = (user.hasTimer("STARTING") || user.hasTimer("PVP")) && claimFaction.getType() == FactionType.PLAYER;
                if (user.hasTimer("COMBAT") && claimFaction.getType() == FactionType.SAFEZONE) {
                    showWall = true;
                }
                if (!showWall) continue;

                int xMin = claim.getXMin();
                int xMax = claim.getXMax();
                int zMin = claim.getZMin();
                int zMax = claim.getZMax();
                int py = playerLoc.getBlockY();

                // Loop over all edge blocks at the player's Y
                for (int x = xMin; x <= xMax; x++) {
                    for (int z : new int[]{zMin, zMax}) {
                        Location edge = new Location(playerLoc.getWorld(), x, py, z);
                        if (edge.distance(playerLoc) <= bufferDistance + 0.5) {
                            addWallBlocks(wallBlocks, edge, x, z, xMin, xMax, zMin, zMax);
                        }
                    }
                }
                for (int z = zMin + 1; z < zMax; z++) {
                    for (int x : new int[]{xMin, xMax}) {
                        Location edge = new Location(playerLoc.getWorld(), x, py, z);
                        if (edge.distance(playerLoc) <= bufferDistance + 0.5) {
                            addWallBlocks(wallBlocks, edge, x, z, xMin, xMax, zMin, zMax);
                        }
                    }
                }
            }
        }

        // Show the wall to the player
        for (Location loc : wallBlocks) {
            if (!Objects.requireNonNull(loc.getWorld()).getBlockAt(loc).isEmpty()) continue;
            player.sendBlockChange(loc, wallMaterial.createBlockData());
        }
        playerWalls.put(player.getUniqueId(), wallBlocks);
    }

    public void removeBufferWall(Player player) {
        Set<Location> lastWall = playerWalls.remove(player.getUniqueId());
        if (lastWall != null) {
            for (Location loc : lastWall) {
                Material realType = loc.getBlock().getType();
                player.sendBlockChange(loc, realType.createBlockData());
            }
        }
    }

    // Helper to add the correct wall shape
    private void addWallBlocks(Set<Location> wallBlocks, Location edge, int x, int z, int xMin, int xMax, int zMin, int zMax) {
        for (int dy = 0; dy < 3; dy++) {
            boolean isCorner = (x == xMin || x == xMax) && (z == zMin || z == zMax);
            if (isCorner) {
                // "U" shape: center, one along X, one along Z
                wallBlocks.add(edge.clone().add(0, dy, 0));
                wallBlocks.add(edge.clone().add(x == xMin ? 1 : -1, dy, 0));
                wallBlocks.add(edge.clone().add(0, dy, z == zMin ? 1 : -1));
            } else if (x == xMin || x == xMax) {
                // X face: flat 3x3 wall along Z
                for (int dz2 = -1; dz2 <= 1; dz2++) {
                    wallBlocks.add(edge.clone().add(0, dy, dz2));
                }
            } else if (z == zMin || z == zMax) {
                // Z face: flat 3x3 wall along X
                for (int dx2 = -1; dx2 <= 1; dx2++) {
                    wallBlocks.add(edge.clone().add(dx2, dy, 0));
                }
            }
        }
    }
}