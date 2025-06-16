package me.sub.RelicFactions.Files.Normal;

import me.sub.RelicFactions.Utils.C;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tab {
    public static String header;
    public static String footer;
    public static boolean enabled;
    public static boolean headerEnabled;
    public static boolean footerEnabled;
    private static final List<String> slotLines = new ArrayList<>();

    public static FileConfiguration get() {
        File file = new File(
                Objects.requireNonNull(Bukkit.getServer().getPluginManager()
                        .getPlugin("Relic-Factions")).getDataFolder(),
                "features" + File.separator + "tab.yml"
        );
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void save() {
        try {
            File folder = new File(
                    Objects.requireNonNull(Bukkit.getServer().getPluginManager()
                            .getPlugin("Relic-Factions")).getDataFolder(),
                    "features"
            );
            folder.mkdirs();

            File file = new File(folder, "tab.yml");
            if (!file.exists()) {
                Objects.requireNonNull(Bukkit.getServer().getPluginManager()
                        .getPlugin("Relic-Factions")).saveResource("tab.yml", false);

                File defaultFile = new File(
                        Objects.requireNonNull(Bukkit.getServer().getPluginManager()
                                .getPlugin("Relic-Factions")).getDataFolder(),
                        "tab.yml"
                );
                if (defaultFile.exists()) {
                    defaultFile.renameTo(file);
                }
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.save(file);
        } catch (IOException e) {
            System.out.println("Unable to save file tab.yml");
        }
    }

    public static void load() {
        FileConfiguration config = get();

        enabled = config.getBoolean("tab.enabled");
        headerEnabled = config.getBoolean("tab.header.enabled");
        footerEnabled = config.getBoolean("tab.footer.enabled");

        header = String.join("\n", config.getStringList("tab.header.text"));
        footer = String.join("\n", config.getStringList("tab.footer.text"));

        slotLines.clear();
        config.getStringList("tab.left-rows").forEach(s -> slotLines.add(C.chat(s)));
        config.getStringList("tab.center-rows").forEach(s -> slotLines.add(C.chat(s)));
        config.getStringList("tab.right-rows").forEach(s -> slotLines.add(C.chat(s)));
        config.getStringList("tab.far-right-rows").forEach(s -> slotLines.add(C.chat(s)));
    }

    public static List<String> getSlotLines() {
        if (slotLines.isEmpty()) {
            load();
        }
        return slotLines;
    }
}