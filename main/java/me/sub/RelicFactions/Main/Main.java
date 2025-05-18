package me.sub.RelicFactions.Main;

import me.sub.RelicFactions.Commands.User.FactionCommand;
import me.sub.RelicFactions.Events.Player.Chat.FormatChatEvent;
import me.sub.RelicFactions.Events.Player.UserRegisterEvent;
import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.FactionData;
import me.sub.RelicFactions.Files.Data.UserData;
import me.sub.RelicFactions.Utils.Maps;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main extends JavaPlugin {

    public HashMap<UUID, User> users = new HashMap<>();
    public HashMap<String, User> userNameHolder = new HashMap<>();
    public HashMap<UUID, Faction> factions = new HashMap<>();
    public HashMap<String, Faction> factionNameHolder = new HashMap<>();

    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    public Main() {
        instance = this;
    }

    @Override
    public void onEnable() {
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
        // User
        getCommand("faction").setExecutor(new FactionCommand()); getCommand("faction").setTabCompleter(new FactionCommand());

    }

    private void events() {
        PluginManager pm = getServer().getPluginManager();

        // User
        pm.registerEvents(new UserRegisterEvent(), this);
        pm.registerEvents(new FormatChatEvent(), this);
    }

    private void files() {
        saveResource("config.yml", false);
        saveResource("locale.yml", false);
        saveResource("messages.yml", false);
    }

    private void loadUsers() {
        if (UserData.getAll() != null) {
            for (File f : UserData.getAll()) {
                YamlConfiguration file = YamlConfiguration.loadConfiguration(f);
                UUID uuid = UUID.fromString(file.getString("uuid"));
                User user = new User(new UserData(uuid));
                users.put(uuid, user);
                userNameHolder.put(user.getName(), user);
            }
        }
    }

    private void loadFactions() {
        if (FactionData.getAll() != null) {
            for (File f : FactionData.getAll()) {
                YamlConfiguration file = YamlConfiguration.loadConfiguration(f);
                UUID uuid = UUID.fromString(file.getString("uuid"));
                Faction faction = new Faction(new FactionData(uuid));
                factions.put(uuid, faction);
                factionNameHolder.put(faction.getName(), faction);
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
            userData.save();
            saved++;
        }
        getLogger().info("Saved " + saved + (saved == 1 ? " user" : " users"));
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
            factionData.get().set("lives", faction.getLives());
            factionData.save();
            saved++;
        }
        getLogger().info("Saved " + saved + (saved == 1 ? " faction" : " factions"));

    }

}
