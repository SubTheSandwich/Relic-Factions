package me.sub.RelicFactions.Files.Classes;

import me.sub.RelicFactions.Files.Data.*;
import me.sub.RelicFactions.Files.Data.Note;
import me.sub.RelicFactions.Files.Enums.ChatType;
import me.sub.RelicFactions.Files.Data.CustomTimer;
import me.sub.RelicFactions.Files.Enums.FactionType;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Maps;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.math.BigDecimal;
import java.util.*;

public class User {

    private boolean userDisconnected;
    private final UserData userData;
    private final UUID uuid;
    private String name;
    private UUID faction;
    private boolean modified;
    private boolean deathbanned;
    private long deathbannedTill;
    private int kills;
    private int deaths;
    private BigDecimal balance;
    private Claim claim;
    private final HashMap<String, PlayerTimer> timers;
    private final HashMap<String, CustomTimer> customTimers;
    private boolean factionBypass;
    private HashMap<Faction, List<Cuboid>> map;
    private final Filter filter;
    private int lives;
    private UUID loggerUUID;
    private ItemStack[] lastInventoryContents;
    private ChatType chatType;
    private boolean isStaffChat;
    private ModMode modMode;
    private boolean isFrozen;
    private boolean isPanic;
    private long storedPlaytime;
    private long lastLoginTimestamp;
    private boolean globalChat;
    private boolean messages;
    private boolean messageSounds;
    private UUID lastMessaged;
    private boolean revived;
    private boolean scoreboard;
    private int coalMined;
    private int ironMined;
    private int copperMined;
    private int goldMined;
    private int redstoneMined;
    private int lapisMined;
    private int diamondMined;
    private int emeraldMined;
    private int quartzMined;
    private int debrisMined;
    private boolean foundDiamonds;
    private Location stuckLocation;
    private boolean mountains;
    private ArrayList<Note> notes;
    private HCFClass hcfClass;
    private BigDecimal bardEnergy;

    public User(UserData userData) {
        userDisconnected = true;
        this.userData = userData;
        this.uuid = userData.getUUID();
        this.name = userData.getName();
        this.faction = userData.get().getString("faction") == null ? null : UUID.fromString(Objects.requireNonNull(userData.get().getString("faction")));
        deathbanned = userData.get().getBoolean("deathban.has");
        deathbannedTill = userData.get().getLong("deathban.ends");
        kills = userData.get().getInt("kills");
        deaths = userData.get().getInt("deaths");
        balance = BigDecimal.valueOf(userData.get().getDouble("balance"));
        timers = Maps.stringToTimers(userData.get().getString("timers"));
        customTimers = Maps.deserializeCustomMap(userData.get().getString("customTimers"));
        lives = userData.get().getInt("lives");
        modified = false;
        claim = null;
        factionBypass = false;
        map = null;
        filter = new Filter();
        loggerUUID = userData.get().getString("loggerUUID") == null ? null : UUID.fromString(Objects.requireNonNull(userData.get().getString("loggerUUID")));
        lastInventoryContents = userData.get().getString("lastInventoryContents") == null ? null : Maps.fromBase64(this, userData.get().getString("lastInventoryContents"));
        chatType = ChatType.PUBLIC;
        isStaffChat = false;
        modMode = null;
        isFrozen = false;
        isPanic = false;
        storedPlaytime = userData.get().getLong("playtime");
        lastLoginTimestamp = 0;
        globalChat = userData.get().getBoolean("settings.global-chat");
        messages = userData.get().getBoolean("settings.messages.enabled");
        messageSounds = userData.get().getBoolean("settings.messages.sounds");
        foundDiamonds = userData.get().getBoolean("settings.foundDiamonds");
        mountains = userData.get().getBoolean("settings.mountains");
        scoreboard = userData.get().getBoolean("settings.scoreboard");
        coalMined = userData.get().getInt("ores.coal");
        ironMined = userData.get().getInt("ores.iron");
        copperMined = userData.get().getInt("ores.copper");
        goldMined = userData.get().getInt("ores.gold");
        redstoneMined = userData.get().getInt("ores.redstone");
        lapisMined = userData.get().getInt("ores.lapis");
        diamondMined = userData.get().getInt("ores.diamond");
        emeraldMined = userData.get().getInt("ores.emerald");
        quartzMined = userData.get().getInt("ores.quartz");
        debrisMined = userData.get().getInt("ores.debris");
        stuckLocation = null;
        notes = Note.deserializeList(userData.get().getString("notes"));
        modified = false;
        lastMessaged = null;
        revived = false;
        hcfClass = null;
        bardEnergy = BigDecimal.ZERO;
    }

