package me.sub.RelicFactions.Files.Data;

import me.sub.RelicFactions.Utils.C;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class Claim {

    private final UUID uuid;
    private Location cornerOne;
    private Location cornerTwo;
    private final Material pillar;
    private boolean attempting;

    public Claim(UUID uuid) {
        this.uuid = uuid;
        cornerOne = null;
        cornerTwo = null;
        pillar = getPillarMaterial();
        attempting = false;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Location getCornerOne() {
        return cornerOne;
    }

    public void setCornerOne(Location cornerOne) {
        this.cornerOne = cornerOne;
    }

    public Location getCornerTwo() {
        return cornerTwo;
    }

    public void setCornerTwo(Location cornerTwo) {
        this.cornerTwo = cornerTwo;
    }

    public Material getPillarType() {
        return pillar;
    }

    public static Material getPillarMaterial() {

        ArrayList<Material> materials = new ArrayList<>();
        for (Material material : Material.values()) {

            if (material.isAir()) continue;
            if (!material.isSolid()) continue;
            if (material.name().toUpperCase().contains("GLASS")) continue;
            if (material.isRecord()) continue;
            if (material.isEdible()) continue;
            if (Tag.TRAPDOORS.isTagged(material)) continue;
            if (Tag.DOORS.isTagged(material)) continue;
            if (Tag.STAIRS.isTagged(material)) continue;
            if (Tag.RAILS.isTagged(material)) continue;
            if (Tag.SLABS.isTagged(material)) continue;
            if (Tag.PRESSURE_PLATES.isTagged(material)) continue;
            if (Tag.BUTTONS.isTagged(material)) continue;
            if (Tag.SLABS.isTagged(material)) continue;
            if (Tag.STAIRS.isTagged(material)) continue;
            if (Tag.ALL_SIGNS.isTagged(material)) continue;
            if (Tag.CAMPFIRES.isTagged(material)) continue;
            if (Tag.CROPS.isTagged(material)) continue;
            if (Tag.FENCE_GATES.isTagged(material)) continue;
            if (Tag.FENCES.isTagged(material)) continue;
            if (Tag.FLOWERS.isTagged(material)) continue;
            if (Tag.WOOL_CARPETS.isTagged(material)) continue;
            if (Tag.BEDS.isTagged(material)) continue;
            if (Tag.CANDLE_CAKES.isTagged(material)) continue;
            if (Tag.CORAL_PLANTS.isTagged(material)) continue;
            if (Tag.WALL_CORALS.isTagged(material)) continue;
            if (Tag.BANNERS.isTagged(material)) continue;
            if (Tag.WALLS.isTagged(material)) continue;
            if (material.equals(Material.FARMLAND)) continue;
            if (material.equals(Material.CAKE)) continue;
            if (material.equals(Material.DAYLIGHT_DETECTOR)) continue;
            if (material.equals(Material.POINTED_DRIPSTONE)) continue;
            if (material.equals(Material.SCULK_VEIN)) continue;
            if (material.equals(Material.SCULK_CATALYST)) continue;
            if (material.equals(Material.SCULK_SHRIEKER)) continue;
            if (material.equals(Material.SCULK_SENSOR)) continue;
            if (material.equals(Material.CALIBRATED_SCULK_SENSOR)) continue;
            if (material.equals(Material.BREWING_STAND)) continue;
            if (material.equals(Material.DECORATED_POT)) continue;
            if (Tag.CAULDRONS.isTagged(material)) continue;
            if (material.equals(Material.LANTERN)) continue;
            if (material.equals(Material.BELL)) continue;
            if (material.equals(Material.LECTERN)) continue;
            if (material.name().toUpperCase().contains("PISTON")) continue;
            if (material.name().toUpperCase().contains("CHEST")) continue;
            if (material.equals(Material.ENCHANTING_TABLE)) continue;
            if (Tag.CORAL_PLANTS.isTagged(material)) continue;
            if (Tag.ANVIL.isTagged(material)) continue;
            if (Tag.LEAVES.isTagged(material)) continue;
            if (material.equals(Material.BARRIER)) continue;
            if (material.equals(Material.HOPPER)) continue;
            if (material.name().toUpperCase().contains("AMETHYST_BUD") || material.name().toUpperCase().contains("AMETHYST_CLUSTER")) continue;
            if (material.equals(Material.DIRT_PATH)) continue;
            if (material.name().toUpperCase().contains("INFESTED")) continue;
            if (material.equals(Material.SNIFFER_EGG)) continue;
            if (Tag.SHULKER_BOXES.isTagged(material)) continue;
            if (material.equals(Material.BAMBOO)) continue;
            if (material.name().contains("CORAL") && !material.name().endsWith("_CORAL_BLOCK")) continue;
            materials.add(material);
        }
        return materials.get(new Random().nextInt(materials.size()));
    }

    public boolean isAttempting() {
        return attempting;
    }

    public void setAttempting(boolean attempting) {
        this.attempting = attempting;
    }

    public static ItemStack getWand() {
        ItemStack wand = new ItemStack(Material.DIAMOND_HOE);
        ItemMeta wandMeta = wand.getItemMeta();
        Objects.requireNonNull(wandMeta).displayName(Component.text(C.chat("&bClaiming Wand")));
        wand.setItemMeta(wandMeta);
        return wand;
    }
}
