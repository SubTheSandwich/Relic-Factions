package me.sub.RelicFactions.Files.Data;

import me.sub.RelicFactions.Files.Enums.Tree;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

public class PotionTree extends Tree {
    private final PotionEffect effect;

    public PotionTree(boolean unlocked, String name, PotionEffect effect, int points, String group, int level) {
        super(unlocked, name, points, group, level);
        this.effect = effect;
    }

    public PotionEffect getEffect() {
        return effect;
    }

    @Override
    public String toString() {
        return "POTION:" + isUnlocked() + ":" +
                effect.getType().getKey().getKey() + ":" +
                effect.getAmplifier() + ":" +
                effect.getDuration();
    }

    @Override
    public String serialize() {
        return String.join(";",
                Boolean.toString(isUnlocked()),
                getName(),
                effect.getType().getKey().getKey(),
                Integer.toString(effect.getAmplifier()),
                Integer.toString(effect.getDuration()),
                Integer.toString(getPoints()),
                getGroup(),
                Integer.toString(getLevel())
        );
    }

    public static PotionTree deserialize(String data) {
        String[] parts = data.split(";");
        boolean unlocked = Boolean.parseBoolean(parts[0]);
        String name = parts[1];
        String effectTypeKey = parts[2];
        int amplifier = Integer.parseInt(parts[3]);
        int duration = Integer.parseInt(parts[4]);
        int points = Integer.parseInt(parts[5]);
        String group = parts[6];
        int level = Integer.parseInt(parts[7]);
        PotionEffectType effectType = PotionEffectType.getByKey(org.bukkit.NamespacedKey.minecraft(effectTypeKey));
        PotionEffect effect = new PotionEffect(Objects.requireNonNull(effectType), duration, amplifier);
        return new PotionTree(unlocked, name, effect, points, group, level);
    }
}