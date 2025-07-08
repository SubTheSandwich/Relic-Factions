package me.sub.RelicFactions.Files.Enums;

import me.sub.RelicFactions.Files.Data.EnchantmentTree;
import me.sub.RelicFactions.Files.Data.PotionTree;

import java.util.ArrayList;

public abstract class Tree {
    private boolean unlocked;
    private final String name;
    private final int points;
    private final String group;
    private final int level;

    public Tree(boolean unlocked, String name, int points, String group, int level) {
        this.unlocked = unlocked;
        this.name = name;
        this.points = points;
        this.group = group;
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public String getGroup() {
        return group;
    }

    public int getPoints() {
        return points;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public String getName() {
        return name;
    }

    public abstract String serialize();

    public static String serializeTreeList(ArrayList<Tree> trees) {
        StringBuilder sb = new StringBuilder();
        for (Tree tree : trees) {
            sb.append(tree.getClass().getSimpleName())
                    .append("|")
                    .append(tree.serialize())
                    .append("\n");
        }
        return sb.toString();
    }

    public static ArrayList<Tree> deserializeTreeList(String data) {
        ArrayList<Tree> trees = new ArrayList<>();
        String[] lines = data.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] typeAndData = line.split("\\|", 2);
            String type = typeAndData[0];
            String serializedData = typeAndData[1];
            switch (type) {
                case "EnchantmentTree":
                    trees.add(EnchantmentTree.deserialize(serializedData));
                    break;
                case "PotionTree":
                    trees.add(PotionTree.deserialize(serializedData));
                    break;
                default:
                    // Unknown type, skip or throw error
                    break;
            }
        }
        return trees;
    }
}