    public BigDecimal getBardEnergy() {
        return bardEnergy;
    }

    public void setBardEnergy(BigDecimal bardEnergy) {
        this.bardEnergy = bardEnergy;
    }

    public HCFClass getUserClass() {
        return hcfClass;
    }

    public void setUserClass(HCFClass hcfClass) {
        this.hcfClass = hcfClass;
    }

    public UUID getLoggerUUID() {
        return loggerUUID;
    }

    public void setLoggerUUID(UUID loggerUUID) {
        modified = true;
        this.loggerUUID = loggerUUID;
    }

    public boolean isFactionBypass() {
        return factionBypass;
    }

    public void setFactionBypass(boolean factionBypass) {
        this.factionBypass = factionBypass;
    }

    public static User get(UUID uuid) {
        return Main.getInstance().users.getOrDefault(uuid, null);
    }

    public static User get(OfflinePlayer p) {
        return Main.getInstance().users.getOrDefault(p.getUniqueId(), null);
    }

    public static User get(Player p) {
        return Main.getInstance().users.getOrDefault(p.getUniqueId(), null);
    }

    public static User get(String name) {
        return Main.getInstance().userNameHolder.getOrDefault(name.toLowerCase(), null);
    }

    public UserData getUserData() {
        return userData;
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

    public UUID getFaction() {
        return faction;
    }

    public void setFaction(UUID faction) {
        modified = true;
        this.faction = faction;
    }

    public boolean isModified() {
        return modified;
    }

    public boolean hasFaction() {
        return faction != null;
    }

    public boolean isDeathBanned() {
        return deathbanned;
    }

    public void setDeathBanned(boolean deathbanned) {
        modified = true;
        this.deathbanned = deathbanned;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        modified = true;
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        modified = true;
        this.deaths = deaths;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        modified = true;
        this.balance = balance;
    }

    public void setModified(boolean b) {
        modified = b;
    }

    public Claim getClaim() {
        return claim;
    }

    public void setClaim(Claim claim) {
        this.claim = claim;
    }

    public HashMap<String, PlayerTimer> getTimers() {
        return timers;
    }

    public void addTimer(PlayerTimer timer) {
        modified = true;
        timers.put(timer.getTimer().name().toUpperCase(), timer);
    }

    public void removeTimer(String name) {
        modified = true;
        timers.remove(name.toUpperCase());
    }

    public PlayerTimer getTimer(String name) {
        return timers.getOrDefault(name.toUpperCase(), null);
    }

    public boolean hasTimer(String name) {
        return getTimer(name) != null;
    }

    public boolean isDisconnected() {
        return userDisconnected;
    }

    public void setDisconnected(boolean userDisconnected) {
        this.userDisconnected = userDisconnected;
        if (!userDisconnected && !timers.isEmpty()) {
            timers.forEach((_, timer) -> timer.tick());
        }
    }

    public HashMap<Faction, List<Cuboid>> getMap() {
        return map;
    }

    public void setMap(HashMap<Faction, List<Cuboid>> map) {
        this.map = map;
    }

    public Filter getFilter() {
        return filter;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        modified = true;
        this.lives = lives;
    }

    public static int getDeathbanTime(Player player) {
        int time = Main.getInstance().getConfig().getInt("deathban.default-time");
        for (String rank : Objects.requireNonNull(Main.getInstance().getConfig().getConfigurationSection("deathban.times")).getKeys(false)) {
            if (!player.hasPermission("relic-factions.deathban." + rank.toLowerCase())) return time;
            time = Main.getInstance().getConfig().getInt("deathban.times." + rank);
        }
        return time;
    }

    public long getDeathbannedTill() {
        return deathbannedTill;
    }

    public void setDeathbannedTill(long deathbannedTill) {
        modified = true;
        this.deathbannedTill = deathbannedTill;
    }

    public ItemStack[] getLastInventoryContents() {
        return lastInventoryContents;
    }

    public void setLastInventoryContents(ItemStack[] lastInventoryContents) {
        modified = true;
        this.lastInventoryContents = lastInventoryContents;
    }

    public ChatType getChatType() {
        return chatType;
    }

    public void setChatType(ChatType chatType) {
        this.chatType = chatType;
    }

    public boolean isStaffChat() {
        return isStaffChat;
    }

    public void setStaffChat(boolean staffChat) {
        isStaffChat = staffChat;
    }

    public ModMode getModMode() {
        return modMode;
    }

    public void setModMode(ModMode modMode) {
        this.modMode = modMode;
    }

    public boolean isFrozen() {
        return isFrozen;
    }

    public void setFrozen(boolean frozen) {
        isFrozen = frozen;
    }

    public boolean isPanic() {
        return isPanic;
    }

    public void setPanic(boolean panic) {
        isPanic = panic;
    }

    public long getLastLoginTimestamp() {
        return lastLoginTimestamp;
    }

    public void setLastLoginTimestamp(long lastLoginTimestamp) {
        this.lastLoginTimestamp = lastLoginTimestamp;
    }

    public long getStoredPlaytime() {
        return storedPlaytime;
    }

    public void setStoredPlaytime(long storedPlaytime) {
        modified = true;
        this.storedPlaytime = storedPlaytime;
    }

    public long getCurrentPlaytime() {
        if (lastLoginTimestamp == 0) {
            return storedPlaytime;
        }
        return storedPlaytime + (System.currentTimeMillis() - lastLoginTimestamp);
    }

    public boolean isGlobalChat() {
        return globalChat;
    }

    public void setGlobalChat(boolean globalChat) {
        modified = true;
        this.globalChat = globalChat;
    }

    public boolean isMessages() {
        return messages;
    }

    public void setMessages(boolean messages) {
        modified = true;
        this.messages = messages;
    }

    public boolean isMessageSounds() {
        return messageSounds;
    }

    public void setMessageSounds(boolean messageSounds) {
        modified = true;
        this.messageSounds = messageSounds;
    }

    public UUID getLastMessaged() {
        return lastMessaged;
    }

    public void setLastMessaged(UUID lastMessaged) {
        this.lastMessaged = lastMessaged;
    }

    public boolean isRevived() {
        return revived;
    }

    public void setRevived(boolean revived) {
        this.revived = revived;
    }

    public boolean isScoreboard() {
        return scoreboard;
    }

    public void setScoreboard(boolean scoreboard) {
        modified = true;
        this.scoreboard = scoreboard;
    }

    public int getDebrisMined() {
        return debrisMined;
    }

    public void setDebrisMined(int debrisMined) {
        modified = true;
        this.debrisMined = debrisMined;
    }

    public int getQuartzMined() {
        return quartzMined;
    }

    public void setQuartzMined(int quartzMined) {
        modified = true;
        this.quartzMined = quartzMined;
    }

    public int getEmeraldMined() {
        return emeraldMined;
    }

    public void setEmeraldMined(int emeraldMined) {
        modified = true;
        this.emeraldMined = emeraldMined;
    }

    public int getDiamondMined() {
        return diamondMined;
    }

    public void setDiamondMined(int diamondMined) {
        modified = true;
        this.diamondMined = diamondMined;
    }

    public int getLapisMined() {
        return lapisMined;
    }

    public void setLapisMined(int lapisMined) {
        modified = true;
        this.lapisMined = lapisMined;
    }

    public int getRedstoneMined() {
        return redstoneMined;
    }

    public void setRedstoneMined(int redstoneMined) {
        modified = true;
        this.redstoneMined = redstoneMined;
    }

    public int getGoldMined() {
        return goldMined;
    }

    public void setGoldMined(int goldMined) {
        modified = true;
        this.goldMined = goldMined;
    }

    public int getCopperMined() {
        return copperMined;
    }

    public void setCopperMined(int copperMined) {
        modified = true;
        this.copperMined = copperMined;
    }

    public int getIronMined() {
        return ironMined;
    }

    public void setIronMined(int ironMined) {
        modified = true;
        this.ironMined = ironMined;
    }

    public int getCoalMined() {
        return coalMined;
    }

    public void setCoalMined(int coalMined) {
        modified = true;
        this.coalMined = coalMined;
    }

    public boolean hasCustomTimer(String name) {
        return customTimers.get(name) != null;
    }

    public void addCustomTimer(CustomTimer timer) {
        modified = true;
        customTimers.put(timer.getName(), timer);
    }

    public void removeCustomTimer(String name) {
        modified = true;
        customTimers.remove(name);
    }

    public HashMap<String, CustomTimer> getCustomTimers() {
        return customTimers;
    }

    public CustomTimer getCustomTimer(String name) {
        return customTimers.getOrDefault(name, null);
    }

    public boolean isFoundDiamonds() {
        return foundDiamonds;
    }

    public void setFoundDiamonds(boolean foundDiamonds) {
        modified = true;
        this.foundDiamonds = foundDiamonds;
    }

    public boolean isMountains() {
        return mountains;
    }

    public void setMountains(boolean mountains) {
        modified = true;
        this.mountains = mountains;
    }

    public Location getStuckLocation() {
        return stuckLocation;
    }

    public void setStuckLocation(Location stuckLocation) {
        this.stuckLocation = stuckLocation;
    }

    public ArrayList<Note> getNotes() {
        return notes;
    }

    public void setNotes(ArrayList<Note> notes) {
        modified = true;
        this.notes = notes;
    }

    public List<Note> getNotesOnPage(int page) {
        int start = (page - 1) * 36;
        int end = Math.min(start + 36, notes.size());
        if (start >= notes.size() || page < 1) {
            return Collections.emptyList();
        }
        return notes.subList(start, end);
    }

    public void updateClass() {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        HCFClass active = HCFClass.getActiveClass(player);
        if (hcfClass != null && active == null) {
            player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.kit.disabled")).replace("%name%", C.capitalizeWord(getUserClass().name()))));
            for (PotionEffectType effect : hcfClass.getPassiveEffectTypes()) {
                player.removePotionEffect(effect);
            }
            setUserClass(null);
            return;
        }
        if (hcfClass == null && active != null) {
            if (!Main.getInstance().getConfig().getBoolean("kits." + active.getConfigKey() + ".enabled", true)) return;
            setUserClass(active);
            for (String s : Locale.get().getStringList("events.kit.enabled")) {
                s = s.replace("%name%", C.capitalizeWord(active.name()));
                player.sendMessage(C.chat(Objects.requireNonNull(s)));
            }
            active.applyPassiveEffects(player);
            // TODO: Bard energy recharge, as well as passive effects
            if (hcfClass.equals(HCFClass.BARD)) {
                new BukkitRunnable() {
                    int messageDelay = 0;
                    @Override
                    public void run() {
                        Player p = Bukkit.getPlayer(uuid);
                        if (messageDelay != 0) messageDelay--;
                        if (p == null) {
                            setBardEnergy(BigDecimal.ZERO);
                            cancel();
                            return;
                        }
                        if (hcfClass == null || !hcfClass.equals(HCFClass.BARD)) {
                            setBardEnergy(BigDecimal.ZERO);
                            cancel();
                            return;
                        }
                        if (getBardEnergy().doubleValue() < Main.getInstance().getConfig().getInt("kits.bard.max-energy")) setBardEnergy(getBardEnergy().add(BigDecimal.valueOf(0.05)));
                        if (!p.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                            ConfigurationSection itemsSection = Main.getInstance().getConfig().getConfigurationSection("kits.bard.items");
                            if (itemsSection == null) return;

                            for (String s : itemsSection.getKeys(false)) {
                                String basePath = "kits.bard.items." + s;
                                if (!Main.getInstance().getConfig().getBoolean(basePath + ".hold")) continue;

                                String itemName = Main.getInstance().getConfig().getString(basePath + ".item");
                                if (itemName == null) continue;

                                Material itemMaterial = Material.matchMaterial(itemName);
                                if (itemMaterial == null) continue;

                                if (!p.getInventory().getItemInMainHand().getType().equals(itemMaterial)) continue;

                                if (!Main.getInstance().getConfig().getBoolean("kits.bard.hold-effects-in-spawn")) {
                                    Faction in = Faction.getAt(p.getLocation());
                                    if (in != null && in.getType().equals(FactionType.SAFEZONE)) {
                                        if (messageDelay == 0) {
                                            messageDelay = 100;
                                            p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.kit.bard.safezone"))));
                                            return;
                                        }
                                    }
                                }

                                if (hasTimer("pvp") || hasTimer("starting")) {
                                    if (messageDelay == 0) {
                                        messageDelay = 100;
                                        p.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.timer.player.cannot-do-pvp"))));
                                        return;
                                    }
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

                                if (hasFaction()) {
                                    Faction faction = Faction.get(getFaction());
                                    for (Player player : faction.getOnlineMembers()) {
                                        if (player.equals(p)) continue;
                                        if (!player.getWorld().equals(p.getWorld())) continue;
                                        if (player.getLocation().distance(p.getLocation()) > Main.getInstance().getConfig().getInt("kits.bard.effect-range")) continue;
                                        player.addPotionEffect(potionEffect);
                                    }
                                }
                            }
                        }
                        if (!p.getInventory().getItemInOffHand().getType().equals(Material.AIR)){
                            ConfigurationSection itemsSection = Main.getInstance().getConfig().getConfigurationSection("kits.bard.items");
                            if (itemsSection == null) return;

                            for (String s : itemsSection.getKeys(false)) {
                                String basePath = "kits.bard.items." + s;
                                if (!Main.getInstance().getConfig().getBoolean(basePath + ".hold")) continue;

                                String itemName = Main.getInstance().getConfig().getString(basePath + ".item");
                                if (itemName == null) continue;

                                Material itemMaterial = Material.matchMaterial(itemName);
                                if (itemMaterial == null) continue;

                                if (!p.getInventory().getItemInOffHand().getType().equals(itemMaterial)) continue;

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

                                if (hasFaction()) {
                                    Faction faction = Faction.get(getFaction());
                                    for (Player player : faction.getOnlineMembers()) {
                                        if (player.equals(p)) continue;
                                        if (!player.getWorld().equals(p.getWorld())) continue;
                                        if (player.getLocation().distance(p.getLocation()) > Main.getInstance().getConfig().getInt("kits.bard.effect-range")) continue;
                                        player.addPotionEffect(potionEffect);
                                    }
                                }
                            }
                        }
                    }
                }.runTaskTimer(Main.getInstance(), 0, 1);
            }
        }
        if (hcfClass != null) {
            if (!hcfClass.equals(HCFClass.MINER)) return;
            double y = player.getLocation().getY();
            if (y <= 20) {
                if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, true, false));
                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.kit.miner.invis-change")).replace("%status%", Objects.requireNonNull(player.hasPotionEffect(PotionEffectType.INVISIBILITY) ? Locale.get().getString("primary.enabled") : Locale.get().getString("primary.disabled")))));
            } else {
                if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                player.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.kit.miner.invis-change")).replace("%status%", Objects.requireNonNull(player.hasPotionEffect(PotionEffectType.INVISIBILITY) ? Locale.get().getString("primary.enabled") : Locale.get().getString("primary.disabled")))));
            }
        }
    }

    public static boolean isBehind(Player attacker, Player target, double maxAngleDegrees) {
        // 1. Target's facing direction (normalized)
        org.bukkit.util.Vector targetFacing = target.getLocation().getDirection().setY(0).normalize();

        // 2. Vector from target to attacker (normalized)
        Vector toAttacker = attacker.getLocation().toVector().subtract(target.getLocation().toVector()).setY(0).normalize();

        // 3. Calculate the angle (in degrees) between the two vectors
        double angle = targetFacing.angle(toAttacker); // in radians
        double angleDegrees = Math.toDegrees(angle);

        // 4. Check if within allowed angle (e.g., 45 degrees)
        return angleDegrees > (180.0 - maxAngleDegrees);
    }
}
