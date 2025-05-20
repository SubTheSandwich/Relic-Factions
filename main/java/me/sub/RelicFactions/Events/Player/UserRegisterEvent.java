package me.sub.RelicFactions.Events.Player;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.PlayerTimer;
import me.sub.RelicFactions.Files.Data.UserData;
import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Fastboard.FastBoard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Objects;

public class UserRegisterEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        User user = User.get(p);
        if (user == null) {
            UserData userData = new UserData(p.getUniqueId());
            userData.setup();
            userData.get().set("uuid", p.getUniqueId().toString());
            userData.get().set("name", p.getName());
            userData.get().set("balance", Main.getInstance().getConfig().getDouble("economy.default-balance"));
            userData.save();
            user = new User(userData);
            Main.getInstance().users.put(p.getUniqueId(), user);
            Main.getInstance().userNameHolder.put(p.getName().toLowerCase(), user);
        }
        if (!p.getName().equals(user.getName())) {
            Main.getInstance().userNameHolder.remove(user.getName().toLowerCase());
            user.setName(p.getName());
            Main.getInstance().userNameHolder.put(user.getName().toLowerCase(), user);
        }
        User finalUser = user;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!Main.getInstance().getConfig().getBoolean("features.scoreboard.enabled")) return;
                ArrayList<String> lines = new ArrayList<>();
                FastBoard board = new FastBoard(p);
                if (!p.isOnline()) {
                    cancel();
                    board.delete();
                    return;
                }
                for (String s : Main.getInstance().getConfig().getStringList("scoreboard.lines")) {
                    if (s.contains("<display=%has_player")) {
                        String[] split = s.split("<display=%has_player_");
                        String timer = split[1];
                        timer = timer.replace("_timer%", "");
                        if (!finalUser.hasTimer(timer)) continue;
                        PlayerTimer time = finalUser.getTimer(timer);
                        s = s.replace("<display=%has_player_" + timer + "_timer%", "");
                        s = s.replace("%player_" + timer + "_timer%", Timer.format(time.getDuration()));
                    }

                    lines.add(C.chat(s));
                }

                if (lines.size() <= Main.getInstance().getConfig().getInt("features.scoreboard.line-limit")) {
                    board.delete();
                } else {
                    board.updateTitle(C.chat(Objects.requireNonNull(Main.getInstance().getConfig().getString("scoreboard.title"))));
                    board.updateLines(lines);
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }
}
