package net.undertaker.furtotemsmod.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.undertaker.furtotemsmod.data.ClientTotemSavedData;

import java.util.*;
import java.util.function.Supplier;

public class SyncBlacklistPacket {
    private final Map<UUID, Set<UUID>> blacklistPlayers;

    public SyncBlacklistPacket(Map<UUID, Set<UUID>> blacklistPlayers) {
        this.blacklistPlayers = blacklistPlayers;
    }

    public SyncBlacklistPacket(FriendlyByteBuf buf) {
        int blacklistSize = buf.readInt();
        this.blacklistPlayers = new HashMap<>();
        for (int i = 0; i < blacklistSize; i++) {
            UUID owner = buf.readUUID();
            int blacklistedSize = buf.readInt();
            Set<UUID> blacklist = new HashSet<>();
            for (int j = 0; j < blacklistedSize; j++) {
                blacklist.add(buf.readUUID());
            }
            this.blacklistPlayers.put(owner, blacklist);
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(blacklistPlayers.size());
        for (Map.Entry<UUID, Set<UUID>> entry : blacklistPlayers.entrySet()) {
            buf.writeUUID(entry.getKey());
            Set<UUID> blacklist = entry.getValue();
            buf.writeInt(blacklist.size());
            for (UUID member : blacklist) {
                buf.writeUUID(member);
            }
        }
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ClientTotemSavedData clientData = ClientTotemSavedData.get();
            clientData.updateBlacklistPlayers(this.blacklistPlayers);
        });
        context.get().setPacketHandled(true);
    }
}
