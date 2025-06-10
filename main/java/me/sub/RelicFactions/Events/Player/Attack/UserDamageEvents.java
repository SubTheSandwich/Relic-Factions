package me.sub.RelicFactions.Events.Player.Attack;

import me.sub.RelicFactions.Events.Player.Interact.UserInteractAtFactionEvent;
import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.RunningConquest;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Data.PlayerTimer;
import me.sub.RelicFactions.Files.Data.ServerTimer;
import me.sub.RelicFactions.Files.Enums.FactionType;
import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Files.Normal.Locale;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import me.sub.RelicFactions.Utils.Calculate;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;

import java.math.BigDecimal;
import java.util.*;

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

        if (UserInteractAtFactionEvent.isCrowbar(damager.getInventory().getItemInMainHand())) {
            e.setCancelled(true);
            return;
        }

        if (hitUser.getModMode() != null) {
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.mod-mode.in"))));
            return;
        }

        if (damagerUser.getModMode() != null) {
            if (damagerUser.getModMode().isInBypass()) {
                generateCombat(hit, damager);
                return;
            }
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.mod-mode.cant"))));
            return;
        }

        if (hitUser.isFrozen() || hitUser.isPanic()) {
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.freeze.cannot-attack"))));
            return;
        }

        if (damagerUser.isFrozen() || damagerUser.isPanic()) {
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.freeze.cannot"))));
            return;
        }

        if (ServerTimer.has("sotw")) {
            if (!Main.getInstance().sotwEnabled.contains(damager.getUniqueId())) {
                e.setCancelled(true);
                damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.sotw-protected"))));
                return;
            }
            if (!Main.getInstance().sotwEnabled.contains(hit.getUniqueId())) {
                e.setCancelled(true);
                damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.sotw-attack-protected"))));
                return;
            }
        }

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

        Faction hitIn = Faction.getAt(hit.getLocation());
        Faction damagerIn = Faction.getAt(damager.getLocation());

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

        if ((hitUser.getFaction() != null && damagerUser.getFaction() != null) && (Faction.get(hitUser.getFaction()).getAllies().contains(damagerUser.getFaction()))) {

            if (Main.getInstance().getConfig().getBoolean("factions.allies.attacking.prevent")) {
                e.setCancelled(true);
                damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.ally.damage")).replace("%player%", hit.getName())));
                return;
            }
            e.setDamage(e.getDamage() * Main.getInstance().getConfig().getDouble("factions.allies.attacking.damage-multiplier"));
            generateCombat(hit, damager);
            return;
        }

        if ((hitUser.getFaction() != null && damagerUser.getFaction() != null) && (hitUser.getFaction().equals(damagerUser.getFaction()))) {
            if (!Faction.get(hitUser.getFaction()).isFF()) {
                e.setCancelled(true);
                damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.member.damage")).replace("%player%", hit.getName())));
                return;
            }
            generateCombat(hit, damager);
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

    Combat Logger Damage

     */

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Villager villager)) return;

        // Get the UUID from PersistentDataContainer
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "logger_uuid");
        String uuidString = villager.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (uuidString == null) return;

        UUID hit;
        try {
            hit = UUID.fromString(uuidString);
        } catch (IllegalArgumentException ex) {
            return; // Not a valid UUID
        }

        e.getEntity().getWorld().strikeLightningEffect(e.getEntity().getLocation());

        User hitUser = User.get(hit);
        if (hitUser == null) return;

        if (hitUser.hasFaction()) {
            Faction faction = Faction.get(hitUser.getFaction());
            if (Main.getInstance().getConfig().getBoolean("elo.enable")) {
                faction.setPoints(faction.getPoints() - Main.getInstance().getConfig().getInt("elo.points-on-death"));
                if (faction.getPoints() < 0) faction.setPoints(0);
            }
        }

        EntityDamageEvent lastDamage = villager.getLastDamageCause();
        if (lastDamage instanceof EntityDamageByEntityEvent entityDamage) {
            Entity damager = entityDamage.getDamager();

            // Drop items
            if (hitUser.getLastInventoryContents() != null) {
                for (ItemStack itemStack : hitUser.getLastInventoryContents()) {
                    if (itemStack == null) continue;
                    damager.getWorld().dropItemNaturally(villager.getLocation(), itemStack);
                }
            }

            // Broadcast message
            if (damager instanceof Player player) {
                User damagerUser = User.get(player);
                if (damagerUser.hasFaction()) {
                    Faction faction = Faction.get(damagerUser.getFaction());
                    if (Main.getInstance().getConfig().getBoolean("elo.enable")) {
                        faction.setPoints(faction.getPoints() + Main.getInstance().getConfig().getInt("elo.points-on-kill"));
                    }
                }
                damagerUser.setKills(damagerUser.getKills() + 1);
                String msg = Locale.get().getString("deathmessage.logger.player");
                if (msg != null) {
                    msg = msg.replace("%killer%", damagerUser.getName())
                            .replace("%killer-kills%", String.valueOf(damagerUser.getKills()))
                            .replace("%dead%", hitUser.getName())
                            .replace("%dead-kills%", String.valueOf(hitUser.getKills()));
                    Bukkit.broadcast(Component.text(C.chat(msg)));
                }
            } else {
                String msg = Locale.get().getString("deathmessage.logger.unknown");
                if (msg != null) {
                    msg = msg.replace("%dead%", hitUser.getName())
                            .replace("%dead-kills%", String.valueOf(hitUser.getKills()));
                    Bukkit.broadcast(Component.text(C.chat(msg)));
                }
            }
        }
    }

    @EventHandler
    public void onVillager(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Villager villager)) return;
        if (!(e.getDamager() instanceof Player damager)) return;

        if (UserInteractAtFactionEvent.isCrowbar(damager.getInventory().getItemInMainHand())) {
            e.setCancelled(true);
            return;
        }

        // Get the UUID from PersistentDataContainer
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "logger_uuid");
        String uuidString = villager.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (uuidString == null) return;

        UUID hit;
        try {
            hit = UUID.fromString(uuidString);
        } catch (IllegalArgumentException ex) {
            return; // Not a valid UUID
        }

        User hitUser = User.get(hit);
        if (hitUser == null) return;
        User damagerUser = User.get(damager);

        if (hitUser.getModMode() != null) {
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.mod-mode.in"))));
            return;
        }

        if (damagerUser.getModMode() != null) {
            if (damagerUser.getModMode().isInBypass()) {
                generateCombat(damager);
                return;
            }
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.mod-mode.cant"))));
            return;
        }

        if (hitUser.isFrozen() || hitUser.isPanic()) {
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.freeze.cannot-attack"))));
            return;
        }

        if (damagerUser.isFrozen() || damagerUser.isPanic()) {
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.freeze.cannot"))));
            return;
        }

        if (ServerTimer.has("sotw")) {
            if (!Main.getInstance().sotwEnabled.contains(damager.getUniqueId())) {
                e.setCancelled(true);
                damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.sotw-protected"))));
                return;
            }
            if (!Main.getInstance().sotwEnabled.contains(hitUser.getUUID())) {
                e.setCancelled(true);
                damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.sotw-attack-protected"))));
                return;
            }
        }

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

        Faction hitIn = Faction.getAt(villager.getLocation());
        Faction damagerIn = Faction.getAt(damager.getLocation());

        if (damagerIn != null && damagerIn.getType().equals(FactionType.SAFEZONE)) {
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.cannot-attack-inside-safezone")).replace("%player%", hitUser.getName())));
            return;
        }

        if (hitIn != null && hitIn.getType().equals(FactionType.SAFEZONE)) {
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.cannot-attack-here")).replace("%player%", hitUser.getName())));
            return;
        }

        if ((hitUser.getFaction() != null && damagerUser.getFaction() != null) && (Faction.get(hitUser.getFaction()).getAllies().contains(damagerUser.getFaction()))) {

            if (Main.getInstance().getConfig().getBoolean("factions.allies.attacking.prevent")) {
                e.setCancelled(true);
                damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.ally.damage")).replace("%player%", hitUser.getName())));
                return;
            }
            e.setDamage(e.getDamage() * Main.getInstance().getConfig().getDouble("factions.allies.attacking.damage-multiplier"));
            generateCombat(damager);
            return;
        }

        if ((hitUser.getFaction() != null && damagerUser.getFaction() != null) && (hitUser.getFaction().equals(damagerUser.getFaction()))) {
            if (!Faction.get(hitUser.getFaction()).isFF()) {
                e.setCancelled(true);
                damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.member.damage")).replace("%player%", hitUser.getName())));
                return;
            }
            generateCombat(damager);
            return;
        }

        generateCombat(damager);
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

        if (UserInteractAtFactionEvent.isCrowbar(damager.getInventory().getItemInMainHand())) {
            e.setCancelled(true);
            return;
        }

        User hitUser = User.get(hit);
        User damagerUser = User.get(damager);

        if (hitUser.getModMode() != null) {
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.mod-mode.in"))));
            return;
        }

        if (damagerUser.getModMode() != null) {
            if (damagerUser.getModMode().isInBypass()) {
                generateCombat(hit, damager);
                return;
            }
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.mod-mode.cant"))));
            return;
        }

        if (hitUser.isFrozen() || hitUser.isPanic()) {
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.freeze.cannot-attack"))));
            return;
        }

        if (damagerUser.isFrozen() || damagerUser.isPanic()) {
            e.setCancelled(true);
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("commands.freeze.cannot"))));
            return;
        }

        if (ServerTimer.has("sotw")) {
            if (!Main.getInstance().sotwEnabled.contains(damager.getUniqueId())) {
                e.setCancelled(true);
                damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.sotw-protected"))));
                return;
            }
            if (!Main.getInstance().sotwEnabled.contains(hit.getUniqueId())) {
                e.setCancelled(true);
                damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.sotw-attack-protected"))));
                return;
            }
        }

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

        Faction hitIn = Faction.getAt(hit.getLocation());
        Faction damagerIn = Faction.getAt(damager.getLocation());

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

        if ((hitUser.getFaction() != null && damagerUser.getFaction() != null) && (Faction.get(hitUser.getFaction()).getAllies().contains(damagerUser.getFaction()))) {

            if (Main.getInstance().getConfig().getBoolean("factions.allies.attacking.prevent")) {
                e.setCancelled(true);
                damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.ally.damage")).replace("%player%", hit.getName())));
                return;
            }
            e.setDamage(e.getDamage() * Main.getInstance().getConfig().getDouble("factions.allies.attacking.damage-multiplier"));
            generateCombat(hit, damager);
            return;
        }

        if ((hitUser.getFaction() != null && damagerUser.getFaction() != null) && (hitUser.getFaction().equals(damagerUser.getFaction()))) {
            if (!Faction.get(hitUser.getFaction()).isFF()) {
                e.setCancelled(true);
                damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("faction.member.damage")).replace("%player%", hit.getName())));
                return;
            }
            generateCombat(hit, damager);
            return;
        }

        generateCombat(hit, damager);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        User user = User.get(p);
        user.setLastInventoryContents(p.getInventory().getContents());
        user.setDeaths(user.getDeaths() + 1);
        // TODO: EOTW

        e.getEntity().getWorld().strikeLightningEffect(e.getEntity().getLocation());
        user.getTimers().clear();
        if (Main.getInstance().getConfig().getBoolean("features.deathban")) {
            int time = User.getDeathbanTime(p);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, time);
            long deathban = calendar.getTimeInMillis();
            user.setDeathbannedTill(deathban);
            user.setDeathBanned(true);
            p.kick(Component.text(C.chat(Objects.requireNonNull(Locale.get().getString("events.deathban.kick")).replace("%time%", Timer.getMessageFormat(deathban - System.currentTimeMillis())))));
        }
        if (user.hasFaction()) {
            Faction faction = Faction.get(user.getFaction());
            if (Main.getInstance().getConfig().getBoolean("elo.enable")) {
                faction.setPoints(faction.getPoints() - Main.getInstance().getConfig().getInt("elo.points-on-death"));
                if (faction.getPoints() < 0) faction.setPoints(0);
            }

            faction.setDTR(BigDecimal.valueOf(Calculate.round(Math.max(-0.99, faction.getDTR().doubleValue() - Main.getInstance().getConfig().getDouble("factions.dtr.death")), 2)));
            faction.setRegening(false);
            Calendar regen = Calendar.getInstance();
            regen.add(Calendar.MINUTE, Main.getInstance().getConfig().getInt("factions.dtr.regen.start-delay"));
            faction.setTimeTilRegen(regen.getTimeInMillis());
        }

        Player killer = p.getKiller();
        String deathMessage;
        if (killer != null) {
            User kill = User.get(killer);
            kill.setKills(kill.getKills() + 1);
            if (kill.hasFaction()) {
                Faction faction = Faction.get(kill.getFaction());
                if (Main.getInstance().getConfig().getBoolean("elo.enable")) {
                    faction.setPoints(faction.getPoints() + Main.getInstance().getConfig().getInt("elo.points-on-kill"));
                }
            }
            if (killer.getInventory().getItemInMainHand().getType() == Material.AIR) {
                deathMessage = Locale.get().getString("deathmessage.entity-attack.player-noitem");
            } else {
                deathMessage = Objects.requireNonNull(Locale.get().getString("deathmessage.entity-attack.player"))
                        .replace("%item%", killer.getInventory().getItemInMainHand().hasItemMeta() ?
                                C.serialize(Objects.requireNonNull(killer.getInventory().getItemInMainHand().getItemMeta()).displayName()) :
                                killer.getInventory().getItemInMainHand().getType().name());
            }
            deathMessage = fillInDead(p, deathMessage, killer);
        } else {
            EntityDamageEvent lastDamage = p.getLastDamageCause();
            if (lastDamage instanceof EntityDamageByEntityEvent) {
                Entity damager = ((EntityDamageByEntityEvent) lastDamage).getDamager();
                if (damager instanceof Projectile projectile) {
                    ProjectileSource shooter = projectile.getShooter();
                    String projectileName = projectile.getType().name().toLowerCase().replace("_", " ");
                    if (shooter instanceof Player shooterPlayer) {
                        User kill = User.get(shooterPlayer);
                        kill.setKills(kill.getKills() + 1);
                        ItemStack held = shooterPlayer.getInventory().getItemInMainHand();
                        if (held.getType() == Material.AIR) {
                            deathMessage = Locale.get().getString("deathmessage.projectile.player-noitem");
                        } else {
                            deathMessage = Objects.requireNonNull(Locale.get().getString("deathmessage.projectile.player"))
                                    .replace("%distance%", (int) p.getLocation().distance(shooterPlayer.getLocation()) + "")
                                    .replace("%item%", held.hasItemMeta() && Objects.requireNonNull(held.getItemMeta()).hasDisplayName() ?
                                            C.serialize(held.getItemMeta().displayName()) :
                                            held.getType().name());
                        }
                        deathMessage = fillInDead(p, deathMessage, shooterPlayer);
                    } else if (shooter instanceof LivingEntity) {
                        deathMessage = Objects.requireNonNull(Locale.get().getString("deathmessage.projectile.entity"))
                                .replace("%entity%", ((LivingEntity) shooter).getName());
                        deathMessage = fillInDead(p, deathMessage);
                    } else {
                        deathMessage = Objects.requireNonNull(Locale.get().getString("deathmessage.projectile.entity"))
                                .replace("%entity%", projectileName);
                        deathMessage = fillInDead(p, deathMessage);
                    }
                } else if (damager instanceof LivingEntity) {
                    deathMessage = Objects.requireNonNull(Locale.get().getString("deathmessage.entity-attack.entity"))
                            .replace("%killer%", damager.getName());
                    deathMessage = fillInDead(p, deathMessage);
                } else {
                    deathMessage = Objects.requireNonNull(Locale.get().getString("deathmessage.entity-attack.entity"))
                            .replace("%killer%", damager.getType().name().toLowerCase().replace("_", " "));
                    deathMessage = fillInDead(p, deathMessage);
                }
            } else if (lastDamage != null) {
                // Environmental and other causes
                deathMessage = Locale.get().getString("deathmessage." + lastDamage.getCause().name().toLowerCase());
                deathMessage = fillInDead(p, deathMessage);
            } else {
                deathMessage = Locale.get().getString("deathmessage.custom");
                deathMessage = fillInDead(p, deathMessage);
            }
        }
        e.deathMessage(Component.text(C.chat(deathMessage)));


        if (Main.getInstance().getRunningConquest() != null) {
            if (!user.hasFaction()) return;
            Faction faction = Faction.get(user.getFaction());
            if (faction == null) return;
            RunningConquest runningConquest = Main.getInstance().getRunningConquest();
            if (!runningConquest.getPoints().isEmpty() || !runningConquest.getPoints().containsKey(faction.getUUID())) return;
            runningConquest.getPoints().put(faction.getUUID(), runningConquest.getPoints().get(faction.getUUID()) - Main.getInstance().getConfig().getInt("conquest.points-lost-per-death"));
            if (runningConquest.getPoints().get(faction.getUUID()) < 0) runningConquest.getPoints().put(faction.getUUID(), 0);
        }

    }

    private String fillInDead(Player dead, String deathMessage, Player killer) {
        User user = User.get(dead);
        User kill = User.get(killer);
        deathMessage = deathMessage.replace("%dead%", user.getName());
        deathMessage = deathMessage.replace("%dead-kills%", user.getKills() + "");
        deathMessage = deathMessage.replace("%killer%", kill.getName());
        deathMessage = deathMessage.replace("%killer-kills%", kill.getKills() + "");
        return deathMessage;
    }

    private String fillInDead(Player dead, String deathMessage) {
        User user = User.get(dead);
        deathMessage = deathMessage.replace("%dead%", user.getName());
        deathMessage = deathMessage.replace("%dead-kills%", user.getKills() + "");
        return deathMessage;
    }

    private void generateCombat(Player hit, Player damager) {
        User hitUser = User.get(hit);
        User damagerUser = User.get(damager);

        if (hitUser.hasTimer("combat")) {
            hitUser.getTimer("combat").setDuration(Timer.COMBAT.getDuration());
        } else {
            hit.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.timer.player.start.combat"))));
            PlayerTimer combat = new PlayerTimer(hit.getUniqueId(), Timer.COMBAT);
            hitUser.addTimer(combat);
        }

        if (damagerUser.hasTimer("combat")) {
            damagerUser.getTimer("combat").setDuration(Timer.COMBAT.getDuration());
        } else {
            damager.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.timer.player.start.combat"))));
            PlayerTimer combat = new PlayerTimer(damager.getUniqueId(), Timer.COMBAT);
            damagerUser.addTimer(combat);
        }
    }

    private void generateCombat(Player hit) {
        User hitUser = User.get(hit);

        if (hitUser.hasTimer("combat")) {
            hitUser.getTimer("combat").setDuration(Timer.COMBAT.getDuration());
        } else {
            hit.sendMessage(C.chat(Objects.requireNonNull(Locale.get().getString("events.timer.player.start.combat"))));
            PlayerTimer combat = new PlayerTimer(hit.getUniqueId(), Timer.COMBAT);
            hitUser.addTimer(combat);
        }
    }

    private boolean inSafezone(Player player) {
        Faction faction = Faction.getAt(player.getLocation());
        if (faction == null) return false;
        return faction.getType().equals(FactionType.SAFEZONE);
    }
}
