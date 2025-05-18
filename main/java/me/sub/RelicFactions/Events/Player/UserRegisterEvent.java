package me.sub.RelicFactions.Events.Player;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.UserData;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.ScoreboardHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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
            userData.save();
            user = new User(userData);
            Main.getInstance().users.put(p.getUniqueId(), user);
            Main.getInstance().userNameHolder.put(p.getName(), user);
        }
        if (!p.getName().equals(user.getName())) {
            Main.getInstance().userNameHolder.remove(user.getName());
            user.setName(p.getName());
            Main.getInstance().userNameHolder.put(user.getName(), user);
        }
    }
}
