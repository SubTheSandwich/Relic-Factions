package me.sub.RelicFactions.Files.Enums;

import me.sub.RelicFactions.Main.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;

public enum HCFClass {

    BARD(
            "bard",
            Material.GOLDEN_HELMET,
            Material.GOLDEN_CHESTPLATE,
            Material.GOLDEN_LEGGINGS,
            Material.GOLDEN_BOOTS
    ) {
        @Override
        public void applyPassiveEffects(Player player) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (getActiveClass(player) == null || getActiveClass(player) != BARD) {
                        cancel();
                        return;
                    }
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0, true, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 0, true, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, true, false));
                }
            }.runTaskTimer(Main.getInstance(), 0, 80);
        }
        @Override
        public PotionEffectType[] getPassiveEffectTypes() {
            return new PotionEffectType[] {
                    PotionEffectType.REGENERATION,
                    PotionEffectType.RESISTANCE,
                    PotionEffectType.SPEED
            };
        }
    },
    ARCHER(
            "archer",
            Material.LEATHER_HELMET,
            Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS,
            Material.LEATHER_BOOTS
    ) {
        @Override
        public void applyPassiveEffects(Player player) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (getActiveClass(player) == null || getActiveClass(player) != ARCHER) {
                        cancel();
                        return;
                    }
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2, true, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 0, true, false));
                }
            }.runTaskTimer(Main.getInstance(), 0, 80);
        }
        @Override
        public PotionEffectType[] getPassiveEffectTypes() {
            return new PotionEffectType[] {
                    PotionEffectType.SPEED,
                    PotionEffectType.RESISTANCE
            };
        }
    },
    ROGUE(
            "rogue",
            Material.CHAINMAIL_HELMET,
            Material.CHAINMAIL_CHESTPLATE,
            Material.CHAINMAIL_LEGGINGS,
            Material.CHAINMAIL_BOOTS
    ) {
        @Override
        public void applyPassiveEffects(Player player) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (getActiveClass(player) == null || getActiveClass(player) != ROGUE) {
                        cancel();
                        return;
                    }
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2, true, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 1, true, false));
                }
            }.runTaskTimer(Main.getInstance(), 0, 80);
        }
        @Override
        public PotionEffectType[] getPassiveEffectTypes() {
            return new PotionEffectType[] {
                    PotionEffectType.SPEED,
                    PotionEffectType.JUMP_BOOST
            };
        }
    },
    MINER(
            "miner",
            Material.IRON_HELMET,
            Material.IRON_CHESTPLATE,
            Material.IRON_LEGGINGS,
            Material.IRON_BOOTS
    ) {
        @Override
        public void applyPassiveEffects(Player player) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (getActiveClass(player) == null || getActiveClass(player) != MINER) {
                        cancel();
                        return;
                    }
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 100, 1, true, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100, 0, true, false));
                }
            }.runTaskTimer(Main.getInstance(), 0, 80);
        }
        @Override
        public PotionEffectType[] getPassiveEffectTypes() {
            return new PotionEffectType[] {
                    PotionEffectType.HASTE,
                    PotionEffectType.NIGHT_VISION
            };
        }
    };

    private final String configKey;
    public final Material helmet, chestplate, leggings, boots;

    HCFClass(String configKey, Material helmet, Material chestplate, Material leggings, Material boots) {
        this.configKey = configKey;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
    }

    public String getConfigKey() {
        return configKey;
    }

    public abstract void applyPassiveEffects(Player player);

    public abstract PotionEffectType[] getPassiveEffectTypes();

    @Nullable
    public static HCFClass getActiveClass(Player player) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (HCFClass hcfClass : HCFClass.values()) {
            if (isFullSet(armor, hcfClass)) {
                return hcfClass;
            }
        }
        return null;
    }

    private static boolean isFullSet(ItemStack[] armor, HCFClass hcfClass) {
        return armor.length == 4 &&
                armor[3] != null && armor[3].getType() == hcfClass.helmet &&
                armor[2] != null && armor[2].getType() == hcfClass.chestplate &&
                armor[1] != null && armor[1].getType() == hcfClass.leggings &&
                armor[0] != null && armor[0].getType() == hcfClass.boots;
    }
}