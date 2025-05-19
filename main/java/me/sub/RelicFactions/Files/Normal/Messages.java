package me.sub.RelicFactions.Files.Normal;

import me.sub.RelicFactions.Utils.C;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Messages {

    public static FileConfiguration get() {
        File file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder(), "messages.yml");
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void save() {
        try {
            File file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder(), "messages.yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.save(file);
        } catch (IOException e) {
            System.out.println("Unable to save file messages.yml");
        }
    }

    public static void send(Player p, String path) {
        for (String s : Messages.get().getStringList(path)) {
            p.sendMessage(C.chat(s));
        }
    }

    public static void send(Player p, String path, String alias) {
        for (String s : Messages.get().getStringList(path)) {
            p.sendMessage(C.chat(s, alias));
        }
    }
}
