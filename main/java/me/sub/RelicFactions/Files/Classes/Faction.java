package me.sub.RelicFactions.Files.Classes;

import me.sub.RelicFactions.Files.Data.Cuboid;
import me.sub.RelicFactions.Files.Data.FactionData;
import me.sub.RelicFactions.Files.Enums.Color;
import me.sub.RelicFactions.Files.Enums.FactionType;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.*;

public class Faction {

    private final FactionData factionData;
    private final UUID uuid;
    private String name;
    private boolean modified;
    private FactionType factionType;
    private UUID leader;
    private Color color;
    private HashMap<UUID, Integer> members;
    private boolean open;
    private BigDecimal balance;
    private BigDecimal dtr;
    private int points;
    private int kothCaptures;
    private int lives;
    private String announcement;
    private Location home;
    private final ArrayList<UUID> invites;
    private ArrayList<Cuboid> claims;
    private boolean deathban;

    public Faction(FactionData factionData) {
        this.factionData = factionData;
        this.uuid = factionData.getUUID();
        this.name = factionData.getName();
        this.leader = factionData.get().getString("leader") == null ? null : UUID.fromString(Objects.requireNonNull(factionData.get().getString("leader")));
        this.factionType = FactionType.valueOf(factionData.get().getString("type"));
        this.color = factionData.get().getString("color") == null ? null : Color.valueOf(factionData.get().getString("color"));
        members = Maps.deserialize(factionData.get().getString("members"));
        open = factionData.get().getBoolean("open");
        balance = BigDecimal.valueOf(factionData.get().getDouble("balance"));
        dtr = BigDecimal.valueOf(factionData.get().getDouble("dtr"));
        points = factionData.get().getInt("points");
        kothCaptures = factionData.get().getInt("koth-captures");
        lives = factionData.get().getInt("lives");
        announcement = factionData.get().getString("announcement") == null ? null : factionData.get().getString("announcement");
        home = factionData.get().getLocation("home") == null ? null : factionData.get().getLocation("home");
        invites = Maps.stringToUuidList(factionData.get().getString("invites"));
        claims = Maps.stringToCuboidList(factionData.get().getString("claims"));
        deathban = factionData.get().getBoolean("deathban");
        modified = false;
    }

    public static boolean isInWilderness(Location location, int boundary) {
        return Math.abs(location.getBlockX()) < boundary && Math.abs(location.getBlockZ()) < boundary;
    }

    public static Faction get(UUID uuid) {
        return Main.getInstance().factions.getOrDefault(uuid, null);
    }
    public static Faction get(String name) {
        return Main.getInstance().factionNameHolder.getOrDefault(name.toLowerCase(), null);
    }

    public void setDeathban(boolean deathban) {
        modified = true;
        this.deathban = deathban;
    }

    public FactionData getUserData() {
        return factionData;
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

    public boolean isModified() {
        return modified;
    }

    public FactionData getFactionData() {
        return factionData;
    }

    public void setType(FactionType factionType) {
        modified = true;
        this.factionType = factionType;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        modified = true;
        this.leader = leader;
    }

    public String getValidName(Player p, boolean includeDeathban) {
        User user = User.get(p);
        String validName;
        if (factionType.equals(FactionType.PLAYER)) {
            if (!user.hasFaction()) {
                validName = C.chat("&c" + name);
            } else {
                if (user.getFaction().equals(uuid)) {
                    validName = C.chat("&a" + name);
                } else {
                    validName = C.chat("&c" + name);
                }
            }
        } else if (factionType.equals(FactionType.KOTH)) {
            validName = C.chat(color.toColorCode() + name + " KOTH");
        } else if (factionType.equals(FactionType.ROAD)) {
            validName = C.chat(color.toColorCode() + name + " Road");
        } else {
            validName = C.chat(color.toColorCode() + name);
        }
        if (includeDeathban) {
            validName += " ";
            validName += isDeathban()
                    ? Locale.get().getString("faction.deathban")
                    : Locale.get().getString("faction.non-deathban");
        }
        return validName;
    }

    public boolean isDeathban() {
        return deathban;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getRoleID(UUID uuid) {
        /*
        leader = 3
        coleader = 2
        captain = 1
        member = 0
        not in = -1
         */
        if (leader.equals(uuid)) return 3;
        if (members.containsKey(uuid)) return members.get(uuid);
        return -1;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        modified = true;
        this.open = open;
    }

    public HashMap<UUID, Integer> getMembers() {
        return members;
    }

    public void setMembers(HashMap<UUID, Integer> members) {
        modified = true;
        this.members = members;
    }

    public FactionType getType() {
        return factionType;
    }

    public Set<Player> getOnlineMembers() {
        Set<Player> players = new HashSet<>();
        for (UUID u : members.keySet()) {
            Player player = Bukkit.getPlayer(u);
            if (player == null) continue;
            players.add(player);
        }
        return players;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        modified = true;
        this.balance = balance;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        modified = true;
        this.points = points;
    }

    public int getKothCaptures() {
        return kothCaptures;
    }

    public void setKothCaptures(int kothCaptures) {
        modified = true;
        this.kothCaptures = kothCaptures;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        modified = true;
        this.lives = lives;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        modified = true;
        this.announcement = announcement;
    }

    public void setModified(boolean b) {
        modified = b;
    }

    public Location getHome() {
        return home;
    }

    public void setHome(Location home) {
        modified = true;
        this.home = home;
    }

    public ArrayList<UUID> getInvites() {
        modified = true;
        return invites;
    }

    public BigDecimal getDTR() {
        return dtr;
    }

    public void setDTR(BigDecimal dtr) {
        modified = true;
        this.dtr = dtr;
    }

    public ArrayList<Cuboid> getClaims() {
        return claims;
    }

    public void setClaims(ArrayList<Cuboid> claims) {
        modified = true;
        this.claims = claims;
    }

    public static Faction getAt(Location location) {
        Location clone = location.clone();
        clone.setY(0);
        if (Main.getInstance().factions.isEmpty()) return null;
        for (Faction faction : Main.getInstance().factions.values()) {
            if (faction.getClaims().isEmpty()) continue;
            for (Cuboid cuboid : faction.getClaims()) {
                if (cuboid.isIn(clone)) return faction;
            }
        }
        return null;
    }

    public static boolean hasAt(Location location) {
        Location clone = location.clone();
        clone.setY(0);
        if (Main.getInstance().factions.isEmpty()) return false;
        for (Faction faction : Main.getInstance().factions.values()) {
            if (faction.getClaims().isEmpty()) continue;
            for (Cuboid cuboid : faction.getClaims()) {
                if (cuboid.isIn(location)) return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Faction{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                ", factionType=" + factionType +
                '}';
    }
}
