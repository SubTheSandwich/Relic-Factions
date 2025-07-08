package me.sub.RelicFactions.Files.Data;

import me.sub.RelicFactions.Files.Enums.Tree;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

public class EnchantmentTree extends Tree {
    private final Enchantment enchantment;
    private final int enchantLevel;

    public EnchantmentTree(boolean unlocked, String name, Enchantment enchantment, int enchantLevel, int points, String group, int level) {
        super(unlocked, name, points, group, level);
        this.enchantment = enchantment;
        this.enchantLevel = enchantLevel;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public int getEnchantLevel() {
        return enchantLevel;
    }

    @Override
    public String toString() {
        return "ENCHANTMENT:" + isUnlocked() + ":" + enchantment.getKey().getKey() + ":" + enchantLevel;
    }

    @Override
    public String serialize() {
        return String.join(";",
                Boolean.toString(isUnlocked()),
                getName(),
                enchantment.getKey().getKey(),
                Integer.toString(enchantLevel),
                Integer.toString(getPoints()),
                getGroup(),
                Integer.toString(getLevel())
        );
    }

    public static EnchantmentTree deserialize(String data) {
        String[] parts = data.split(";");
        boolean unlocked = Boolean.parseBoolean(parts[0]);
        String name = parts[1];
        String enchKey = parts[2];
        int enchantLevel = Integer.parseInt(parts[3]);
        int points = Integer.parseInt(parts[4]);
        String group = parts[5];
        int level = Integer.parseInt(parts[6]);
        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchKey));
        return new EnchantmentTree(unlocked, name, enchantment, enchantLevel, points, group, level);
    }
}