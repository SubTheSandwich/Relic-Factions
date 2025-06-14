package me.sub.RelicFactions.Main;

import me.sub.RelicFactions.Commands.Admin.*;
import me.sub.RelicFactions.Commands.Staff.*;
import me.sub.RelicFactions.Commands.User.*;
import me.sub.RelicFactions.Events.Player.Armor.HCFClassEvent;
import me.sub.RelicFactions.Events.Player.Attack.UserDamageEvents;
import me.sub.RelicFactions.Events.Player.Chat.FormatChatEvent;
import me.sub.RelicFactions.Events.Player.Commands.BlockCommandEvent;
import me.sub.RelicFactions.Events.Player.Interact.*;
import me.sub.RelicFactions.Events.Player.Limiters.EnchantLimitEvents;
import me.sub.RelicFactions.Events.Player.Movement.FreezeMovementEvent;
import me.sub.RelicFactions.Events.Player.Movement.UserMoveEvent;
import me.sub.RelicFactions.Events.Player.Server.UserDisconnectEvent;
import me.sub.RelicFactions.Events.Player.Server.UserRegisterEvent;
import me.sub.RelicFactions.Events.World.ListenerEvents;
import me.sub.RelicFactions.Files.Classes.*;
import me.sub.RelicFactions.Files.Data.*;
import me.sub.RelicFactions.Files.Enums.FactionType;
import me.sub.RelicFactions.Files.Normal.*;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Econ;
import me.sub.RelicFactions.Utils.Fastboard.FastBoard;
import me.sub.RelicFactions.Utils.Maps;
import me.sub.RelicFactions.Utils.Permission;
import me.sub.RelicFactions.Utils.Tab.TabManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Main extends JavaPlugin {

    // TODO: Ability Items (Switcher Ball, etc.)

    // TODO: Tab List
    /*

    Brewing Potion Limiting is very complex, may come back and tackle another time
    Same with Holograms

    Will revisit reclaims also

     */

    private static Economy econ = null;
    private final Logger logger = getLogger();
    private TabManager manager;

    public HashMap<UUID, User> users = new HashMap<>();
    public HashMap<String, User> userNameHolder = new HashMap<>();
    public HashMap<UUID, Faction> factions = new HashMap<>();
    public HashMap<String, Faction> factionNameHolder = new HashMap<>();
    public HashMap<UUID, KOTH> koths = new HashMap<>();
    public HashMap<String, KOTH> kothNameHolder = new HashMap<>();
    public HashMap<UUID, Mountain> mountains = new HashMap<>();
    public HashMap<String, Mountain> mountainNameHolder = new HashMap<>();
    public HashMap<UUID, Conquest> conquests = new HashMap<>();
    public HashMap<String, Conquest> conquestNameHolder = new HashMap<>();

    public HashMap<UUID, RunningKOTH> runningKOTHS = new HashMap<>();
    private RunningConquest runningConquest = null;
    private boolean eotw;

    public final Map<UUID, Float> arrowForceMap = new HashMap<>();

    private final Chat chat;

    public HashMap<UUID, FastBoard> boards = new HashMap<>();

    public final HashMap<String, ServerTimer> serverTimers = new HashMap<>();
    public Set<UUID> sotwEnabled = new HashSet<>();

    private boolean isServerFrozen;
    private String keyAllCommand;


    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    public Main() {
        chat = new Chat();
        instance = this;
    }

    public Chat getChat() {
        return chat;
    }

    public RunningConquest getRunningConquest() {
        return runningConquest;
    }

    public void setRunningConquest(RunningConquest runningConquest) {
        this.runningConquest = runningConquest;
    }

    @Override
    public void onLoad() {
        Bukkit.getServicesManager().register(Economy.class, new Econ(), this, ServicePriority.Highest);
    }

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        files();
        events();
        commands();
        loadFiles();
        new BukkitRunnable() {
            @Override
            public void run() {
                saveUsers();
                saveFactions();
                saveKOTHS();
                saveMountains();
                saveConquests();
            }
        }.runTaskTimer(this, 5 * 60 * 20L, 5 * 60 * 20L);
        handleDTR();
        isServerFrozen = false;
        keyAllCommand = null;
        eotw = false;
        manager = new TabManager();
    }

    @Override
    public void onDisable() {
        saveFiles();
    }

    private void commands() {
        // Admin
        Objects.requireNonNull(getCommand("hcf")).setExecutor(new HCFCommand()); Objects.requireNonNull(getCommand("hcf")).setTabCompleter(new HCFCommand());
        Objects.requireNonNull(getCommand("timer")).setExecutor(new TimerCommand()); Objects.requireNonNull(getCommand("timer")).setTabCompleter(new TimerCommand());
        Objects.requireNonNull(getCommand("economy")).setExecutor(new EconomyCommand()); Objects.requireNonNull(getCommand("economy")).setTabCompleter(new EconomyCommand());
        Objects.requireNonNull(getCommand("lives")).setExecutor(new LivesCommand()); Objects.requireNonNull(getCommand("lives")).setTabCompleter(new LivesCommand());
        Objects.requireNonNull(getCommand("sotw")).setExecutor(new SOTWCommand()); Objects.requireNonNull(getCommand("sotw")).setTabCompleter(new SOTWCommand());
        Objects.requireNonNull(getCommand("gamemode")).setExecutor(new GamemodeCommand()); Objects.requireNonNull(getCommand("gamemode")).setTabCompleter(new GamemodeCommand());
        Objects.requireNonNull(getCommand("teleport")).setExecutor(new TeleportCommand()); Objects.requireNonNull(getCommand("teleport")).setTabCompleter(new TeleportCommand());
        Objects.requireNonNull(getCommand("tphere")).setExecutor(new TPHereCommand()); Objects.requireNonNull(getCommand("tphere")).setTabCompleter(new TPHereCommand());
        Objects.requireNonNull(getCommand("spawner")).setExecutor(new SpawnerCommand()); Objects.requireNonNull(getCommand("spawner")).setTabCompleter(new SpawnerCommand());
        Objects.requireNonNull(getCommand("world")).setExecutor(new WorldCommand()); Objects.requireNonNull(getCommand("world")).setTabCompleter(new WorldCommand());
        Objects.requireNonNull(getCommand("revive")).setExecutor(new ReviveCommand()); Objects.requireNonNull(getCommand("revive")).setTabCompleter(new ReviveCommand());
        Objects.requireNonNull(getCommand("clearinventory")).setExecutor(new ClearInventoryCommand()); Objects.requireNonNull(getCommand("clearinventory")).setTabCompleter(new ClearInventoryCommand());
        Objects.requireNonNull(getCommand("end")).setExecutor(new EndCommand()); Objects.requireNonNull(getCommand("end")).setTabCompleter(new EndCommand());
        Objects.requireNonNull(getCommand("sale")).setExecutor(new SaleCommand()); Objects.requireNonNull(getCommand("sale")).setTabCompleter(new SaleCommand());
        Objects.requireNonNull(getCommand("keysale")).setExecutor(new KeySaleCommand()); Objects.requireNonNull(getCommand("keysale")).setTabCompleter(new KeySaleCommand());
        Objects.requireNonNull(getCommand("keyall")).setExecutor(new KeyAllCommand()); Objects.requireNonNull(getCommand("keyall")).setTabCompleter(new KeyAllCommand());
        Objects.requireNonNull(getCommand("customtimer")).setExecutor(new CustomTimerCommand()); Objects.requireNonNull(getCommand("customtimer")).setTabCompleter(new CustomTimerCommand());
        Objects.requireNonNull(getCommand("crowbar")).setExecutor(new CrowbarCommand()); Objects.requireNonNull(getCommand("crowbar")).setTabCompleter(new CrowbarCommand());
        Objects.requireNonNull(getCommand("koth")).setExecutor(new KOTHCommand()); Objects.requireNonNull(getCommand("koth")).setTabCompleter(new KOTHCommand());
        Objects.requireNonNull(getCommand("mountain")).setExecutor(new MountainCommand()); Objects.requireNonNull(getCommand("mountain")).setTabCompleter(new MountainCommand());
        Objects.requireNonNull(getCommand("conquest")).setExecutor(new ConquestCommand()); Objects.requireNonNull(getCommand("conquest")).setTabCompleter(new ConquestCommand());
        Objects.requireNonNull(getCommand("eotw")).setExecutor(new EOTWCommand()); Objects.requireNonNull(getCommand("eotw")).setTabCompleter(new EOTWCommand());

        // Staff
        Objects.requireNonNull(getCommand("staffchat")).setExecutor(new StaffChatCommand()); Objects.requireNonNull(getCommand("staffchat")).setTabCompleter(new StaffChatCommand());
        Objects.requireNonNull(getCommand("vanish")).setExecutor(new VanishCommand()); Objects.requireNonNull(getCommand("vanish")).setTabCompleter(new VanishCommand());
        Objects.requireNonNull(getCommand("modmode")).setExecutor(new ModModeCommand()); Objects.requireNonNull(getCommand("modmode")).setTabCompleter(new ModModeCommand());
        Objects.requireNonNull(getCommand("invsee")).setExecutor(new InvseeCommand()); Objects.requireNonNull(getCommand("invsee")).setTabCompleter(new InvseeCommand());
        Objects.requireNonNull(getCommand("freeze")).setExecutor(new FreezeCommand()); Objects.requireNonNull(getCommand("freeze")).setTabCompleter(new FreezeCommand());
        Objects.requireNonNull(getCommand("fly")).setExecutor(new FlyCommand()); Objects.requireNonNull(getCommand("fly")).setTabCompleter(new FlyCommand());
        Objects.requireNonNull(getCommand("panic")).setExecutor(new PanicCommand()); Objects.requireNonNull(getCommand("panic")).setTabCompleter(new PanicCommand());
        Objects.requireNonNull(getCommand("heal")).setExecutor(new HealCommand()); Objects.requireNonNull(getCommand("heal")).setTabCompleter(new HealCommand());
        Objects.requireNonNull(getCommand("feed")).setExecutor(new FeedCommand()); Objects.requireNonNull(getCommand("feed")).setTabCompleter(new FeedCommand());
        Objects.requireNonNull(getCommand("notes")).setExecutor(new NotesCommand()); Objects.requireNonNull(getCommand("notes")).setTabCompleter(new NotesCommand());
        Objects.requireNonNull(getCommand("chat")).setExecutor(new ChatCommand()); Objects.requireNonNull(getCommand("chat")).setTabCompleter(new ChatCommand());

        // User
        Objects.requireNonNull(getCommand("faction")).setExecutor(new FactionCommand()); Objects.requireNonNull(getCommand("faction")).setTabCompleter(new FactionCommand());
        Objects.requireNonNull(getCommand("balance")).setExecutor(new BalanceCommand()); Objects.requireNonNull(getCommand("balance")).setTabCompleter(new BalanceCommand());
        Objects.requireNonNull(getCommand("help")).setExecutor(new HelpCommand()); Objects.requireNonNull(getCommand("help")).setTabCompleter(new HelpCommand());
        Objects.requireNonNull(getCommand("filter")).setExecutor(new FilterCommand()); Objects.requireNonNull(getCommand("filter")).setTabCompleter(new FilterCommand());
        Objects.requireNonNull(getCommand("lff")).setExecutor(new LFFCommand()); Objects.requireNonNull(getCommand("lff")).setTabCompleter(new LFFCommand());
        Objects.requireNonNull(getCommand("pvp")).setExecutor(new PvPCommand()); Objects.requireNonNull(getCommand("pvp")).setTabCompleter(new PvPCommand());
        Objects.requireNonNull(getCommand("coords")).setExecutor(new CoordsCommand()); Objects.requireNonNull(getCommand("coords")).setTabCompleter(new CoordsCommand());
        Objects.requireNonNull(getCommand("ping")).setExecutor(new PingCommand()); Objects.requireNonNull(getCommand("ping")).setTabCompleter(new PingCommand());
        Objects.requireNonNull(getCommand("request")).setExecutor(new RequestCommand()); Objects.requireNonNull(getCommand("request")).setTabCompleter(new RequestCommand());
        Objects.requireNonNull(getCommand("report")).setExecutor(new ReportCommand()); Objects.requireNonNull(getCommand("report")).setTabCompleter(new ReportCommand());
        Objects.requireNonNull(getCommand("logout")).setExecutor(new LogoutCommand()); Objects.requireNonNull(getCommand("logout")).setTabCompleter(new LogoutCommand());
        Objects.requireNonNull(getCommand("pay")).setExecutor(new PayCommand()); Objects.requireNonNull(getCommand("pay")).setTabCompleter(new PayCommand());
        Objects.requireNonNull(getCommand("playtime")).setExecutor(new PlaytimeCommand()); Objects.requireNonNull(getCommand("playtime")).setTabCompleter(new PlaytimeCommand());
        Objects.requireNonNull(getCommand("toggleglobalchat")).setExecutor(new ToggleGlobalChatCommand()); Objects.requireNonNull(getCommand("toggleglobalchat")).setTabCompleter(new ToggleGlobalChatCommand());
        Objects.requireNonNull(getCommand("message")).setExecutor(new MessageCommand()); Objects.requireNonNull(getCommand("message")).setTabCompleter(new MessageCommand());
        Objects.requireNonNull(getCommand("reply")).setExecutor(new ReplyCommand()); Objects.requireNonNull(getCommand("reply")).setTabCompleter(new ReplyCommand());
        Objects.requireNonNull(getCommand("settings")).setExecutor(new SettingsCommand()); Objects.requireNonNull(getCommand("settings")).setTabCompleter(new SettingsCommand());
        Objects.requireNonNull(getCommand("profile")).setExecutor(new ProfileCommand()); Objects.requireNonNull(getCommand("profile")).setTabCompleter(new ProfileCommand());
    }


    private void events() {
        PluginManager pm = getServer().getPluginManager();

        // User
        pm.registerEvents(new UserRegisterEvent(), this);
        pm.registerEvents(new FormatChatEvent(), this);
        pm.registerEvents(new UserClaimEvents(), this);
        pm.registerEvents(new UserDisconnectEvent(), this);
        pm.registerEvents(new UserMoveEvent(), this);
        pm.registerEvents(new UserInteractAtFactionEvent(), this);
        pm.registerEvents(new UserFilterEvent(), this);
        pm.registerEvents(new ItemUseEvents(), this);
        pm.registerEvents(new UserDamageEvents(), this);
        pm.registerEvents(new ModModeEvents(), this);
        pm.registerEvents(new FreezeMovementEvent(), this);
        pm.registerEvents(new SettingsInteractEvent(), this);
        pm.registerEvents(new BlockCommandEvent(), this);
        pm.registerEvents(new EnchantLimitEvents(), this);
        pm.registerEvents(new NotesInteractEvent(), this);
        pm.registerEvents(new HCFClassEvent(), this);

        // Server
        pm.registerEvents(new ListenerEvents(), this);
    }

    private void files() {
        saveResource("config.yml", false);
        saveResource("locale.yml", false);
        saveResource("messages.yml", false);
        saveResource("tab.yml", false);
        Tab.load();
        Locale.load();
        ModModeFile.save();
        Inventories.save();
        Locations locations = new Locations();
        locations.save();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    private final Map<UUID, Integer> factionRegenDelay = new HashMap<>();

    private void handleDTR() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Faction faction : factions.values().stream()
                        .filter(f -> f.getType() == FactionType.PLAYER)
                        .collect(Collectors.toSet())) {

                    if (faction.getDTR().doubleValue() <= 0 && !faction.isOnDTRFreeze()) {
                        Calendar regen = Calendar.getInstance();
                        regen.add(Calendar.MINUTE, Main.getInstance().getConfig().getInt("factions.dtr.regen.start-delay"));
                        faction.setTimeTilRegen(regen.getTimeInMillis());
                        continue;
                    }

                    // Handle DTR freeze
                    if (faction.isOnDTRFreeze()) {
                        if (faction.getTimeTilRegen() - System.currentTimeMillis() <= 0) {
                            faction.setTimeTilRegen(0);
                            faction.setRegening(true);
                        }
                        if (faction.getMaxDTR() == faction.getDTR().doubleValue() && faction.getTimeTilRegen() != 0) {
                            faction.setTimeTilRegen(0);
                            factionRegenDelay.remove(faction.getUUID());
                        }
                        continue;
                    }

                    if (faction.getMaxDTR() == faction.getDTR().doubleValue() && faction.getTimeTilRegen() != 0) {
                        faction.setTimeTilRegen(0);
                        factionRegenDelay.remove(faction.getUUID());
                        continue;
                    }

                    // Only regen if flagged
                    if (!faction.isRegening()) continue;

                    // Stop if at max DTR
                    if (faction.getMaxDTR() == faction.getDTR().doubleValue()) {
                        faction.setRegening(false);
                        faction.setTimeTilRegen(0);
                        factionRegenDelay.remove(faction.getUUID());
                        continue;
                    }

                    // Handle regen delay
                    int time = factionRegenDelay.getOrDefault(faction.getUUID(), 0);
                    int delay = Main.getInstance().getConfig().getInt("factions.dtr.regen.delay") * 60;
                    if (time < delay) {
                        factionRegenDelay.put(faction.getUUID(), time + 1);
                        continue;
                    }

                    // Regen DTR
                    double increment = Main.getInstance().getConfig().getDouble("factions.dtr.regen.increment");
                    double newDTR = Math.min(faction.getDTR().doubleValue() + increment, faction.getMaxDTR());
                    faction.setDTR(BigDecimal.valueOf(newDTR));

                    // If at max, stop regening and reset delay
                    if (faction.getDTR().doubleValue() == faction.getMaxDTR()) {
                        faction.setRegening(false);
                        faction.setTimeTilRegen(0);
                        factionRegenDelay.remove(faction.getUUID());
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 20);
    }

    private void loadUsers() {
        if (UserData.getAll() != null) {
            for (File f : UserData.getAll()) {
                YamlConfiguration file = YamlConfiguration.loadConfiguration(f);
                UUID uuid = UUID.fromString(Objects.requireNonNull(file.getString("uuid")));
                User user = new User(new UserData(uuid));
                users.put(uuid, user);
                userNameHolder.put(user.getName().toLowerCase(), user);
            }
        }
    }

    private void loadFactions() {
        if (FactionData.getAll() != null) {
            for (File f : FactionData.getAll()) {
                YamlConfiguration file = YamlConfiguration.loadConfiguration(f);
                UUID uuid = UUID.fromString(Objects.requireNonNull(file.getString("uuid")));
                Faction faction = new Faction(new FactionData(uuid));
                factions.put(uuid, faction);
                factionNameHolder.put(faction.getName().toLowerCase(), faction);
            }
        }
    }

    private void loadKOTHS() {
        if (KOTHData.getAll() != null) {
            for (File f : KOTHData.getAll()) {
                YamlConfiguration file = YamlConfiguration.loadConfiguration(f);
                UUID uuid = UUID.fromString(Objects.requireNonNull(file.getString("uuid")));
                KOTH koth = new KOTH(new KOTHData(uuid));
                koths.put(uuid, koth);
                kothNameHolder.put(koth.getName().toLowerCase(), koth);
            }
        }
    }

    private void loadMountains() {
        if (MountainData.getAll() != null) {
            for (File f : MountainData.getAll()) {
                YamlConfiguration file = YamlConfiguration.loadConfiguration(f);
                UUID uuid = UUID.fromString(Objects.requireNonNull(file.getString("uuid")));
                Mountain mountain = new Mountain(new MountainData(uuid));
                mountains.put(uuid, mountain);
                mountainNameHolder.put(mountain.getName().toLowerCase(), mountain);
            }
        }
    }

    private void loadConquests() {
        if (ConquestData.getAll() != null) {
            for (File f : ConquestData.getAll()) {
                YamlConfiguration file = YamlConfiguration.loadConfiguration(f);
                UUID uuid = UUID.fromString(Objects.requireNonNull(file.getString("uuid")));
                Conquest conquest = new Conquest(new ConquestData(uuid));
                conquests.put(uuid, conquest);
                conquestNameHolder.put(conquest.getName().toLowerCase(), conquest);
            }
        }
    }

    private void saveUsers() {
        int saved = 0;
        if (users.isEmpty()) return;
        for (Map.Entry<UUID, User> entry : users.entrySet()) {
            User user = entry.getValue();
            if (!user.isModified()) continue;
            UserData userData = user.getUserData();
            userData.get().set("name", user.getName());
            userData.get().set("faction", user.getFaction() == null ? null : user.getFaction().toString());
            userData.get().set("deathban.has", user.isDeathBanned());
            userData.get().set("deathban.ends", user.getDeathbannedTill());
            userData.get().set("kills", user.getKills());
            userData.get().set("deaths", user.getDeaths());
            userData.get().set("balance", user.getBalance().doubleValue());
            userData.get().set("lives", user.getLives());
            userData.get().set("timers", Maps.timersToString(user.getTimers()));
            userData.get().set("customTimers", Maps.serializeCustomMap(user.getCustomTimers()));
            userData.get().set("lastInventoryContents", Maps.toBase64(user, user.getLastInventoryContents()));
            userData.get().set("playtime", user.getStoredPlaytime());
            userData.get().set("notes", Note.serializeList(user.getNotes()));
            userData.get().set("settings.global-chat", user.isGlobalChat());
            userData.get().set("settings.messages.enabled", user.isMessages());
            userData.get().set("settings.messages.sounds", user.isMessageSounds());
            userData.get().set("settings.scoreboard", user.isScoreboard());
            userData.get().set("settings.foundDiamonds", user.isFoundDiamonds());
            userData.get().set("settings.mountains", user.isMountains());
            userData.get().set("ores.coal", user.getCoalMined());
            userData.get().set("ores.iron", user.getIronMined());
            userData.get().set("ores.copper", user.getCopperMined());
            userData.get().set("ores.gold", user.getGoldMined());
            userData.get().set("ores.redstone", user.getRedstoneMined());
            userData.get().set("ores.lapis", user.getLapisMined());
            userData.get().set("ores.diamond", user.getDiamondMined());
            userData.get().set("ores.emerald", user.getEmeraldMined());
            userData.get().set("ores.quartz", user.getQuartzMined());
            userData.get().set("ores.debris", user.getDebrisMined());
            userData.save();
            user.setModified(false);
            saved++;
        }
        if (saved > 0) logger.info("Saved " + saved + (saved == 1 ? " user" : " users"));
    }

    private void saveFactions() {
        int saved = 0;
        if (factions.isEmpty()) return;
        for (Map.Entry<UUID, Faction> entry : factions.entrySet()) {
            Faction faction = entry.getValue();
            if (!faction.isModified()) continue;
            FactionData factionData = faction.getFactionData();
            if (faction.getType().equals(FactionType.PLAYER)) {
                factionData.get().set("members", Maps.serialize(faction.getMembers()));
                factionData.get().set("open", faction.isOpen());
                factionData.get().set("leader", faction.getLeader() == null ? null : faction.getLeader().toString());
                factionData.get().set("points", faction.getPoints());
                factionData.get().set("koth-captures", faction.getKothCaptures());
                factionData.get().set("balance", faction.getBalance().doubleValue());
                factionData.get().set("dtr", faction.getDTR().doubleValue());
                factionData.get().set("lives", faction.getLives());
                factionData.get().set("invites", Maps.uuidListToString(faction.getInvites()));
                factionData.get().set("timeTilRegen", faction.getTimeTilRegen());
                factionData.get().set("regening", faction.isRegening());
                factionData.get().set("allies", Maps.uuidListToString(faction.getAllies()));
                factionData.get().set("allyRequests", Maps.uuidListToString(faction.getAllyRequests()));
            }
            factionData.get().set("name", faction.getName());
            factionData.get().set("color", faction.getColor() == null ? null : faction.getColor().name());
            factionData.get().set("type", faction.getType().toString());
            factionData.get().set("claims", Maps.cuboidListToString(faction.getClaims()));
            factionData.get().set("deathban", faction.isDeathban());
            factionData.get().set("home", faction.getHome() == null ? null : faction.getHome());
            factionData.save();
            faction.setModified(false);
            saved++;
        }
        if (saved > 0)  logger.info("Saved " + saved + (saved == 1 ? " faction" : " factions"));

    }

    private void saveKOTHS() {
        int saved = 0;
        if (koths.isEmpty()) return;
        for (Map.Entry<UUID, KOTH> entry : koths.entrySet()) {
            KOTH koth = entry.getValue();
            if (!koth.isModified()) continue;
            KOTHData kothData = koth.getKothData();
            kothData.get().set("name", koth.getName());
            kothData.get().set("positionOne", koth.getPositionOne());
            kothData.get().set("positionTwo", koth.getPositionTwo());
            kothData.get().set("pearlable", koth.isPearlable());
            kothData.get().set("special", koth.isSpecial());
            kothData.get().set("time", koth.getTime());
            kothData.get().set("faction", koth.getFaction() == null ? null : koth.getFaction().toString());
            kothData.save();
            koth.setModified(false);
            saved++;
        }
        if (saved > 0)  logger.info("Saved " + saved + (saved == 1 ? " koth" : " koths"));
    }

    private void saveMountains() {
        int saved = 0;
        if (mountains.isEmpty()) return;
        for (Map.Entry<UUID, Mountain> entry : mountains.entrySet()) {
            Mountain mountain = entry.getValue();
            if (!mountain.isModified()) continue;
            MountainData mountainData = mountain.getMountainData();
            mountainData.get().set("name", mountainData.getName());
            mountainData.get().set("positionOne", mountain.getPositionOne());
            mountainData.get().set("positionTwo", mountain.getPositionTwo());
            mountainData.get().set("type", mountain.getType() == null ? null : mountain.getType().name());
            mountainData.get().set("time", mountain.getDefaultTime());
            mountainData.save();
            mountain.setModified(false);
            saved++;
        }
        if (saved > 0) logger.info("Saved " + saved + (saved == 1 ? " mountain" : " mountains"));
    }

    private void saveConquests() {
        int saved = 0;
        if (conquests.isEmpty()) return;
        for (Map.Entry<UUID, Conquest> entry : conquests.entrySet()) {
            Conquest conquest = entry.getValue();
            if (!conquest.isModified()) continue;
            ConquestData conquestData = conquest.getConquestData();
            conquestData.get().set("name", conquest.getName());
            conquestData.get().set("red",  conquest.getRed().serialize());
            conquestData.get().set("green",  conquest.getGreen().serialize());
            conquestData.get().set("blue",  conquest.getBlue().serialize());
            conquestData.get().set("yellow",  conquest.getYellow().serialize());
            conquestData.save();
            conquest.setModified(false);
            saved++;
        }
        if (saved > 0) logger.info("Saved " + saved + (saved == 1 ? " conquest" : " conquests"));
    }

    public static Economy getEconomy() {
        return econ;
    }

    public void saveFiles() {
        saveUsers();
        saveFactions();
        saveKOTHS();
        saveMountains();
        saveConquests();
    }

    public void loadFiles() {
        loadUsers();
        loadFactions();
        loadKOTHS();
        loadMountains();
        loadConquests();
    }

    public boolean isServerFrozen() {
        return isServerFrozen;
    }

    public void setServerFrozen(boolean serverFrozen) {
        isServerFrozen = serverFrozen;
    }

    public static List<Player> getOnlineStaff() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> Permission.has(p, "staff") || Permission.has(p, "admin"))
                .collect(Collectors.toList());
    }

    public String getKeyAllCommand() {
        return keyAllCommand;
    }

    public void setKeyAllCommand(String keyAllCommand) {
        this.keyAllCommand = keyAllCommand;
    }

    public void sendGlobalMessage(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(C.chat(message));
        }
    }

    public boolean isValidMaterial(Material material) {
        String name = material.name().toUpperCase();

        if (material.isAir()) return false;
        if (!material.isSolid()) return false;
        if (name.contains("GLASS")) return false;
        if (material.isRecord()) return false;
        if (material.isEdible()) return false;
        if (Tag.TRAPDOORS.isTagged(material)) return false;
        if (Tag.DOORS.isTagged(material)) return false;
        if (Tag.STAIRS.isTagged(material)) return false;
        if (Tag.RAILS.isTagged(material)) return false;
        if (Tag.SLABS.isTagged(material)) return false;
        if (Tag.PRESSURE_PLATES.isTagged(material)) return false;
        if (Tag.BUTTONS.isTagged(material)) return false;
        if (Tag.ALL_SIGNS.isTagged(material)) return false;
        if (Tag.CAMPFIRES.isTagged(material)) return false;
        if (Tag.CROPS.isTagged(material)) return false;
        if (Tag.FENCE_GATES.isTagged(material)) return false;
        if (Tag.FENCES.isTagged(material)) return false;
        if (Tag.FLOWERS.isTagged(material)) return false;
        if (Tag.WOOL_CARPETS.isTagged(material)) return false;
        if (Tag.BEDS.isTagged(material)) return false;
        if (Tag.CANDLE_CAKES.isTagged(material)) return false;
        if (Tag.CORAL_PLANTS.isTagged(material)) return false;
        if (Tag.WALL_CORALS.isTagged(material)) return false;
        if (Tag.BANNERS.isTagged(material)) return false;
        if (Tag.WALLS.isTagged(material)) return false;
        if (material.equals(Material.FARMLAND)) return false;
        if (material.equals(Material.CAKE)) return false;
        if (material.equals(Material.DAYLIGHT_DETECTOR)) return false;
        if (material.equals(Material.POINTED_DRIPSTONE)) return false;
        if (material.equals(Material.SCULK_VEIN)) return false;
        if (material.equals(Material.SCULK_CATALYST)) return false;
        if (material.equals(Material.SCULK_SHRIEKER)) return false;
        if (material.equals(Material.SCULK_SENSOR)) return false;
        if (material.equals(Material.CALIBRATED_SCULK_SENSOR)) return false;
        if (material.equals(Material.BREWING_STAND)) return false;
        if (material.equals(Material.DECORATED_POT)) return false;
        if (Tag.CAULDRONS.isTagged(material)) return false;
        if (material.equals(Material.LANTERN)) return false;
        if (material.equals(Material.BELL)) return false;
        if (material.equals(Material.LECTERN)) return false;
        if (name.contains("PISTON")) return false;
        if (name.contains("CHEST")) return false;
        if (material.equals(Material.ENCHANTING_TABLE)) return false;
        if (Tag.ANVIL.isTagged(material)) return false;
        if (Tag.LEAVES.isTagged(material)) return false;
        if (material.equals(Material.BARRIER)) return false;
        if (material.equals(Material.HOPPER)) return false;
        if (name.contains("AMETHYST_BUD") || name.contains("AMETHYST_CLUSTER")) return false;
        if (material.equals(Material.DIRT_PATH)) return false;
        if (name.contains("INFESTED")) return false;
        if (material.equals(Material.SNIFFER_EGG)) return false;
        if (Tag.SHULKER_BOXES.isTagged(material)) return false;
        if (material.equals(Material.BAMBOO)) return false;
        return !name.contains("CORAL") || name.endsWith("_CORAL_BLOCK");
    }

    public boolean isEOTW() {
        return eotw;
    }

    public void setEOTW(boolean eotw) {
        this.eotw = eotw;
    }

    public TabManager getTabManager() {
        return manager;
    }
}
