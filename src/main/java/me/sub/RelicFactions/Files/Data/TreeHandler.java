package me.sub.RelicFactions.Files.Data;

import java.util.UUID;

public class TreeHandler {

    private UUID faction;
    private boolean isTreeForceOnce;
    private boolean isTreeOnce;
    private boolean isTreeRemoveOne;

    public TreeHandler() {
        faction = null;
        isTreeForceOnce = false;
        isTreeOnce = false;
        isTreeRemoveOne = false;
    }

    public UUID getFaction() {
        return faction;
    }

    public void setFaction(UUID faction) {
        this.faction = faction;
    }

    public boolean isTreeForceOnce() {
        return isTreeForceOnce;
    }

    public void setTreeForceOnce(boolean treeForceOnce) {
        isTreeForceOnce = treeForceOnce;
    }

    public boolean isTreeOnce() {
        return isTreeOnce;
    }

    public void setTreeOnce(boolean treeOnce) {
        isTreeOnce = treeOnce;
    }

    public boolean isTreeRemoveOne() {
        return isTreeRemoveOne;
    }

    public void setTreeRemoveOne(boolean treeRemoveOne) {
        isTreeRemoveOne = treeRemoveOne;
    }
}
