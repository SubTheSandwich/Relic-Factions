package me.sub.RelicFactions.Files.Normal;

import me.sub.RelicFactions.Utils.C;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Messages {

    private static FileConfiguration config;
    private static File file;

    public static void load() {
        file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Relic-Factions")).getDataFolder(), "messages.yml");
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get() {
        if (config == null) {
            load();
        }
        return config;
    }

    public static void save() {
        try {
            if (config != null && file != null) {
                config.save(file);
            }
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

    public static void send(CommandSender p, String path) {
        for (String s : Messages.get().getStringList(path)) {
            p.sendMessage(C.chat(s));
        }
    }

    public static void send(CommandSender p, String path, String alias) {
        for (String s : Messages.get().getStringList(path)) {
            p.sendMessage(C.chat(s, alias));
        }
    }
}
