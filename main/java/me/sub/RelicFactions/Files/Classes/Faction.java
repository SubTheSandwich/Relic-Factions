package me.sub.RelicFactions.Files.Classes;

import me.sub.RelicFactions.Files.Data.FactionData;
import me.sub.RelicFactions.Files.Enums.Color;
import me.sub.RelicFactions.Files.Enums.FactionType;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Maps;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
    private int points;
    private int kothCaptures;
    private int lives;
    private String announcement;

    public Faction(FactionData factionData) {
        this.factionData = factionData;
        this.uuid = factionData.getUUID();
        this.name = factionData.getName();
        this.leader = factionData.get().getString("leader") == null ? null : UUID.fromString(factionData.get().getString("leader"));
        this.factionType = FactionType.valueOf(factionData.get().getString("type"));
        this.color = factionData.get().getString("color") == null ? null : Color.valueOf(factionData.get().getString("color"));
        members = Maps.deserialize(factionData.get().getString("members"));
        open = factionData.get().getBoolean("open");
        balance = BigDecimal.valueOf(factionData.get().getDouble("balance"));
        points = factionData.get().getInt("points");
        kothCaptures = factionData.get().getInt("koth-captures");
        lives = factionData.get().getInt("lives");
        announcement = factionData.get().getString("announcement") == null ? null : factionData.get().getString("announcement");
        modified = false;
    }

    public static Faction get(UUID uuid) {
        return Main.getInstance().factions.getOrDefault(uuid, null);
    }
    public static Faction get(String name) {
        return Main.getInstance().factionNameHolder.getOrDefault(name, null);
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

    public String getValidName(Player p) {
        User user = User.get(p);
        if (factionType.equals(FactionType.PLAYER)) {
            if (!user.hasFaction()) {
                return C.chat("&c" + name);
            } else {
                if (user.getFaction().equals(uuid)) {
                    return C.chat("&a" + name);
                } else {
                    return C.chat("&c" + name);
                }
            }
        } else if (factionType.equals(FactionType.KOTH)) {
            return C.chat(color.toColorCode() + name + " KOTH");
        } else if (factionType.equals(FactionType.ROAD)) {
            return C.chat(color.toColorCode() + name + " Road");
        } else {
            return C.chat(color.toColorCode() + name);
        }
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
        if (Bukkit.getPlayer(leader) != null) players.add(Bukkit.getPlayer(leader));
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
}
