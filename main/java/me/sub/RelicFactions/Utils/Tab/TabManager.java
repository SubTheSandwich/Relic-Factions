package me.sub.RelicFactions.Utils.Tab;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.sub.RelicFactions.Files.Normal.Tab;
import me.sub.RelicFactions.Utils.C;
import org.bukkit.entity.Player;

import java.util.*;

public class TabManager {

    public HashMap<UUID, ArrayList<TabSlot>> manager;

    public TabManager() {
        manager = new HashMap<>();
    }

    public void send(Player player) {
        clear(player);
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
            infoDataList.add(new PlayerInfoData(
                    new WrappedGameProfile(uuid, String.format("%02d", i)),
                    0,
                    EnumWrappers.NativeGameMode.SURVIVAL,
                    WrappedChatComponent.fromText(name)
            ));
            slots.add(slot);
        }

        manager.put(player.getUniqueId(), slots);

        addPacket.getPlayerInfoDataLists().write(1, infoDataList);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, addPacket);

        // Send header/footer packet
        PacketContainer headerFooter = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
        headerFooter.getChatComponents().write(0, WrappedChatComponent.fromText(C.chat(Tab.header)));
        headerFooter.getChatComponents().write(1, WrappedChatComponent.fromText(C.chat(Tab.footer)));
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, headerFooter);
    }


    private void clear(Player player) {
        if (manager.containsKey(player.getUniqueId())) {
            for (TabSlot slot : manager.get(player.getUniqueId())) {
                PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE);
                packet.getUUIDLists().write(0, Collections.singletonList(slot.getUUID()));
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            }
        }
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE);
        packet.getUUIDLists().write(0, Collections.singletonList(player.getUniqueId()));
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);

        PacketContainer clearHeaderFooter = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
        clearHeaderFooter.getChatComponents().write(0, WrappedChatComponent.fromText(""));
        clearHeaderFooter.getChatComponents().write(1, WrappedChatComponent.fromText(""));
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, clearHeaderFooter);
    }


}
