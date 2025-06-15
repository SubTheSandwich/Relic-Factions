package me.sub.RelicFactions.Utils.Tab;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import me.sub.RelicFactions.Files.Classes.Faction;
import me.sub.RelicFactions.Files.Classes.RunningKOTH;
import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Files.Enums.Timer;
import me.sub.RelicFactions.Files.Normal.Tab;
import me.sub.RelicFactions.Main.Main;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.entity.Player;

import java.util.*;

public class TabManager {

    final String skinTexture = "ewogICJ0aW1lc3RhbXAiIDogMTc0ODc5Mjc0MzM4MiwKICAicHJvZmlsZUlkIiA6ICJlZmI1ZWQ2YjVjOTU0ODBlYWFmMjAyZDIxOWVmNjBjNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaWtlSHdhazAwMSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xNTViYjliZDk3ZThiNDIwMmFkYmRiNWJhZWRlNjhhNDRiODVmMWQ0MjRkYzBjMjJjNTA1ZjMwMGJmYzUyMDRkIgogICAgfQogIH0KfQ==";
    final String skinSignature = "EChalNpCWCd9uZXuU9+ImU4ZmVV0GdJ3v9+srDoFFps5vZm8faeslyZOJOfNc7uBcy2q0ZgncuwGeNmatH0teAwUmV4tnVZgd+gaMyu5rcgA5Xy1NF8QcVlJM+PfICd23Og7l0lwO9zaN1lO482CMRHaPNaYw96w+QMqDGmCwMIjUyTDe5F+zzzMm2NmqsIaUfWrd6z25Jx6+EuLekPTQReJW6d5rj8Cjr7wdFxv3qQ+oyFmD8NawKzzQ9hFd+RZOJYs4bE+1uG/Yx9PFaSNMUKjikyiLJscSK9kPy1EAlQIGjAynxA1ru6QpqcLJcTzt5brRAY0A8Tkqz+e0X5racqpF1ARv+F2WS8igpx4/94Ud3qUOrHrNdS8UUj30oRIO2Q7qbQjwZwt1YnkPBulS2+5cX5xR7ay3745eZPlxg0kBApX+KL2utxX+JIUADLjgxqxRK5mXWU5olt/bDcfQxmB/4S4EOgc8ywrwojTbUvuyIqYkOBX9cqvtV+hzVAC1kOs4W4rxWcjk6BTtbQtpkYWDPJlqPgMvzb9uhnrxSzKMubicM7LV1LEQFt057Xq+B7wnxGJn852GCQoZpQ2gWnZS98Sv+ug4rGOKGkAErS6EhjQiMKnPGeKP7N3Ahjp4M8cEmXQVPzTSCu7cDDwUTWSTh46NXTKmisTnGr8n18=";
    final String EMPTY =  "                                 ";

    public HashMap<UUID, TabPlayer> manager;

    public TabManager() {
        manager = new HashMap<>();
    }

    public void send(Player player) {
        boolean diff = clear(player);
        User user = User.get(player);

        if (!diff && manager.containsKey(player.getUniqueId())) return;

        PacketContainer addPacket = ProtocolLibrary.getProtocolManager()
                .createPacket(PacketType.Play.Server.PLAYER_INFO);
        addPacket.getPlayerInfoActions().write(0, EnumSet.of(
                EnumWrappers.PlayerInfoAction.ADD_PLAYER,
                EnumWrappers.PlayerInfoAction.UPDATE_LISTED,
                EnumWrappers.PlayerInfoAction.UPDATE_LATENCY,
                EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME
        ));

        List<PlayerInfoData> infoDataList = new ArrayList<>();
        List<String> lines = Tab.getSlotLines();
        ArrayList<TabSlot> slots = new ArrayList<>();

        for (int i = 0; i < 80; i++) {
            UUID uuid = getSlotUUID(player.getUniqueId(), i);
            String name = getName(player, C.chat(lines.get(i)));
            addTabSlot(slots, infoDataList, uuid, name, i, player);
        }

        TabPlayer tabPlayer = manager.getOrDefault(player.getUniqueId(), new TabPlayer(player.getUniqueId()));
        tabPlayer.setSlots(slots);
        tabPlayer.setHeader(C.chat(Tab.header));
        tabPlayer.setFooter(C.chat(Tab.footer));
        manager.put(player.getUniqueId(), tabPlayer);

        addPacket.getPlayerInfoDataLists().write(1, infoDataList);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, addPacket);

