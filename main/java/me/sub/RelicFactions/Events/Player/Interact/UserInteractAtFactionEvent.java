package me.sub.RelicFactions.Events.Player.Interact;

import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.ModMode;
import me.sub.RelicFactions.Files.Enums.FactionType;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Files.Normal.ModModeFile;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class UserInteractAtFactionEvent implements Listener {

    private void combined(Player player, Location location, boolean isUse) {
        User user = User.get(player);
        if (rejectedModifierType(user, location) == null) return;
        if (Objects.requireNonNull(rejectedModifierType(user, location)).equalsIgnoreCase("WARZONE")) {
            if (isUse) {
                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.cannot-use")).replace("%faction%", Objects.requireNonNull(Locale.get().getString("faction.warzone")))));
            } else {
                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.cannot-build")).replace("%faction%", Objects.requireNonNull(Locale.get().getString("faction.warzone")))));
            }
        } else if (Objects.requireNonNull(rejectedModifierType(user, location)).equalsIgnoreCase("WILDERNESS")) {
            if (isUse) {
                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.cannot-use")).replace("%faction%", Objects.requireNonNull(Locale.get().getString("faction.wilderness")))));
            } else {
                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.cannot-build")).replace("%faction%", Objects.requireNonNull(Locale.get().getString("faction.wilderness")))));
            }
        } else if (Objects.requireNonNull(rejectedModifierType(user, location)).equalsIgnoreCase("FROZEN")) {
            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.freeze.cannot"))));
        } else if (Objects.requireNonNull(rejectedModifierType(user, location)).equalsIgnoreCase("FROZEN_SERVER")) {
            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.freeze.cannot"))));
        } else if (Objects.requireNonNull(rejectedModifierType(user, location)).equalsIgnoreCase("MOD-MODE")) {
            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.mod-mode.cant"))));
        } else if (Objects.requireNonNull(rejectedModifierType(user, location)).equalsIgnoreCase("NONE")) {
            return;
        } else {
            if (isUse) {
                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.cannot-use")).replace("%faction%", Objects.requireNonNull(Faction.getAt(location)).getValidName(player, false))));
            } else {
                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.cannot-build")).replace("%faction%", Objects.requireNonNull(Faction.getAt(location)).getValidName(player, false))));
            }
        }
    }

    private boolean cannotModify(User user, Location location) {
        if (user.isFactionBypass()) return false;
        if (user.isFrozen()) return true;
        if (user.getModMode() != null) {
            ModMode modMode = user.getModMode();
            if (!modMode.isInBypass()) return true;
        }
        if (Faction.getAt(location) == null) {
            switch (Objects.requireNonNull(location.getWorld()).getEnvironment()) {
                case NORMAL, CUSTOM -> {
                    return Faction.isInWilderness(location, Main.getInstance().getConfig().getInt("factions.sizes.worlds.default.warzone-build-limit"));
                }
                case NETHER -> {
                    return Faction.isInWilderness(location, Main.getInstance().getConfig().getInt("factions.sizes.worlds.nether.warzone-build-limit"));
                }
                case THE_END -> { return true; }
            }

        }
        Faction faction = Faction.getAt(location);
        if (!Objects.requireNonNull(faction).getType().equals(FactionType.PLAYER)) return true;
        if (faction.getDTR().doubleValue() <= 0) return false;
        if (user.getFaction() == null) return true;
        return !user.getFaction().equals(faction.getUUID());
    }
    private String rejectedModifierType(User user, Location location) {
        if (user.getModMode() != null) {
            if (!user.getModMode().isInBypass()) return "MOD-MODE";
        }
        if (Main.getInstance().isServerFrozen()) return "FROZEN_SERVER";
        if (user.isFrozen() || user.isPanic()) return "FROZEN";
        if (Faction.getAt(location) == null) {
            switch (Objects.requireNonNull(location.getWorld()).getEnvironment()) {
                case NORMAL, CUSTOM -> {
                    if (Faction.isInWilderness(location, Main.getInstance().getConfig().getInt("factions.sizes.worlds.default.warzone-build-limit"))) {
                        return "WARZONE";
                    }
                }
                case NETHER -> {
                    if (Faction.isInWilderness(location, Main.getInstance().getConfig().getInt("factions.sizes.worlds.nether.warzone-build-limit"))) {
                        return "WARZONE";
                    }
                }
                case THE_END -> {
                    return "WILDERNESS";
                }
            }
            return null;
        }
        return "FACTION";
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        Location location = e.getBlock().getLocation();
        if (cannotModify(user, location)) {
            if (Objects.requireNonNull(rejectedModifierType(user, location)).equalsIgnoreCase("FROZEN_SERVER")) {
                if (!p.hasPermission("relic.bypass.freeze")) {
                    e.setCancelled(true);
                    combined(p, location, false);
                    return;
                }
            }
            e.setCancelled(true);
            combined(p, location, false);
            return;
        }
        Block block = e.getBlock();
        if (isPlayerPlaced(block)) {
            block.removeMetadata("playerPlaced", Main.getInstance());
            return;
        }

        if (isOre(block.getType())) {
            switch (block.getType()) {
                case COAL_ORE:
                case DEEPSLATE_COAL_ORE:
                    user.setCoalMined(user.getCoalMined() + 1);
                    break;
                case IRON_ORE:
                case DEEPSLATE_IRON_ORE:
                    user.setIronMined(user.getIronMined() + 1);
                    break;
                case COPPER_ORE:
                case DEEPSLATE_COPPER_ORE:
                    user.setCopperMined(user.getCopperMined() + 1);
                    break;
                case DIAMOND_ORE:
                case DEEPSLATE_DIAMOND_ORE:
                    user.setDiamondMined(user.getDiamondMined() + 1);
                    break;
                case GOLD_ORE:
                case DEEPSLATE_GOLD_ORE:
                case NETHER_GOLD_ORE:
                    user.setGoldMined(user.getGoldMined() + 1);
                    break;
                case REDSTONE_ORE:
                case DEEPSLATE_REDSTONE_ORE:
                    user.setRedstoneMined(user.getRedstoneMined() + 1);
                    break;
                case LAPIS_ORE:
                case DEEPSLATE_LAPIS_ORE:
                    user.setLapisMined(user.getLapisMined() + 1);
                    break;
                case EMERALD_ORE:
                case DEEPSLATE_EMERALD_ORE:
                    user.setEmeraldMined(user.getEmeraldMined() + 1);
                    break;
                case NETHER_QUARTZ_ORE:
                    user.setQuartzMined(user.getQuartzMined() + 1);
                    break;
                case ANCIENT_DEBRIS:
                    user.setDebrisMined(user.getDebrisMined() + 1);
                    break;
                default:
                    break;
            }
        }

        if (isVeinFound(block)) return;

        if (isDiamondOre(block)) {
            Set<Block> vein = new HashSet<>();
            findVein(block, vein);

            for (Block b : vein) {
                b.setMetadata("veinFound", new FixedMetadataValue(Main.getInstance(), true));
            }

            if (!vein.isEmpty()) {
                String message = Locale.get().getString("events.found-diamonds");
                message = Objects.requireNonNull(message).replace("%player%", p.getName());
                message = message.replace("%amount%", vein.size() + "");
                Main.getInstance().getServer().broadcastMessage(C.chat(message));
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        Location location = e.getBlock().getLocation();
        if (cannotModify(user, location)) {
            if (Objects.requireNonNull(rejectedModifierType(user, location)).equalsIgnoreCase("FROZEN_SERVER")) {
                if (!p.hasPermission("relic.bypass.freeze")) {
                    e.setCancelled(true);
                    combined(p, location, false);
                    return;
                }
            }
            e.setCancelled(true);
            combined(p, location, false);
            return;
        }
        Block block = e.getBlockPlaced();
        if (isOre(block.getType())) {
            block.setMetadata("playerPlaced", new FixedMetadataValue(Main.getInstance(), true));
        }
    }

    @EventHandler
    public void onManipulate(PlayerArmorStandManipulateEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        Location location = e.getRightClicked().getLocation();
        if (cannotModify(user, location)) {
            if (Objects.requireNonNull(rejectedModifierType(user, location)).equalsIgnoreCase("FROZEN_SERVER")) {
                if (!p.hasPermission("relic.bypass.freeze")) {
                    e.setCancelled(true);
                    combined(p, location, true);
                    return;
                }
            }
            e.setCancelled(true);
            combined(p, location, true);
        }
    }

    @EventHandler
    public void onBed(PlayerBedEnterEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        Location location = e.getBed().getLocation();
        if (cannotModify(user, location)) {
            if (Objects.requireNonNull(rejectedModifierType(user, location)).equalsIgnoreCase("FROZEN_SERVER")) {
                if (!p.hasPermission("relic.bypass.freeze")) {
                    e.setCancelled(true);
                    combined(p, location, true);
                    return;
                }
            }
            e.setCancelled(true);
            combined(p, location, true);
        }
    }

    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        Location location = e.getBlock().getLocation();
        if (cannotModify(user, location)) {
            if (Objects.requireNonNull(rejectedModifierType(user, location)).equalsIgnoreCase("FROZEN_SERVER")) {
                if (!p.hasPermission("relic.bypass.freeze")) {
                    e.setCancelled(true);
                    combined(p, location, true);
                    return;
                }
            }
            e.setCancelled(true);
            combined(p, location, true);
        }
    }

    @EventHandler
    public void onBucket(PlayerBucketEntityEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        Location location = e.getEntity().getLocation();
        if (cannotModify(user, location)) {
            if (Objects.requireNonNull(rejectedModifierType(user, location)).equalsIgnoreCase("FROZEN_SERVER")) {
                if (!p.hasPermission("relic.bypass.freeze")) {
                    e.setCancelled(true);
                    combined(p, location, true);
                    return;
                }
            }
            e.setCancelled(true);
            combined(p, location, true);
        }
    }

    @EventHandler
    public void onHarvest(PlayerHarvestBlockEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        Location location = e.getHarvestedBlock().getLocation();
        if (cannotModify(user, location)) {
            if (Objects.requireNonNull(rejectedModifierType(user, location)).equalsIgnoreCase("FROZEN_SERVER")) {
                if (!p.hasPermission("relic.bypass.freeze")) {
                    e.setCancelled(true);
                    combined(p, location, false);
                    return;
                }
            }
            e.setCancelled(true);
            combined(p, location, false);
        }
    }

    @EventHandler
    public void onShear(PlayerShearEntityEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        Location location = e.getEntity().getLocation();
        if (cannotModify(user, location)) {
            if (Objects.requireNonNull(rejectedModifierType(user, location)).equalsIgnoreCase("FROZEN_SERVER")) {
                if (!p.hasPermission("relic.bypass.freeze")) {
                    e.setCancelled(true);
                    combined(p, location, true);
                    return;
                }
            }
            e.setCancelled(true);
            combined(p, location, true);
        }
    }

    @EventHandler
    public void onSign(PlayerSignOpenEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        Location location = e.getSign().getLocation();
        if (cannotModify(user, location)) {
            if (Objects.requireNonNull(rejectedModifierType(user, location)).equalsIgnoreCase("FROZEN_SERVER")) {
                if (!p.hasPermission("relic.bypass.freeze")) {
                    e.setCancelled(true);
                    combined(p, location, true);
                    return;
                }
            }
            e.setCancelled(true);
            combined(p, location, true);
        }
    }

    @EventHandler
    public void onTake(PlayerTakeLecternBookEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        Location location = e.getLectern().getLocation();
        if (cannotModify(user, location)) {
            if (Objects.requireNonNull(rejectedModifierType(user, location)).equalsIgnoreCase("FROZEN_SERVER")) {
                if (!p.hasPermission("relic.bypass.freeze")) {
                    e.setCancelled(true);
                    combined(p, location, false);
                    return;
                }
            }
            e.setCancelled(true);
            combined(p, location, false);
        }
    }

    @EventHandler
    public void onUnleash(PlayerUnleashEntityEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        Location location = e.getEntity().getLocation();
        if (cannotModify(user, location)) {
            if (Objects.requireNonNull(rejectedModifierType(user, location)).equalsIgnoreCase("FROZEN_SERVER")) {
                if (!p.hasPermission("relic.bypass.freeze")) {
                    e.setCancelled(true);
                    combined(p, location, true);
                    return;
                }
            }
            e.setCancelled(true);
            combined(p, location, true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        Action a = e.getAction();
        if (e.getItem() != null) {
            if (user.getModMode() == null && ModModeFile.getItem(e.getItem()) != null) {
                p.getInventory().remove(e.getItem());
            }
        }
        if (a.equals(Action.RIGHT_CLICK_BLOCK)) {
            Block block = e.getClickedBlock();
            Material type = Objects.requireNonNull(block).getType();
            if (Tag.TRAPDOORS.isTagged(type) || Tag.WOODEN_DOORS.isTagged(type) || Tag.BUTTONS.isTagged(type) || Tag.ANVIL.isTagged(type)
                    || type.equals(Material.BARREL) || type.equals(Material.BEACON) || type.equals(Material.BLAST_FURNACE) || type.equals(Material.CARTOGRAPHY_TABLE)
                    || type.equals(Material.CHEST) || type.equals(Material.CRAFTER) || type.equals(Material.CRAFTING_TABLE) || type.equals(Material.DISPENSER)
                    || type.equals(Material.DROPPER) || type.equals(Material.TRAPPED_CHEST) || type.equals(Material.FURNACE) || type.equals(Material.SMOKER)
                    || type.equals(Material.HOPPER) || type.equals(Material.LOOM) || type.equals(Material.SHULKER_BOX) || type.equals(Material.SMITHING_TABLE)
                    || type.equals(Material.STONECUTTER) || Tag.FENCE_GATES.isTagged(type) || type.equals(Material.GRINDSTONE) || type.equals(Material.TNT)
                    || type.equals(Material.TNT_MINECART) || type.equals(Material.BREWING_STAND) || type.equals(Material.FLOWER_POT) || type.equals(Material.DECORATED_POT)) {
                if (cannotModify(user, block.getLocation())) {
                    if (Objects.requireNonNull(rejectedModifierType(user, block.getLocation())).equalsIgnoreCase("FROZEN_SERVER")) {
                        if (!p.hasPermission("relic.bypass.freeze")) {
                            e.setCancelled(true);
                            combined(p, block.getLocation(), true);
                            return;
                        }
                    }
                    e.setCancelled(true);
                    combined(p, block.getLocation(), true);
                    return;
                }
            }
            return;
        }
        if (a.equals(Action.PHYSICAL)) {
            Block block = e.getClickedBlock();
            if (cannotModify(user, Objects.requireNonNull(block).getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onExplode(BlockExplodeEvent e) {
        e.blockList().clear();
    }
    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        e.blockList().clear();
    }

    @EventHandler
    public void onEntity(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        if (!(e.getRightClicked() instanceof GlowItemFrame) && !(e.getRightClicked() instanceof ItemFrame)) return;
        Entity block = e.getRightClicked();
        if (cannotModify(user, block.getLocation())) {
            if (Objects.requireNonNull(rejectedModifierType(user, e.getRightClicked().getLocation())).equalsIgnoreCase("FROZEN_SERVER")) {
                if (!p.hasPermission("relic.bypass.freeze")) {
                    e.setCancelled(true);
                    combined(p, e.getRightClicked().getLocation(), true);
                    return;
                }
            }
            e.setCancelled(true);
            combined(p, block.getLocation(), true);
        }
    }

    @EventHandler
    public void onEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) return;
        if (!(e.getEntity() instanceof GlowItemFrame) && !(e.getEntity() instanceof ItemFrame) && !(e.getEntity() instanceof ArmorStand))
            return;
        Entity block = e.getEntity();
        User user = User.get(p);
        if (cannotModify(user, block.getLocation())) {
            if (Objects.requireNonNull(rejectedModifierType(user, e.getEntity().getLocation())).equalsIgnoreCase("FROZEN_SERVER")) {
                if (!p.hasPermission("relic.bypass.freeze")) {
                    e.setCancelled(true);
                    combined(p, e.getEntity().getLocation(), false);
                    return;
                }
            }
            e.setCancelled(true);
            combined(p, block.getLocation(), true);
        }
    }

    private boolean isDiamondOre(Block block) {
        Material type = block.getType();
        return type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE;
    }

    private boolean isPlayerPlaced(Block block) {
        if (!block.hasMetadata("playerPlaced")) return false;
        for (MetadataValue value : block.getMetadata("playerPlaced")) {
            if (value.getOwningPlugin() == Main.getInstance() && value.asBoolean()) {
                return true;
            }
        }
        return false;
    }

    private void findVein(Block block, Set<Block> vein) {
        if (!isDiamondOre(block) || vein.contains(block) || isPlayerPlaced(block)) return;
        vein.add(block);

        // Check all 6 adjacent blocks (no diagonals)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1) continue;
                    Block neighbor = block.getRelative(dx, dy, dz);
                    findVein(neighbor, vein);
                }
            }
        }
    }

    private boolean isVeinFound(Block block) {
        if (!block.hasMetadata("veinFound")) return false;
        for (MetadataValue value : block.getMetadata("veinFound")) {
            if (value.getOwningPlugin() == Main.getInstance() && value.asBoolean()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOre(Material material) {
        return switch (material) {
            case COAL_ORE, DEEPSLATE_COAL_ORE, IRON_ORE, DEEPSLATE_IRON_ORE, COPPER_ORE, DEEPSLATE_COPPER_ORE, GOLD_ORE,
                 DEEPSLATE_GOLD_ORE, REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE, LAPIS_ORE, DEEPSLATE_LAPIS_ORE, DIAMOND_ORE,
                 DEEPSLATE_DIAMOND_ORE, EMERALD_ORE, DEEPSLATE_EMERALD_ORE, NETHER_GOLD_ORE, NETHER_QUARTZ_ORE,
                 ANCIENT_DEBRIS -> true;
            default -> false;
        };
    }
}
