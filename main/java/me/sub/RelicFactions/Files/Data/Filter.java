package me.sub.RelicFactions.Files.Data;

import org.bukkit.Material;

import java.util.ArrayList;

public class Filter {

    private boolean enabled;
    private final ArrayList<Material> items;

    public Filter() {
        enabled = false;
        items = new ArrayList<>();
    }

    public ArrayList<Material> getItems() {
        return items;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
