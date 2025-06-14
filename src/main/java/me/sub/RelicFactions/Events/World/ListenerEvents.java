package me.sub.RelicFactions.Events.World;

import me.sub.RelicFactions.Main.Main;
import org.bukkit.Chunk;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.Arrays;

public class ListenerEvents implements Listener {

    @EventHandler
    public void onExtract(FurnaceExtractEvent e) {
        e.setExpToDrop((int) (e.getExpToDrop() * Main.getInstance().getConfig().getDouble("listeners.experience.furnace-multiplier")));
    }

    @EventHandler
    public void onFish(PlayerFishEvent e) {
        if (e.getState().equals(PlayerFishEvent.State.CAUGHT_FISH)) {
            e.setExpToDrop((int) (e.getExpToDrop() * Main.getInstance().getConfig().getDouble("listeners.experience.fishing-multiplier")));
        }
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent e) {
        Chunk chunk = e.getLocation().getChunk();

        long count = Arrays.stream(chunk.getEntities())
                .filter(entity -> entity.getType() != EntityType.PLAYER)
                .count();
        if (count >= Main.getInstance().getConfig().getInt("limiters.entity.per-chunk")) {
            e.setCancelled(true);
        }
    }
}
