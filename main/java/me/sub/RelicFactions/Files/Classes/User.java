package me.sub.RelicFactions.Files.Classes;

import me.sub.RelicFactions.Files.Data.UserData;
import me.sub.RelicFactions.Main.Main;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class User {

    private final UserData userData;
    private final UUID uuid;
    private String name;
    private UUID faction;
    private boolean modified;
    private final ArrayList<UUID> invites;
    private boolean deathbanned;
    private int kills;
    private int deaths;

    public User(UserData userData) {
        this.userData = userData;
        this.uuid = userData.getUUID();
        this.name = userData.getName();
        this.faction = userData.get().getString("faction") == null ? null : UUID.fromString(userData.get().getString("faction"));
        invites = new ArrayList<>();
        deathbanned = userData.get().getBoolean("deathban.has");
        kills = userData.get().getInt("kills");
        deaths = userData.get().getInt("deaths");
        modified = false;
    }

    public static User get(UUID uuid) {
        return Main.getInstance().users.getOrDefault(uuid, null);
    }

    public static User get(Player p) {
        return Main.getInstance().users.getOrDefault(p.getUniqueId(), null);
    }

    public static User get(String name) {
        return Main.getInstance().userNameHolder.getOrDefault(name, null);
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

    public ArrayList<UUID> getInvites() {
        return invites;
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
}
