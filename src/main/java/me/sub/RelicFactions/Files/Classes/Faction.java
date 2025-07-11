package me.sub.RelicFactions.Files.Classes;

import me.sub.RelicFactions.Files.Data.Cuboid;
import me.sub.RelicFactions.Files.Data.FactionData;
import me.sub.RelicFactions.Files.Enums.Color;
import me.sub.RelicFactions.Files.Enums.FactionType;
import me.sub.RelicFactions.Files.Enums.Tree;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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
    private boolean ff;
    private long timeTilRegen;
    private boolean isRegening;
    private ArrayList<UUID> allies;
    private ArrayList<UUID> allyRequests;
    private UUID focusedFaction;
    private HashMap<String, Tree> tree;

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
        timeTilRegen = factionData.get().getLong("timeTilRegen");
        isRegening = factionData.get().getBoolean("regening");
        allies = Maps.stringToUuidList(factionData.get().getString("allies"));
        allyRequests = Maps.stringToUuidList(factionData.get().getString("allyRequests"));
        focusedFaction = null;
        ff = false;
        modified = false;
        if (factionData.get().getString("tree") == null) {
            tree = Main.getInstance().getDefaultTree();
        } else {
            ArrayList<Tree> trees = Tree.deserializeTreeList(Objects.requireNonNull(factionData.get().getString("tree")));
            HashMap<String, Tree> map = new HashMap<>();
            for (Tree tree : trees) {
                map.put(tree.getName(), tree);
            }
            tree = map;
        }
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

    public static String formatDTR(BigDecimal dtr) {
        if (dtr.doubleValue() <= 0) {
            return C.chat("&c" + dtr.doubleValue());
        } else if (dtr.doubleValue() <= 0.99) {
            return C.chat("&e" + dtr.doubleValue());
        } else {
            return C.chat("&a" + dtr.doubleValue());
        }
    }

    public void setDeathban(boolean deathban) {
        modified = true;
        this.deathban = deathban;
    }

    public boolean isFF() {
        return ff;
    }

    public void setFF(boolean ff) {
        this.ff = ff;
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
        String baseName;
        if (factionType.equals(FactionType.PLAYER)) {
            if (!user.hasFaction()) {
                baseName = "&c" + name;
            } else {
                if (user.getFaction().equals(uuid)) {
                    baseName = "&a" + name;
                } else if (Faction.get(user.getFaction()).getAllies().contains(uuid)) {
                    baseName = "&9" + name;
                } else {
                    baseName = "&c" + name;
                }
            }
        } else if (factionType.equals(FactionType.MOUNTAIN)) {
            baseName = color.toColorCode() + name + " Mountain";
        } else if (factionType.equals(FactionType.KOTH)) {
            baseName = color.toColorCode() + name + " KOTH";
        } else if (factionType.equals(FactionType.ROAD)) {
            baseName = color.toColorCode() + name + " Road";
        } else {
            baseName = color.toColorCode() + name;
        }
        if (includeDeathban) {
            baseName += " " + Locale.get().getString(
                    isDeathban() ? "faction.deathban" : "faction.non-deathban"
            );
        }
        baseName += "&r";
        return C.chat(baseName);
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

    public String getRoleName(UUID uuid) {
        return switch (getRoleID(uuid)) {
            case 3 -> "leader";
            case 2 -> "coleader";
            case 1 -> "captain";
            case 0 -> "member";
            default -> null;
        };
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

    public static String getAtName(Player p) {
        Location clone = p.getLocation().clone();
        clone.setY(0);
        if (Main.getInstance().factions.isEmpty()) return null;
        for (Faction faction : Main.getInstance().factions.values()) {
            if (faction.getClaims().isEmpty()) continue;
            for (Cuboid cuboid : faction.getClaims()) {
                if (cuboid.isIn(clone)) return faction.getValidName(p, false);
            }
        }
        switch (Objects.requireNonNull(clone.getWorld()).getEnvironment()) {
            case NORMAL, CUSTOM -> {
                if (!Faction.isInWilderness(clone, Main.getInstance().getConfig().getInt("factions.sizes.worlds.default.warzone"))) {
                    return C.chat(Objects.requireNonNull(Locale.get().getString("faction.wilderness")));
                }
                return C.chat(Objects.requireNonNull(Locale.get().getString("faction.warzone")));
            }
            case NETHER -> {
                if (!Faction.isInWilderness(clone, Main.getInstance().getConfig().getInt("factions.sizes.worlds.nether.warzone"))) {
                    return C.chat(Objects.requireNonNull(Locale.get().getString("faction.wilderness")));
                }
                return C.chat(Objects.requireNonNull(Locale.get().getString("faction.warzone")));
            }
            case THE_END -> { return Locale.get().getString("faction.wilderness"); }
            default -> {
                return null;
            }
        }
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

    @Override
    public String toString() {
        return "Faction{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                ", factionType=" + factionType +
                '}';
    }

    public static ArrayList<Faction> getPage(int page) {
        if (page < 1) return new ArrayList<>();
        List<Faction> playerFactions = Main.getInstance().factions.values().stream()
                .filter(f -> f.getType() == FactionType.PLAYER)
                .sorted((f1, f2) -> Integer.compare(
                        f2.getOnlineMembers().size(), f1.getOnlineMembers().size()
                ))
                .collect(Collectors.toList());
        int fromIndex = (page - 1) * 10;
        if (fromIndex >= playerFactions.size()) return new ArrayList<>();
        int toIndex = Math.min(fromIndex + 10, playerFactions.size());
        return new ArrayList<>(playerFactions.subList(fromIndex, toIndex));
    }

    public static int getMaxPages() {
        long count = Main.getInstance().factions.values().stream()
                .filter(f -> f.getType() == FactionType.PLAYER)
                .count();
        return (int) Math.ceil((double) count / 10);
    }

    public static Map<Faction, List<Cuboid>> getNearbyClaims(
            Location playerLoc,
            double radius
    ) {
        Map<Faction, List<Cuboid>> result = new HashMap<>();
        double px = playerLoc.getX();
        double pz = playerLoc.getZ();
        ArrayList<Faction> allFactions = new ArrayList<>(Main.getInstance().factions.values());

        for (Faction faction : allFactions) {
            List<Cuboid> nearby = new ArrayList<>();
            for (Cuboid claim : faction.getClaims()) {
                double closestX = Math.max(claim.getXMin(), Math.min(px, claim.getXMax()));
                double closestZ = Math.max(claim.getZMin(), Math.min(pz, claim.getZMax()));
                double dx = px - closestX;
                double dz = pz - closestZ;
                double distSq = dx * dx + dz * dz;
                if (distSq <= radius * radius) {
                    nearby.add(claim);
                }
            }
            if (!nearby.isEmpty()) {
                result.put(faction, nearby);
            }
        }
        return result;
    }

    public long getTimeTilRegen() {
        return timeTilRegen;
    }

    public void setTimeTilRegen(long timeTilRegen) {
        modified = true;
        this.timeTilRegen = timeTilRegen;
    }

    public boolean isRegening() {
        return isRegening;
    }

    public void setRegening(boolean regening) {
        modified = true;
        isRegening = regening;
    }

    public boolean isOnDTRFreeze() {
        return !isRegening && timeTilRegen != 0;
    }

    public double getMaxDTR() {
        return Math.min(Main.getInstance().getConfig().getDouble("factions.dtr.max"), getMembers().size() * Main.getInstance().getConfig().getDouble("factions.dtr.multiple"));
    }

    public ArrayList<UUID> getAllies() {
        return allies;
    }

    public void setAllies(ArrayList<UUID> allies) {
        modified = true;
        this.allies = allies;
    }

    public ArrayList<UUID> getAllyRequests() {
        return allyRequests;
    }

    public void setAllyRequests(ArrayList<UUID> allyRequests) {
        modified = true;
        this.allyRequests = allyRequests;
    }

    public UUID getFocusedFaction() {
        return focusedFaction;
    }

    public void setFocusedFaction(UUID focusedFaction) {
        this.focusedFaction = focusedFaction;
    }

    public Cuboid getCuboidAtLocation(Location location) {
        Location clone = location.clone();
        clone.setY(0);
        if (claims == null || claims.isEmpty()) return null;
        for (Cuboid cuboid : claims) {
            if (cuboid.isIn(clone)) return cuboid;
        }
        return null;
    }

    public HashMap<String, Tree> getTree() {
        return tree;
    }

    public void setTree(HashMap<String, Tree> tree) {
        modified = true;
        this.tree = tree;
    }
}
