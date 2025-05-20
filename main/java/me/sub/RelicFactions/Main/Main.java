package me.sub.RelicFactions.Main;

import me.sub.RelicFactions.Admin.HCFCommand;
import me.sub.RelicFactions.Admin.TimerCommand;
import me.sub.RelicFactions.Commands.User.BalanceCommand;
import me.sub.RelicFactions.Commands.User.FactionCommand;
import me.sub.RelicFactions.Events.Player.Chat.FormatChatEvent;
import me.sub.RelicFactions.Events.Player.Interact.PlayerClaimEvents;
import me.sub.RelicFactions.Events.Player.Interact.UserInteractAtFactionEvent;
import me.sub.RelicFactions.Events.Player.Movement.UserMoveEvent;
import me.sub.RelicFactions.Events.Player.Server.UserDisconnectEvent;
import me.sub.RelicFactions.Events.Player.Server.UserRegisterEvent;
import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.FactionData;
import me.sub.RelicFactions.Files.Data.UserData;
import me.sub.RelicFactions.Utils.Econ;
import me.sub.RelicFactions.Utils.Fastboard.FastBoard;
import me.sub.RelicFactions.Utils.Maps;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public class Main extends JavaPlugin {

    private static Economy econ = null;
    private final Logger logger = getLogger();

    public HashMap<UUID, User> users = new HashMap<>();
    public HashMap<String, User> userNameHolder = new HashMap<>();
    public HashMap<UUID, Faction> factions = new HashMap<>();
    public HashMap<String, Faction> factionNameHolder = new HashMap<>();
    public HashMap<UUID, FastBoard> boards = new HashMap<>();

    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    public Main() {
        instance = this;
    }

    @Override
    public void onLoad() {
        Bukkit.getServicesManager().register(Economy.class, new Econ(), this, ServicePriority.Highest);
    }

    @Override
    public void onEnable() {
        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        files();
        events();
        commands();
        loadUsers();
        loadFactions();
        new BukkitRunnable() {
            @Override
            public void run() {
                saveUsers();
                saveFactions();
            }
        }.runTaskTimer(this, 5 * 60 * 20L, 5 * 60 * 20L);
    }

    @Override
    public void onDisable() {
        saveUsers();
        saveFactions();
    }

    private void commands() {
        // Admin
        Objects.requireNonNull(getCommand("hcf")).setExecutor(new HCFCommand()); Objects.requireNonNull(getCommand("hcf")).setTabCompleter(new HCFCommand());
        Objects.requireNonNull(getCommand("timer")).setExecutor(new TimerCommand()); Objects.requireNonNull(getCommand("timer")).setTabCompleter(new TimerCommand());

        // User
        Objects.requireNonNull(getCommand("faction")).setExecutor(new FactionCommand()); Objects.requireNonNull(getCommand("faction")).setTabCompleter(new FactionCommand());
        Objects.requireNonNull(getCommand("balance")).setExecutor(new BalanceCommand()); Objects.requireNonNull(getCommand("balance")).setTabCompleter(new BalanceCommand());
    }

    private void events() {
        PluginManager pm = getServer().getPluginManager();

        // User
        pm.registerEvents(new UserRegisterEvent(), this);
        pm.registerEvents(new FormatChatEvent(), this);
        pm.registerEvents(new PlayerClaimEvents(), this);
        pm.registerEvents(new UserDisconnectEvent(), this);
        pm.registerEvents(new UserMoveEvent(), this);
        pm.registerEvents(new UserInteractAtFactionEvent(), this);
    }

    private void files() {
        saveResource("config.yml", false);
        saveResource("locale.yml", false);
        saveResource("messages.yml", false);
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
        return econ != null;
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

    private void saveUsers() {
        int saved = 0;
        if (users.isEmpty()) return;
        for (Map.Entry<UUID, User> entry : users.entrySet()) {
            User user = entry.getValue();
            if (!user.isModified()) continue;
            UserData userData = user.getUserData();
            userData.get().set("name", user.getName());
            userData.get().set("faction", user.getFaction().toString());
            userData.get().set("deathbanned", user.isDeathBanned());
            userData.get().set("kills", user.getKills());
            userData.get().set("deaths", user.getDeaths());
            userData.get().set("balance", user.getBalance().doubleValue());
            userData.get().set("timers", Maps.timersToString(user.getTimers()));
            userData.save();
            user.setModified(false);
            saved++;
        }
        logger.info("Saved " + saved + (saved == 1 ? " user" : " users"));
    }

    private void saveFactions() {
        int saved = 0;
        if (factions.isEmpty()) return;
        for (Map.Entry<UUID, Faction> entry : factions.entrySet()) {
            Faction faction = entry.getValue();
            if (!faction.isModified()) continue;
            FactionData factionData = faction.getFactionData();
            factionData.get().set("name", faction.getName());
            factionData.get().set("members", Maps.serialize(faction.getMembers()));
            factionData.get().set("open", faction.isOpen());
            factionData.get().set("color", faction.getColor() == null ? null : faction.getColor().name());
            factionData.get().set("leader", faction.getLeader() == null ? null : faction.getLeader().toString());
            factionData.get().set("type", faction.getType().toString());
            factionData.get().set("points", faction.getPoints());
            factionData.get().set("koth-captures", faction.getKothCaptures());
            factionData.get().set("balance", faction.getBalance().doubleValue());
            factionData.get().set("dtr", faction.getDTR().doubleValue());
            factionData.get().set("lives", faction.getLives());
            factionData.get().set("invites", Maps.uuidListToString(faction.getInvites()));
            factionData.get().set("claims", Maps.cuboidListToString(faction.getClaims()));
            factionData.get().set("deathban", faction.isDeathban());
            factionData.save();
            faction.setModified(false);
            saved++;
        }
        logger.info("Saved " + saved + (saved == 1 ? " faction" : " factions"));

    }

    public static Economy getEconomy() {
        return econ;
    }

    public void saveFiles() {
        saveUsers();
        saveFactions();
    }
}
