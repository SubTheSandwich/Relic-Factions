package me.sub.RelicFactions.Events.Player.Attack;

import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.PlayerTimer;
import me.sub.RelicFactions.Files.Enums.FactionType;
import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Objects;

public class UserDamageEvents implements Listener {

    /*

    Player on Player Damage

    */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player hit)) return;
        if (!(e.getDamager() instanceof Player damager)) return;
        User hitUser = User.get(hit);
        User damagerUser = User.get(damager);

        Faction hitIn = Faction.getAt(hit.getLocation());
        Faction damagerIn = Faction.getAt(damager.getLocation());

        if ((hitUser.getFaction() != null && damagerUser.getFaction() != null) && (hitUser.getFaction().equals(damagerUser.getFaction()))) {
            if (!Faction.get(hitUser.getFaction()).isFF()) {
                damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.member.damage")).replace("%player%", hit.getName())));
                return;
            }
            generateCombat(hit, damager);
            return;
        }

        if (damagerIn != null && damagerIn.getType().equals(FactionType.SAFEZONE)) {
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.cannot-attack-inside-safezone")).replace("%player%", hit.getName())));
            return;
        }

        if (hitIn != null && hitIn.getType().equals(FactionType.SAFEZONE)) {
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.cannot-attack-here")).replace("%player%", hit.getName())));
            return;
        }

        // TODO: Implement SOTW

        if (damagerUser.hasTimer("pvp") || damagerUser.hasTimer("starting")) {
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.protected"))));
            return;
        }

        if (hitUser.hasTimer("pvp") || hitUser.hasTimer("starting")) {
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.attack-protected"))));
            return;
        }
        generateCombat(hit, damager);
    }

    /*

    Safezone Damage

     */

    @EventHandler
    public void onSafezoneHit(EntityDamageByBlockEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        Faction faction = Faction.getAt(p.getLocation());
        if (faction == null) return;
        if (faction.getType().equals(FactionType.SAFEZONE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (e.getDamager() instanceof Projectile) return;
        if (e.getDamager() instanceof Player) return;
        Faction faction = Faction.getAt(p.getLocation());
        if (faction == null) return;
        if (inSafezone(p)) {
            e.setCancelled(true);
        }
    }

    /*

    Projectile Damage

     */

    @EventHandler
    public void onProjectile(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player hit)) return;
        if (inSafezone(hit)) {
            e.setCancelled(true);
            return;
        }
        if (!(e.getDamager() instanceof Projectile projectile)) return;
        if (!(projectile.getShooter() instanceof Player damager)) return;

        User hitUser = User.get(hit);
        User damagerUser = User.get(damager);

        Faction hitIn = Faction.getAt(hit.getLocation());
        Faction damagerIn = Faction.getAt(damager.getLocation());

        if ((hitUser.getFaction() != null && damagerUser.getFaction() != null) && (hitUser.getFaction().equals(damagerUser.getFaction()))) {
            if (!Faction.get(hitUser.getFaction()).isFF()) {
                damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.member.damage")).replace("%player%", hit.getName())));
                return;
            }
            generateCombat(hit, damager);
            return;
        }

        if (damagerIn != null && damagerIn.getType().equals(FactionType.SAFEZONE)) {
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.cannot-attack-inside-safezone")).replace("%player%", hit.getName())));
            return;
        }

        if (hitIn != null && hitIn.getType().equals(FactionType.SAFEZONE)) {
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.cannot-attack-here")).replace("%player%", hit.getName())));
            return;
        }

        // TODO: Implement SOTW

        if (damagerUser.hasTimer("pvp") || damagerUser.hasTimer("starting")) {
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.protected"))));
            return;
        }

        if (hitUser.hasTimer("pvp") || hitUser.hasTimer("starting")) {
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.attack-protected"))));
            return;
        }
        generateCombat(hit, damager);
    }

    private void generateCombat(Player hit, Player damager) {
        User hitUser = User.get(hit);
        User damagerUser = User.get(damager);

        if (hitUser.hasTimer("combat")) {
            hitUser.getTimer("combat").setDuration(Timer.COMBAT.getDuration());
        } else {
            hit.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.timer.start.combat"))));
            PlayerTimer combat = new PlayerTimer(hit.getUniqueId(), Timer.COMBAT);
            hitUser.addTimer(combat);
        }

        if (damagerUser.hasTimer("combat")) {
            damagerUser.getTimer("combat").setDuration(Timer.COMBAT.getDuration());
        } else {
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.timer.start.combat"))));
            PlayerTimer combat = new PlayerTimer(damager.getUniqueId(), Timer.COMBAT);
            damagerUser.addTimer(combat);
        }
    }

    private boolean inSafezone(Player player) {
        Faction faction = Faction.getAt(player.getLocation());
        if (faction == null) return false;
        return faction.getType().equals(FactionType.SAFEZONE);
    }
}