        // Send header/footer packet
        PacketContainer headerFooter = ProtocolLibrary.getProtocolManager()
                .createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
        if (Tab.headerEnabled) {
            headerFooter.getChatComponents().write(0, WrappedChatComponent.fromText(C.chat(Tab.header)));
        }
        if (Tab.footerEnabled) {
            headerFooter.getChatComponents().write(1, WrappedChatComponent.fromText(C.chat(Tab.footer)));
        }
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, headerFooter);
        Map<UUID, RunningKOTH> koths = Main.getInstance().runningKOTHS;

        int current = tabPlayer.getCurrentKoth();
        int kothCount = koths.size();

        if (current >= kothCount || current < 0) current = 0;

        tabPlayer.setTimeSinceLastKothUpdate(tabPlayer.getTimeSinceLastKothUpdate() + 1);
        if (tabPlayer.getTimeSinceLastKothUpdate() >= 10) {
            // Prepare for next update
            if (koths.isEmpty()) {
                int next = current + 1;
                if (next >= kothCount) next = 0;
                tabPlayer.setCurrentKoth(next);
                tabPlayer.setTimeSinceLastKothUpdate(0);
                return;
            }


            List<RunningKOTH> kothList = koths.values().stream()
                    .sorted(Comparator.comparing(rk -> rk.getKOTH().getName()))
                    .toList();

            RunningKOTH runningKOTH = kothList.get(current);
            if (runningKOTH.getControllingPlayer() == null) {
                int next = current + 1;
                if (next >= kothCount) next = 0;
                tabPlayer.setCurrentKoth(next);
                tabPlayer.setTimeSinceLastKothUpdate(0);
                return;
            }

            UUID uuid = runningKOTH.getControllingPlayer();
            if (!user.hasFaction()) {
                int next = current + 1;
                if (next >= kothCount) next = 0;
                tabPlayer.setCurrentKoth(next);
                tabPlayer.setTimeSinceLastKothUpdate(0);
                return;
            }
            Faction faction = Faction.get(user.getFaction());
            if (!uuid.equals(player.getUniqueId()) && faction.getOnlineMembers().stream()
                    .map(Player::getUniqueId)
                    .noneMatch(id -> id.equals(uuid))) {
                int next = current + 1;
                if (next >= kothCount) next = 0;
                tabPlayer.setCurrentKoth(next);
                tabPlayer.setTimeSinceLastKothUpdate(0);
                return;
            }
            tabPlayer.setTimeSinceLastKothUpdate(0);
        }
    }

    private void addTabSlot(
            List<TabSlot> slots,
            List<PlayerInfoData> infoDataList,
            UUID uuid,
            String name,
            int index,
            Player player
    ) {
        TabSlot slot = new TabSlot(uuid, name);
        WrappedGameProfile profile = new WrappedGameProfile(uuid, String.format("%02d", index));
        profile.getProperties().put("textures", new WrappedSignedProperty("textures", skinTexture, skinSignature));
        infoDataList.add(new PlayerInfoData(
                uuid,
                0,
                true,
                EnumWrappers.NativeGameMode.SURVIVAL,
                profile,
                WrappedChatComponent.fromText(name),
                WrappedRemoteChatSessionData.fromPlayer(player)
        ));
        slots.add(slot);
    }

    private UUID getSlotUUID(UUID playerUUID, int slotIndex) {
        String base = playerUUID.toString() + ":" + slotIndex;
        return UUID.nameUUIDFromBytes(base.getBytes());
    }

    private boolean clear(Player player) {
        List<String> lines = Tab.getSlotLines();
        ArrayList<TabSlot> slots = new ArrayList<>();

        for (int i = 0; i < 80; i++) {
            UUID uuid = getSlotUUID(player.getUniqueId(), i);
            String name = getName(player, C.chat(lines.get(i)));
            TabSlot slot = new TabSlot(uuid, name);
            slots.add(slot);
        }

        boolean different = false;
        UUID playerId = player.getUniqueId();

        if (manager.containsKey(playerId)) {
            TabPlayer tabPlayer = manager.get(playerId);

            String currentHeader = tabPlayer.getHeader();
            String newHeader = C.chat(Tab.header);
            if (!currentHeader.equals(newHeader)) {
                different = true;
            }

            String currentFooter = tabPlayer.getFooter();
            String newFooter = C.chat(Tab.footer);
            if (!currentFooter.equals(newFooter)) {
                different = true;
            }

            List<TabSlot> oldSlots = tabPlayer.getSlots();
            for (int i = 0; i < slots.size(); i++) {
                if (different) break;
                TabSlot oldSlot = oldSlots.get(i);
                TabSlot newSlot = slots.get(i);
                if (!oldSlot.equals(newSlot)) {
                    different = true;
                    break;
                }
            }
        } else {
            different = true;
        }

        if (different || !manager.containsKey(playerId)) {
            if (manager.containsKey(playerId)) {
                TabPlayer tabPlayer = manager.get(playerId);
                for (TabSlot slot : tabPlayer.getSlots()) {
                    PacketContainer packet = ProtocolLibrary.getProtocolManager()
                            .createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE);
                    packet.getUUIDLists().write(0, Collections.singletonList(slot.getUUID()));
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                }
            }
            PacketContainer clearHeaderFooter = ProtocolLibrary.getProtocolManager()
                    .createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
            clearHeaderFooter.getChatComponents().write(0, WrappedChatComponent.fromText(""));
            clearHeaderFooter.getChatComponents().write(1, WrappedChatComponent.fromText(""));
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, clearHeaderFooter);
        }

        return different;
    }

    private String getName(Player player, String name) {
        User user = User.get(player);
        if (name.isEmpty()) return EMPTY;

        // Replace placeholders
        name = name.replace("%kills%", String.valueOf(user.getKills()))
                .replace("%deaths%", String.valueOf(user.getDeaths()));

        if (name.contains("%faction-at%")) {
            name = name.replace("%faction-at%", Objects.requireNonNull(Faction.getAtName(player)));
        }

        if (name.contains("%location%")) {
            name = name.replace("%location%", String.format(
                    "X: %d, Y: %d, Z: %d",
                    player.getLocation().getBlockX(),
                    player.getLocation().getBlockY(),
                    player.getLocation().getBlockZ()
            ));
        }

        // Handle <display=%has_faction%>
        if (name.contains("<display=%has_faction%")) {
            if (!user.hasFaction()) {
                return EMPTY;
            } else {
                Faction faction = Faction.get(user.getFaction());
                name = name.replace("<display=%has_faction%", "");
                name = name.replace("%dtr%", Faction.formatDTR(faction.getDTR()))
                        .replace("%faction-online-count%", String.valueOf(faction.getOnlineMembers().size()))
                        .replace("%faction-balance%", Main.getEconomy().format(faction.getBalance().doubleValue()));
            }
        }

        // Handle <display=%has_faction_home%>
        if (name.contains("<display=%has_faction_home%")) {
            name = name.replace("<display=%has_faction_home%", "");
            if (!user.hasFaction()) {
                return EMPTY;
            } else {
                Faction faction = Faction.get(user.getFaction());
                if (faction.getHome() == null) {
                    return EMPTY;
                } else {
                    name = name.replace("%faction-home%",
                            faction.getHome().getBlockX() + ", " + faction.getHome().getBlockZ());
                }
            }
        }

        if (name.contains("<display=%is_koth%")) {
            name = name.replace("<display=%is_koth%", "");
            Map<UUID, RunningKOTH> koths = Main.getInstance().runningKOTHS;
            if (koths.isEmpty()) return EMPTY;

            // Sort KOTHs by name for a consistent order
            List<RunningKOTH> kothList = koths.values().stream()
                    .sorted(Comparator.comparing(rk -> rk.getKOTH().getName()))
                    .toList();

            TabPlayer tabPlayer = manager.get(player.getUniqueId());
            int current = tabPlayer.getCurrentKoth();
            int kothCount = kothList.size();

            // Ensure current index is in bounds
            if (current >= kothCount || current < 0) current = 0;

            RunningKOTH runningKOTH = kothList.get(current);

            // Replace placeholders
            name = name.replace("%active-koth%", runningKOTH.getKOTH().getName());
            name = name.replace("%active-koth-time%", Timer.format(runningKOTH.getTimeLeft()));
        }
        return name;
    }
}