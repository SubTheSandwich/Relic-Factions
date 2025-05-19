package me.sub.RelicFactions.Files.Classes;

import me.sub.RelicFactions.Files.Data.Claim;
import me.sub.RelicFactions.Files.Data.PlayerTimer;
import me.sub.RelicFactions.Files.Data.UserData;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.Maps;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

public class User {

    private final UserData userData;
    private final UUID uuid;
    private String name;
    private UUID faction;
    private boolean modified;
    private boolean deathbanned;
    private int kills;
    private int deaths;
    private BigDecimal balance;
    private Claim claim;
    private final HashMap<String, PlayerTimer> timers;

    public User(UserData userData) {
        this.userData = userData;
        this.uuid = userData.getUUID();
        this.name = userData.getName();
        this.faction = userData.get().getString("faction") == null ? null : UUID.fromString(userData.get().getString("faction"));
        deathbanned = userData.get().getBoolean("deathban.has");
        kills = userData.get().getInt("kills");
        deaths = userData.get().getInt("deaths");
        balance = BigDecimal.valueOf(userData.get().getDouble("balance"));
        timers = Maps.stringToTimers(userData.get().getString("timers"));
        modified = false;
        claim = null;
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
        timers.put(timer.getTimer().name(), timer);
    }
}
