package me.sub.RelicFactions.Utils.Tab;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import me.sub.RelicFactions.Files.Normal.Tab;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.entity.Player;

import java.util.*;

public class TabManager {

    final String skinTexture = "ewogICJ0aW1lc3RhbXAiIDogMTc0ODc5Mjc0MzM4MiwKICAicHJvZmlsZUlkIiA6ICJlZmI1ZWQ2YjVjOTU0ODBlYWFmMjAyZDIxOWVmNjBjNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaWtlSHdhazAwMSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xNTViYjliZDk3ZThiNDIwMmFkYmRiNWJhZWRlNjhhNDRiODVmMWQ0MjRkYzBjMjJjNTA1ZjMwMGJmYzUyMDRkIgogICAgfQogIH0KfQ==";
    final String skinSignature = "EChalNpCWCd9uZXuU9+ImU4ZmVV0GdJ3v9+srDoFFps5vZm8faeslyZOJOfNc7uBcy2q0ZgncuwGeNmatH0teAwUmV4tnVZgd+gaMyu5rcgA5Xy1NF8QcVlJM+PfICd23Og7l0lwO9zaN1lO482CMRHaPNaYw96w+QMqDGmCwMIjUyTDe5F+zzzMm2NmqsIaUfWrd6z25Jx6+EuLekPTQReJW6d5rj8Cjr7wdFxv3qQ+oyFmD8NawKzzQ9hFd+RZOJYs4bE+1uG/Yx9PFaSNMUKjikyiLJscSK9kPy1EAlQIGjAynxA1ru6QpqcLJcTzt5brRAY0A8Tkqz+e0X5racqpF1ARv+F2WS8igpx4/94Ud3qUOrHrNdS8UUj30oRIO2Q7qbQjwZwt1YnkPBulS2+5cX5xR7ay3745eZPlxg0kBApX+KL2utxX+JIUADLjgxqxRK5mXWU5olt/bDcfQxmB/4S4EOgc8ywrwojTbUvuyIqYkOBX9cqvtV+hzVAC1kOs4W4rxWcjk6BTtbQtpkYWDPJlqPgMvzb9uhnrxSzKMubicM7LV1LEQFt057Xq+B7wnxGJn852GCQoZpQ2gWnZS98Sv+ug4rGOKGkAErS6EhjQiMKnPGeKP7N3Ahjp4M8cEmXQVPzTSCu7cDDwUTWSTh46NXTKmisTnGr8n18=";

    public HashMap<UUID, TabPlayer> manager;

    public TabManager() {
        manager = new HashMap<>();
    }

    public void send(Player player) {
        boolean diff = clear(player);

        if (!diff && manager.containsKey(player.getUniqueId())) return;
        PacketContainer addPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
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
            UUID uuid = UUID.randomUUID();
            String name = C.chat(lines.get(i));
            TabSlot slot = new TabSlot(uuid, name);
            WrappedGameProfile profile = new WrappedGameProfile(uuid, String.format("%02d", i));
            profile.getProperties().put("textures", new WrappedSignedProperty("textures",
                    skinTexture,
                    skinSignature));
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

        TabPlayer tabPlayer = new TabPlayer(player.getUniqueId());
        if (manager.containsKey(player.getUniqueId())) tabPlayer = manager.get(player.getUniqueId());
        tabPlayer.setSlots(slots);
        tabPlayer.setHeader(C.chat(Tab.header));
        tabPlayer.setFooter(C.chat(Tab.footer));

        if (!manager.containsKey(player.getUniqueId())) manager.put(player.getUniqueId(), tabPlayer);
        addPacket.getPlayerInfoDataLists().write(1, infoDataList);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, addPacket);

        // Send header/footer packet
        PacketContainer headerFooter = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
        headerFooter.getChatComponents().write(0, WrappedChatComponent.fromText(C.chat(Tab.header)));
        headerFooter.getChatComponents().write(1, WrappedChatComponent.fromText(C.chat(Tab.footer)));
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, headerFooter);
    }


    private boolean clear(Player player) {
        List<String> lines = Tab.getSlotLines();

        ArrayList<TabSlot> slots = new ArrayList<>();

        for (int i = 0; i < 80; i++) {
            UUID uuid = UUID.randomUUID();
            String name = C.chat(lines.get(i));
            TabSlot slot = new TabSlot(uuid, name);
            slots.add(slot);
        }
        boolean different = false;
        if (manager.containsKey(player.getUniqueId())) {
            TabPlayer tabPlayer = manager.get(player.getUniqueId());
            for (int i = 0; i < slots.size(); i++) {
                TabSlot oldSlot = tabPlayer.getSlots().get(i);
                TabSlot newSlot = slots.get(i);
                if (!oldSlot.equals(newSlot)) {
                    different = true;
                    break;
                }
            }
            if (!tabPlayer.getHeader().equals(C.chat(Tab.header))) {
                different = true;
            }
            if (!tabPlayer.getFooter().equals(C.chat(Tab.footer))) {
                different = true;
            }
        }
        if (different || !manager.containsKey(player.getUniqueId())) {
            if (manager.containsKey(player.getUniqueId())) {
                TabPlayer tabPlayer = manager.get(player.getUniqueId());
                for (TabSlot slot : tabPlayer.getSlots()) {
                    PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE);
                    packet.getUUIDLists().write(0, Collections.singletonList(slot.getUUID()));
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                }
            }
            PacketContainer clearHeaderFooter = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
            clearHeaderFooter.getChatComponents().write(0, WrappedChatComponent.fromText(""));
            clearHeaderFooter.getChatComponents().write(1, WrappedChatComponent.fromText(""));
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, clearHeaderFooter);
        }
        return different;
    }
}
