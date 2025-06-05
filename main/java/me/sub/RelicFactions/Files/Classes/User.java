package me.sub.RelicFactions.Files.Classes;

import me.sub.RelicFactions.Files.Data.*;
import me.sub.RelicFactions.Files.Enums.ChatType;
import me.sub.RelicFactions.Files.Data.CustomTimer;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.Maps;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
        lastInventoryContents = userData.get().getString("lastInventoryContents") == null ? null : Maps.fromBase64(userData.get().getString("lastInventoryContents"));
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

        modified = false;
        lastMessaged = null;
        revived = false;
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
            timers.forEach((s, timer) -> timer.tick());
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
}
