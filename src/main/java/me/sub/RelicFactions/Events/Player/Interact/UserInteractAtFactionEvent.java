package me.sub.RelicFactions.Events.Player.Interact;

import me.sub.RelicFactions.Commands.Admin.CrowbarCommand;
import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.Mountain;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.Cuboid;
import me.sub.RelicFactions.Files.Enums.HCFClass;
import me.sub.RelicFactions.Files.Data.ModMode;
import me.sub.RelicFactions.Files.Data.PlayerTimer;
import me.sub.RelicFactions.Files.Enums.FactionType;
import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Files.Normal.ModModeFile;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.sign.Side;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.*;

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
        } else if (Objects.requireNonNull(rejectedModifierType(user, location)).equalsIgnoreCase("BORDER")) {
            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.border.build"))));
        } else {
            if (Objects.requireNonNull(rejectedModifierType(user, location)).equalsIgnoreCase("NONE")) return;
            if (isUse) {
                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.cannot-use")).replace("%faction%", Objects.requireNonNull(Faction.getAt(location)).getValidName(player, false))));
            } else {
                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.cannot-build")).replace("%faction%", Objects.requireNonNull(Faction.getAt(location)).getValidName(player, false))));
            }
        }
    }

    private boolean cannotModify(User user, Location location) {
        if (UserClaimEvents.isPastBorder(location)) return true;
        if (user.isFactionBypass()) return false;
        if (user.isFrozen()) return true;
        if (user.getModMode() != null) {
            ModMode modMode = user.getModMode();
            if (!modMode.isInBypass()) return true;
        }
        if (Faction.getAt(location) == null) {
            if (Main.getInstance().isEOTW()) return false;
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
        if (Main.getInstance().isEOTW()) return false;
        if (faction.getDTR().doubleValue() <= 0) return false;
        if (user.getFaction() == null) return true;
        return !user.getFaction().equals(faction.getUUID());
    }
    private String rejectedModifierType(User user, Location location) {
        if (UserClaimEvents.isPastBorder(location)) return "BORDER";
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
        Block block = e.getBlock();

        for (Mountain mountain : Main.getInstance().mountains.values()) {
            if (!mountain.isSetup()) continue;
            Cuboid cuboid = mountain.getCuboid();
            if (cuboid.isIn(location)) {
                if (isCrowbar(p.getInventory().getItemInMainHand())) {
                    e.setCancelled(true);
                    return;
                }
                if (!e.getBlock().getType().equals(mountain.getType())) {
                    e.setCancelled(true);
                    return;
                }
                return;
            }
        }
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

        // --- SUBCLAIM PROTECTION START ---
        // 1. Prevent breaking a [Subclaim] sign that is not theirs
        if (block.getState() instanceof Sign sign) {
            String firstLine = C.strip(C.serialize(sign.line(0)));
            if (firstLine.equalsIgnoreCase("[Subclaim]")) {
                String owner = C.strip(C.serialize(sign.line(1)));
                if (!p.getName().equalsIgnoreCase(owner) && !user.isFactionBypass()) {
                    e.setCancelled(true);
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.subclaim.cannot-open"))
                            .replace("%name%", owner)));
                    return;
                }
            }
        }

        // 2. Prevent breaking a container with a [Subclaim] sign not theirs (including double chests)
        if (block.getState() instanceof Container) {
            // Support for double chests
            List<Block> chestBlocks = new ArrayList<>();
            BlockState state = block.getState();
            if (state instanceof Chest chest) {
                Inventory inv = chest.getInventory();
                if (inv instanceof DoubleChestInventory doubleInv) {
                    DoubleChest doubleChest = doubleInv.getHolder();
                    Chest left = (Chest) Objects.requireNonNull(doubleChest).getLeftSide();
                    Chest right = (Chest) doubleChest.getRightSide();
                    chestBlocks.add(Objects.requireNonNull(left).getBlock());
                    chestBlocks.add(Objects.requireNonNull(right).getBlock());
                    chestBlocks.add(Objects.requireNonNull(right).getBlock());
                } else {
                    chestBlocks.add(block);
                }
            } else {
                chestBlocks.add(block);
            }

            boolean subclaimFound = false;
            String subclaimOwner = null;

            for (Block chestBlock : chestBlocks) {
                for (BlockFace face : BlockFace.values()) {
                    if (face == BlockFace.SELF) continue;
                    Block relative = chestBlock.getRelative(face);
                    BlockState relState = relative.getState();
                    if (relState instanceof Sign foundSign) {
                        boolean attached = false;
                        if (relative.getBlockData() instanceof org.bukkit.block.data.Directional directional) {
                            if (relative.getRelative(directional.getFacing().getOppositeFace()).equals(chestBlock)) {
                                attached = true;
                            }
                        }
                        String firstLine = C.strip(C.serialize(foundSign.line(0)));
                        if (attached && firstLine.equalsIgnoreCase("[Subclaim]")) {
                            subclaimFound = true;
                            subclaimOwner = C.strip(C.serialize(foundSign.line(1)));
                            break;
                        }
                    }
                }
                if (subclaimFound) break;
            }

            if (subclaimFound && !p.getName().equalsIgnoreCase(subclaimOwner) && !user.isFactionBypass()) {
                e.setCancelled(true);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.subclaim.cannot-open"))
                        .replace("%name%", subclaimOwner)));
                return;

            }
        }
        // --- SUBCLAIM PROTECTION END ---

        if (isCrowbar(p.getInventory().getItemInMainHand())) {
            e.setCancelled(true);
            return;
        }

        if (isOre(block.getType()) && Main.getInstance().getConfig().getBoolean("features.autosmelt")) {
            Material smelted = getSmeltedResult(block.getType());
            if (smelted != null) {
                e.setDropItems(false);
                block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(smelted));
                block.getWorld().spawn(block.getLocation().add(0.5, 0.5, 0.5), org.bukkit.entity.ExperienceOrb.class)
                        .setExperience(getSmeltXP(block.getType()));
                return;
            }
        }

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
                for (Player player : Bukkit.getOnlinePlayers()) {
                    User use = User.get(player);
                    if (!use.isFoundDiamonds()) continue;
                    player.sendMessage(C.chat(message));
                }
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
        if (user.hasTimer("combat") && !Main.getInstance().getConfig().getBoolean("listeners.place-in-combat")) {
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.timer.player.cannot-place"))));
            e.setCancelled(true);
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
    public void onChange(SignChangeEvent e) {
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
            return;
        }

        if (!Main.getInstance().getConfig().getBoolean("elevators.sign")) return;

        Block block = e.getBlock();
        List<Component> lines = e.lines();
        if (block.getState() instanceof Sign sign) {
            List<Component> oldLines = sign.getSide(e.getSide()).lines();
            if (C.serialize(oldLines.getFirst()).equalsIgnoreCase(C.chat("&9[Elevator]")) && !C.serialize(lines.getFirst()).equals("[Elevator]")) {
                e.line(0, Component.text(C.strip(C.serialize(e.line(0)))));
                return;
            }
        }

        String line0 = C.serialize(e.line(0));
        String line1 = C.serialize(e.line(1));

        if (line0.equalsIgnoreCase("[Subclaim]")) {
            Block signBlock = e.getBlock();
            Block attachedBlock = null;
            if (!user.hasFaction()) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.subclaim.need-faction"))));
                e.setCancelled(true);
                return;
            }
            Faction faction = Faction.get(user.getFaction());
            if (faction.getCuboidAtLocation(e.getBlock().getLocation()) == null) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.subclaim.invalid-territory"))));
                e.setCancelled(true);
                return;
            }

            if (faction.getRoleID(p.getUniqueId()) < 1) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.subclaim.invalid-rank"))));
                e.setCancelled(true);
                return;
            }

            BlockState state = signBlock.getState();
            if (state instanceof Sign) {
                if (signBlock.getBlockData() instanceof org.bukkit.block.data.type.WallSign wallSign) {
                    attachedBlock = signBlock.getRelative(wallSign.getFacing().getOppositeFace());
                } else if (signBlock.getBlockData() instanceof org.bukkit.block.data.type.Sign) {
                    attachedBlock = signBlock.getRelative(BlockFace.DOWN);
                }
            }

            if (attachedBlock == null || !(attachedBlock.getState() instanceof Container)) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.subclaim.not-container"))));
                e.setCancelled(true);
                return;
            }

            // --- DOUBLE CHEST SUPPORT ---
            BlockState attachedState = attachedBlock.getState();
            List<Block> chestBlocks = new ArrayList<>();
            if (attachedState instanceof Chest chest) {
                Inventory inv = chest.getInventory();
                if (inv instanceof DoubleChestInventory doubleInv) {
                    DoubleChest doubleChest = doubleInv.getHolder();
                    Chest left = (Chest) Objects.requireNonNull(doubleChest).getLeftSide();
                    Chest right = (Chest) doubleChest.getRightSide();
                    chestBlocks.add(Objects.requireNonNull(left).getBlock());
                    chestBlocks.add(Objects.requireNonNull(right).getBlock());
                } else {
                    chestBlocks.add(attachedBlock);
                }
            } else {
                chestBlocks.add(attachedBlock);
            }

            boolean signFound = false;
            for (Block chestBlock : chestBlocks) {
                for (BlockFace face : BlockFace.values()) {
                    if (face == BlockFace.SELF) continue;
                    Block relative = chestBlock.getRelative(face);
                    BlockState relState = relative.getState();
                    if (relState instanceof Sign foundSign) {
                        boolean attached = false;
                        if (relative.getBlockData() instanceof org.bukkit.block.data.Directional directional) {
                            if (relative.getRelative(directional.getFacing().getOppositeFace()).equals(chestBlock)) {
                                attached = true;
                            }
                        }
                        String firstLine = C.strip(C.serialize(foundSign.line(0)));
                        if (attached && firstLine.equalsIgnoreCase("[Subclaim]")) {
                            signFound = true;
                            break;
                        }
                    }
                }
                if (signFound) break;
            }
            if (signFound) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.subclaim.already-exists"))));
                e.setCancelled(true);
                return;
            }

            User in = User.get(line1);
            if (in == null || !in.hasFaction() || !in.getFaction().equals(user.getFaction())) {
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.subclaim.invalid-faction-member")).replace("%name%", line1)));
                e.setCancelled(true);
                return;
            }
            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.subclaim.created")).replace("%player%", in.getName())));
            return;
        }

        if (!line0.equalsIgnoreCase("[Elevator]")) return;
        if (!line1.equalsIgnoreCase("Up") && !line1.equalsIgnoreCase("Down")) return;

        e.line(0, Component.text(C.chat("&9[Elevator]")));
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
        e.setCancelled(isCrowbar(p.getInventory().getItemInMainHand()));
        if (a.equals(Action.RIGHT_CLICK_BLOCK) || a.equals(Action.RIGHT_CLICK_AIR)) {
            if (user.getUserClass() != null && user.getUserClass().equals(HCFClass.ARCHER)) {
                if (e.getItem() != null && !e.getItem().getType().equals(Material.AIR)) {
                    ItemStack item = e.getItem();
                    boolean mainHand = e.getHand() == EquipmentSlot.HAND;

                    // Archer Speed Consumable
                    if (item.getType().equals(Material.matchMaterial(
                            Objects.requireNonNull(Main.getInstance().getConfig().getString("kits.archer.speed.item"))))) {
                        e.setCancelled(true);
                        if (user.hasTimer("speedeffect")) {
                            String message = Locale.get().getString("events.timer.player.cooldown.speedeffect") == null
                                    ? Locale.get().getString("events.timer.player.cooldown.default")
                                    : Locale.get().getString("events.timer.cooldown.speedeffect");
                            message = Objects.requireNonNull(message)
                                    .replace("%time%", Timer.format(user.getTimer("speedeffect").getDuration()));
                            p.sendMessage(C.chat(message));
                            return;
                        }
                        PotionEffectType effectType = Registry.POTION_EFFECT_TYPE.get(
                                NamespacedKey.minecraft(Objects.requireNonNull(
                                        Main.getInstance().getConfig().getString("kits.archer.speed.effect")).toLowerCase()));

                        int duration = Main.getInstance().getConfig().getInt("kits.archer.speed.duration");
                        int amplifier = Main.getInstance().getConfig().getInt("kits.archer.speed.amplifier");

                        PotionEffect potionEffect = new PotionEffect(Objects.requireNonNull(effectType), duration, amplifier, true, false);
                        if (mainHand) {
                            if (p.getInventory().getItemInMainHand().getAmount() == 1) {
                                p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                            } else {
                                p.getInventory().getItemInMainHand().setAmount(
                                        p.getInventory().getItemInMainHand().getAmount() - 1);
                            }
                        } else {
                            if (p.getInventory().getItemInOffHand().getAmount() == 1) {
                                p.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                            } else {
                                p.getInventory().getItemInOffHand().setAmount(
                                        p.getInventory().getItemInOffHand().getAmount() - 1);
                            }
                        }
                        p.addPotionEffect(potionEffect);
                        PlayerTimer timer = new PlayerTimer(p.getUniqueId(), Timer.SPEEDEFFECT);
                        user.addTimer(timer);
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.kit.speed-active"))));

                        // Restore Archer passive effects after duration
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (user.getUserClass() == HCFClass.ARCHER &&
                                        HCFClass.getActiveClass(p) == HCFClass.ARCHER) {
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2, true, false));
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 0, true, false));
                                }
                            }
                        }.runTaskLater(Main.getInstance(), duration + 5);

                        return;
                    }

                    // Archer Jump Consumable
                    if (item.getType().equals(Material.matchMaterial(
                            Objects.requireNonNull(Main.getInstance().getConfig().getString("kits.archer.jump.item"))))) {
                        e.setCancelled(true);
                        if (user.hasTimer("jumpeffect")) {
                            String message = Locale.get().getString("events.timer.player.cooldown.jumpeffect") == null
                                    ? Locale.get().getString("events.timer.player.cooldown.default")
                                    : Locale.get().getString("events.timer.cooldown.jumpeffect");
                            message = Objects.requireNonNull(message)
                                    .replace("%time%", Timer.format(user.getTimer("jumpeffect").getDuration()));
                            p.sendMessage(C.chat(message));
                            return;
                        }
                        PotionEffectType effectType = Registry.POTION_EFFECT_TYPE.get(
                                NamespacedKey.minecraft(Objects.requireNonNull(
                                        Main.getInstance().getConfig().getString("kits.archer.jump.effect")).toLowerCase()));

                        int duration = Main.getInstance().getConfig().getInt("kits.archer.jump.duration");
                        int amplifier = Main.getInstance().getConfig().getInt("kits.archer.jump.amplifier");

                        PotionEffect potionEffect = new PotionEffect(Objects.requireNonNull(effectType), duration, amplifier, true, false);
                        if (mainHand) {
                            if (p.getInventory().getItemInMainHand().getAmount() == 1) {
                                p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                            } else {
                                p.getInventory().getItemInMainHand().setAmount(
                                        p.getInventory().getItemInMainHand().getAmount() - 1);
                            }
                        } else {
                            if (p.getInventory().getItemInOffHand().getAmount() == 1) {
                                p.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                            } else {
                                p.getInventory().getItemInOffHand().setAmount(
                                        p.getInventory().getItemInOffHand().getAmount() - 1);
                            }
                        }
                        p.addPotionEffect(potionEffect);
                        PlayerTimer timer = new PlayerTimer(p.getUniqueId(), Timer.JUMPEFFECT);
                        user.addTimer(timer);
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.kit.jump-active"))));

                        // Restore Archer passive effects after duration
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (user.getUserClass() == HCFClass.ARCHER &&
                                        HCFClass.getActiveClass(p) == HCFClass.ARCHER) {
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2, true, false));
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 0, true, false));
                                }
                            }
                        }.runTaskLater(Main.getInstance(), duration + 5);

                        return;
                    }
                }
            }

            if (user.getUserClass() != null && user.getUserClass().equals(HCFClass.ROGUE)) {
                if (e.getItem() != null && !e.getItem().getType().equals(Material.AIR)) {
                    ItemStack item = e.getItem();
                    boolean mainHand = e.getHand() == EquipmentSlot.HAND;

                    // Rogue Speed Consumable
                    if (item.getType().equals(Material.matchMaterial(
                            Objects.requireNonNull(Main.getInstance().getConfig().getString("kits.rogue.speed.item"))))) {
                        e.setCancelled(true);
                        if (user.hasTimer("speedeffect")) {
                            String message = Locale.get().getString("events.timer.player.cooldown.speedeffect") == null
                                    ? Locale.get().getString("events.timer.player.cooldown.default")
                                    : Locale.get().getString("events.timer.cooldown.speedeffect");
                            message = Objects.requireNonNull(message)
                                    .replace("%time%", Timer.format(user.getTimer("speedeffect").getDuration()));
                            p.sendMessage(C.chat(message));
                            return;
                        }
                        PotionEffectType effectType = Registry.POTION_EFFECT_TYPE.get(
                                NamespacedKey.minecraft(Objects.requireNonNull(
                                        Main.getInstance().getConfig().getString("kits.rogue.speed.effect")).toLowerCase()));

                        int duration = Main.getInstance().getConfig().getInt("kits.rogue.speed.duration");
                        int amplifier = Main.getInstance().getConfig().getInt("kits.rogue.speed.amplifier");

                        PotionEffect potionEffect = new PotionEffect(Objects.requireNonNull(effectType), duration, amplifier, true, false);
                        if (mainHand) {
                            if (p.getInventory().getItemInMainHand().getAmount() == 1) {
                                p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                            } else {
                                p.getInventory().getItemInMainHand().setAmount(
                                        p.getInventory().getItemInMainHand().getAmount() - 1);
                            }
                        } else {
                            if (p.getInventory().getItemInOffHand().getAmount() == 1) {
                                p.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                            } else {
                                p.getInventory().getItemInOffHand().setAmount(
                                        p.getInventory().getItemInOffHand().getAmount() - 1);
                            }
                        }
                        p.addPotionEffect(potionEffect);
                        PlayerTimer timer = new PlayerTimer(p.getUniqueId(), Timer.SPEEDEFFECT);
                        user.addTimer(timer);
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.kit.speed-active"))));

                        // Restore Rogue passive effects after duration
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (user.getUserClass() == HCFClass.ROGUE &&
                                        HCFClass.getActiveClass(p) == HCFClass.ROGUE) {
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2, true, false));
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 1, true, false));
                                }
                            }
                        }.runTaskLater(Main.getInstance(), duration + 5);

                        return;
                    }

                    // Rogue Jump Consumable
                    if (item.getType().equals(Material.matchMaterial(
                            Objects.requireNonNull(Main.getInstance().getConfig().getString("kits.rogue.jump.item"))))) {
                        e.setCancelled(true);
                        if (user.hasTimer("jumpeffect")) {
                            String message = Locale.get().getString("events.timer.player.cooldown.jumpeffect") == null
                                    ? Locale.get().getString("events.timer.player.cooldown.default")
                                    : Locale.get().getString("events.timer.cooldown.jumpeffect");
                            message = Objects.requireNonNull(message)
                                    .replace("%time%", Timer.format(user.getTimer("jumpeffect").getDuration()));
                            p.sendMessage(C.chat(message));
                            return;
                        }
                        PotionEffectType effectType = Registry.POTION_EFFECT_TYPE.get(
                                NamespacedKey.minecraft(Objects.requireNonNull(
                                        Main.getInstance().getConfig().getString("kits.rogue.jump.effect")).toLowerCase()));

                        int duration = Main.getInstance().getConfig().getInt("kits.rogue.jump.duration");
                        int amplifier = Main.getInstance().getConfig().getInt("kits.rogue.jump.amplifier");

                        PotionEffect potionEffect = new PotionEffect(Objects.requireNonNull(effectType), duration, amplifier, true, false);
                        if (mainHand) {
                            if (p.getInventory().getItemInMainHand().getAmount() == 1) {
                                p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                            } else {
                                p.getInventory().getItemInMainHand().setAmount(
                                        p.getInventory().getItemInMainHand().getAmount() - 1);
                            }
                        } else {
                            if (p.getInventory().getItemInOffHand().getAmount() == 1) {
                                p.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                            } else {
                                p.getInventory().getItemInOffHand().setAmount(
                                        p.getInventory().getItemInOffHand().getAmount() - 1);
                            }
                        }
                        p.addPotionEffect(potionEffect);
                        PlayerTimer timer = new PlayerTimer(p.getUniqueId(), Timer.JUMPEFFECT);
                        user.addTimer(timer);
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.kit.jump-active"))));

                        // Restore Rogue passive effects after duration
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (user.getUserClass() == HCFClass.ROGUE &&
                                        HCFClass.getActiveClass(p) == HCFClass.ROGUE) {
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2, true, false));
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 1, true, false));
                                }
                            }
                        }.runTaskLater(Main.getInstance(), duration + 5);

                        return;
                    }
                }
            }

            if (user.getUserClass() != null && user.getUserClass().equals(HCFClass.BARD)) {
                if (e.getItem() != null && !e.getItem().getType().equals(Material.AIR)) {

                    ItemStack item = e.getItem();
                    boolean mainHand = e.getHand() == EquipmentSlot.HAND;
                    ConfigurationSection itemsSection = Main.getInstance().getConfig().getConfigurationSection("kits.bard.items");
                    if (itemsSection == null) return;

                    for (String s : itemsSection.getKeys(false)) {
                        String basePath = "kits.bard.items." + s;
                        if (Main.getInstance().getConfig().getBoolean(basePath + ".hold")) continue;

                        String itemName = Main.getInstance().getConfig().getString(basePath + ".item");
                        if (itemName == null) continue;

                        Material itemMaterial = Material.matchMaterial(itemName);
                        if (itemMaterial == null) continue;

                        if (!item.getType().equals(itemMaterial)) continue;

                        e.setCancelled(true);

                        if (!Main.getInstance().getConfig().getBoolean("kits.bard.click-effects-in-spawn")) {
                            Faction in = Faction.getAt(p.getLocation());
                            if (in != null && in.getType().equals(FactionType.SAFEZONE)) {
                                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.kit.bard.safezone"))));
                                return;
                            }
                        }

                        if (!Main.getInstance().getConfig().getBoolean("kits.bard.click-effects-combat") && user.hasTimer("combat")) {
                            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.timer.player.cannot-do"))));
                            return;
                        }

                        if (user.getBardEnergy().doubleValue() < Main.getInstance().getConfig().getInt(basePath + ".energy")) {
                            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.kit.bard.not-enough"))));
                            return;
                        }

                        if (user.hasTimer("pvp") || user.hasTimer("starting")) {
                            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.timer.player.cannot-do-pvp"))));
                            return;
                        }

                        if (user.hasTimer("bardeffect")) {
                            String message = Locale.get().getString("events.timer.player.cooldown.bardeffect") == null ? Locale.get().getString("events.timer.player.cooldown.default") : Locale.get().getString("events.timer.cooldown.bardeffect");
                            message = Objects.requireNonNull(message).replace("%time%", Timer.format(user.getTimer("bardeffect").getDuration()));
                            p.sendMessage(C.chat(message));
                            return;
                        }

                        String effectName = Main.getInstance().getConfig().getString(basePath + ".effect");
                        if (effectName == null) continue;

                        PotionEffectType effectType = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(effectName.toLowerCase()));
                        if (effectType == null) continue;

                        int duration = Main.getInstance().getConfig().getInt(basePath + ".duration");
                        int amplifier = Main.getInstance().getConfig().getInt(basePath + ".amplifier");

                        PotionEffect potionEffect = new PotionEffect(effectType, duration, amplifier, true, false);

                        if (Main.getInstance().getConfig().getBoolean(basePath + ".applyOnBard")) {
                            p.addPotionEffect(potionEffect);
                        }

                        PlayerTimer timer = new PlayerTimer(p.getUniqueId(), Timer.BARDEFFECT);
                        user.addTimer(timer);
                        user.setBardEnergy(user.getBardEnergy().subtract(BigDecimal.valueOf(Main.getInstance().getConfig().getInt(basePath + ".energy"))));

                        if (mainHand) {
                            if (p.getInventory().getItemInMainHand().getAmount() == 1) {
                                p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                            } else {
                                p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
                            }
                        } else {
                            if (p.getInventory().getItemInOffHand().getAmount() == 1) {
                                p.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                            } else {
                                p.getInventory().getItemInOffHand().setAmount(p.getInventory().getItemInOffHand().getAmount() - 1);
                            }
                        }

                        if (!Main.getInstance().getConfig().getBoolean(basePath + ".otherFaction")) {
                            if (user.hasFaction()) {
                                Faction faction = Faction.get(user.getFaction());
                                for (Player player : faction.getOnlineMembers()) {
                                    if (player.equals(p)) continue;
                                    if (!player.getWorld().equals(p.getWorld())) continue;
                                    if (player.getLocation().distance(p.getLocation()) > Main.getInstance().getConfig().getInt("kits.bard.effect-range"))
                                        continue;
                                    player.addPotionEffect(potionEffect);
                                }
                            }
                        } else {
                            Collection<Entity> nearby = p.getWorld().getNearbyEntities(p.getLocation(), Main.getInstance().getConfig().getInt("kits.bard.effect-range"), Main.getInstance().getConfig().getInt("kits.bard.effect-range"), Main.getInstance().getConfig().getInt("kits.bard.effect-range"));
                            for (Entity entity : nearby) {
                                if (!(entity instanceof Player player)) continue;
                                User play = User.get(player);
                                if (user.hasFaction() && play.hasFaction() && play.getFaction().equals(user.getFaction())) continue;
                                if (player.equals(p)) continue;
                                player.addPotionEffect(potionEffect);
                            }
                        }
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.kit.bard.used-effect")).replace("%effect%", Objects.requireNonNull(Main.getInstance().getConfig().getString(basePath + ".name"))).replace("%energy%", Main.getInstance().getConfig().getInt(basePath + ".energy") + "")));
                        return;
                    }
                }
            }
        }
        if (a.equals(Action.RIGHT_CLICK_BLOCK)) {
            Block block = e.getClickedBlock();
            // Check if the block is a container
            if (Objects.requireNonNull(block).getState() instanceof Container) {
                // Support for double chests
                List<Block> chestBlocks = new ArrayList<>();
                BlockState state = block.getState();
                if (state instanceof Chest chest) {
                    Inventory inv = chest.getInventory();
                    if (inv instanceof DoubleChestInventory doubleInv) {
                        DoubleChest doubleChest = doubleInv.getHolder();
                        Chest left = (Chest) doubleChest.getLeftSide();
                        Chest right = (Chest) doubleChest.getRightSide();
                        chestBlocks.add(left.getBlock());
                        chestBlocks.add(right.getBlock());
                    } else {
                        chestBlocks.add(block);
                    }
                } else {
                    chestBlocks.add(block);
                }

                boolean subclaimFound = false;
                String subclaimOwner = null;

                for (Block chestBlock : chestBlocks) {
                    for (BlockFace face : BlockFace.values()) {
                        if (face == BlockFace.SELF) continue;
                        Block relative = chestBlock.getRelative(face);
                        BlockState relState = relative.getState();
                        if (relState instanceof Sign foundSign) {
                            boolean attached = false;
                            if (relative.getBlockData() instanceof org.bukkit.block.data.Directional directional) {
                                if (relative.getRelative(directional.getFacing().getOppositeFace()).equals(chestBlock)) {
                                    attached = true;
                                }
                            }
                            String firstLine = C.strip(C.serialize(foundSign.line(0)));
                            if (attached && firstLine.equalsIgnoreCase("[Subclaim]")) {
                                subclaimFound = true;
                                subclaimOwner = C.strip(C.serialize(foundSign.line(1)));
                                break;
                            }
                        }
                    }
                    if (subclaimFound) break;
                }

                if (subclaimFound && !user.isFactionBypass()) {
                    if (!p.getName().equalsIgnoreCase(subclaimOwner)) {
                        e.setCancelled(true);
                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.subclaim.cannot-open"))
                                .replace("%name%", subclaimOwner)));
                        return;
                    }
                }
            }
            if (block.getState() instanceof Sign sign) {
                List<Component> lines = sign.getSide(Side.FRONT).lines();
                if (!C.serialize(lines.getFirst()).equalsIgnoreCase(C.chat("&9[Elevator]"))) return;
                e.setCancelled(true);
                String type = C.serialize(lines.get(1));
                if (!type.equalsIgnoreCase("up") && !type.equalsIgnoreCase("down")) {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.error"))));
                    return;
                }
                int x = block.getX();
                int z = block.getZ();
                int y = block.getY();
                int maxY = block.getWorld().getMaxHeight();
                int minY = block.getWorld().getMinHeight();

                Location target = null;

                if (type.equalsIgnoreCase("up")) {
                    for (int ny = y + 2; ny <= maxY; ny++) {
                        Block feet = block.getWorld().getBlockAt(x, ny, z);
                        Block head = block.getWorld().getBlockAt(x, ny + 1, z);
                        Block below = block.getWorld().getBlockAt(x, ny - 1, z);
                        if (isPassableForElevator(feet) && isPassableForElevator(head) && below.getType().isSolid()) {
                            target = new Location(block.getWorld(), x + 0.5, ny, z + 0.5);
                            break;
                        }
                    }
                } else if (type.equalsIgnoreCase("down")) {
                    for (int ny = y - 1; ny >= minY; ny--) {
                        Block feet = block.getWorld().getBlockAt(x, ny, z);
                        Block head = block.getWorld().getBlockAt(x, ny + 1, z);
                        Block below = block.getWorld().getBlockAt(x, ny - 1, z);
                        if (isPassableForElevator(feet) && isPassableForElevator(head) && below.getType().isSolid()) {
                            target = new Location(block.getWorld(), x + 0.5, ny, z + 0.5);
                            break;
                        }
                    }
                }

                if (target != null) {
                    Location finalTarget = target;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            p.teleport(finalTarget, PlayerTeleportEvent.TeleportCause.PLUGIN);
                        }
                    }.runTaskLater(Main.getInstance(), 1);
                } else {
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.no-safe-location"))));
                }
            }
            Material type = Objects.requireNonNull(block).getType();
            if (Tag.TRAPDOORS.isTagged(type) || type.equals(Material.LEVER) || Tag.WOODEN_DOORS.isTagged(type) || Tag.BUTTONS.isTagged(type) || Tag.ANVIL.isTagged(type)
                    || type.equals(Material.BARREL) || type.equals(Material.BEACON) || type.equals(Material.BLAST_FURNACE) || type.equals(Material.CARTOGRAPHY_TABLE)
                    || type.equals(Material.CHEST) || type.equals(Material.CRAFTER) || type.equals(Material.CRAFTING_TABLE) || type.equals(Material.DISPENSER)
                    || type.equals(Material.DROPPER) || type.equals(Material.TRAPPED_CHEST) || type.equals(Material.FURNACE) || type.equals(Material.SMOKER)
                    || type.equals(Material.HOPPER) || type.equals(Material.LOOM) || type.equals(Material.SHULKER_BOX) || type.equals(Material.SMITHING_TABLE)
                    || type.equals(Material.STONECUTTER) || Tag.FENCE_GATES.isTagged(type) || type.equals(Material.GRINDSTONE) || type.equals(Material.TNT)
                    || type.equals(Material.TNT_MINECART) || type.equals(Material.BREWING_STAND) || type.equals(Material.FLOWER_POT) || type.equals(Material.DECORATED_POT)) {
                if (Tag.TRAPDOORS.isTagged(type) && type.equals(Material.IRON_TRAPDOOR)) return;
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
                if (type.equals(Material.ENDER_CHEST) && Main.getInstance().getConfig().getBoolean("limiters.disable-enderchest")) {
                    e.setCancelled(true);
                    p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("primary.feature-disabled"))));
                    return;
                }
            }
            ItemStack item = e.getItem();
            if (item == null) return;
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;
            if (!(meta instanceof Damageable)) return;
            NamespacedKey use = new NamespacedKey(Main.getInstance(), "crowbar_uses");
            NamespacedKey max = new NamespacedKey(Main.getInstance(), "maxUses");
            if (!meta.getPersistentDataContainer().has(use)) return;
            int usesLeft = meta.getPersistentDataContainer().getOrDefault(use, PersistentDataType.INTEGER, 0);
            int maxUses = meta.getPersistentDataContainer().getOrDefault(max, PersistentDataType.INTEGER, 0);
            if (usesLeft == 0 || maxUses == 0) {
                p.getInventory().remove(item);
                return;
            }
            if (block.getType() != Material.SPAWNER) return;

            BlockState state = block.getState();
            if (!(state instanceof CreatureSpawner spawner)) return;
            Main.getInstance().getServer().dispatchCommand(Main.getInstance().getServer().getConsoleSender(), "spawner " + Objects.requireNonNull(spawner.getSpawnedType()).name() + " " + p.getName());

            block.setType(Material.AIR);
            usesLeft--;

            if (usesLeft <= 0) {
                if (e.getHand() == EquipmentSlot.HAND) {
                    p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                } else if (e.getHand() == EquipmentSlot.OFF_HAND) {
                    p.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                }
            } else {
                ItemStack newCrowbar = CrowbarCommand.getCrowbar(usesLeft, maxUses);
                ItemMeta newMeta = Objects.requireNonNull(newCrowbar).getItemMeta();
                if (newMeta instanceof Damageable newDamageable) {
                    int maxDurability = newCrowbar.getType().getMaxDurability();
                    int damage = maxDurability - (int) ((usesLeft / (double) maxUses) * maxDurability);
                    newDamageable.setDamage(damage);
                    newCrowbar.setItemMeta(newDamageable);
                }
                if (e.getHand() == EquipmentSlot.HAND) {
                    p.getInventory().setItemInMainHand(newCrowbar);
                } else if (e.getHand() == EquipmentSlot.OFF_HAND) {
                    p.getInventory().setItemInOffHand(newCrowbar);
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
        e.setCancelled(isCrowbar(p.getInventory().getItemInMainHand()));
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
        e.setCancelled(isCrowbar(p.getInventory().getItemInMainHand()));
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

    public static boolean isCrowbar(ItemStack used) {
        if (used == null) return false;
        if (used.getItemMeta() == null) return false;
        ItemMeta meta = used.getItemMeta();
        NamespacedKey use = new NamespacedKey(Main.getInstance(), "crowbar_uses");
        return meta.getPersistentDataContainer().has(use);
    }

    private boolean isPassableForElevator(Block block) {
        return block.isEmpty() || block.isPassable();
    }

    @EventHandler
    public void onMinecart(VehicleEnterEvent e) {
        if (!Main.getInstance().getConfig().getBoolean("elevators.minecart")) return;
        if (e.getVehicle() instanceof org.bukkit.entity.Minecart && e.getEntered() instanceof Player p) {
            if (User.get(p) == null) return;
            if (User.get(p).getModMode() != null) {
                e.setCancelled(true);
                p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.mod-mode.cant"))));
                return;
            }
            Location l = e.getVehicle().getLocation();
            Location loc = new Location(p.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), p.getLocation().getYaw(), p.getLocation().getPitch());
            Material m = loc.getBlock().getType();
            if (Tag.FENCE_GATES.isTagged(m) || loc.getBlock().getState() instanceof Sign) {
                e.setCancelled(true);
                p.teleport(teleportSpot(loc, loc.getBlockY(), loc.getWorld().getMaxHeight() - 1));
            }
        }
    }

    public Location teleportSpot(Location loc, int min, int max) {
        for (int k = min; k < max; k++) {
            Block m1 = (new Location(loc.getWorld(), loc.getBlockX(), k, loc.getBlockZ())).getBlock();
            Block m2 = (new Location(loc.getWorld(), loc.getBlockX(), (k + 1), loc.getBlockZ())).getBlock();
            if (isPassableForElevator(m1) && isPassableForElevator(m2)) return new Location(loc.getWorld(), loc.getBlockX() + 0.5, k, loc.getBlockZ() + 0.5, loc.getYaw(), loc.getPitch());
        }
        return new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()), loc.getBlockZ() + 0.5, loc.getYaw(), loc.getPitch());
    }

    private Material getSmeltedResult(Material ore) {
        return switch (ore) {
            case IRON_ORE, DEEPSLATE_IRON_ORE -> Material.IRON_INGOT;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE, NETHER_GOLD_ORE -> Material.GOLD_INGOT;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> Material.COPPER_INGOT;
            case ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP;
            case NETHER_QUARTZ_ORE -> Material.QUARTZ;
            default -> null;
        };
    }

    private int getSmeltXP(Material ore) {
        return switch (ore) {
            case IRON_ORE, DEEPSLATE_IRON_ORE, GOLD_ORE, DEEPSLATE_GOLD_ORE, NETHER_GOLD_ORE, COPPER_ORE,
                 DEEPSLATE_COPPER_ORE -> 1;
            case ANCIENT_DEBRIS, NETHER_QUARTZ_ORE -> 2;
            default -> 0;
        };
    }
}
