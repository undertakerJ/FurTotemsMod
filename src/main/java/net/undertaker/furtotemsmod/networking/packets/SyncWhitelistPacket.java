package net.undertaker.furtotemsmod.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.undertaker.furtotemsmod.data.ClientTotemSavedData;

import java.util.*;
import java.util.function.Supplier;

public class SyncWhitelistPacket {
    private final Map<UUID, Set<UUID>> whitelistPlayers;

    public SyncWhitelistPacket(Map<UUID, Set<UUID>> whitelistPlayers) {
        this.whitelistPlayers = whitelistPlayers;
    }

    public SyncWhitelistPacket(FriendlyByteBuf buf) {
        int whitelistSize = buf.readInt();
        this.whitelistPlayers = new HashMap<>();
        for (int i = 0; i < whitelistSize; i++) {
            UUID owner = buf.readUUID();
            int memberSize = buf.readInt();
            Set<UUID> members = new HashSet<>();
            for (int j = 0; j < memberSize; j++) {
                members.add(buf.readUUID());
            }
            this.whitelistPlayers.put(owner, members);
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(whitelistPlayers.size());
        for (Map.Entry<UUID, Set<UUID>> entry : whitelistPlayers.entrySet()) {
            buf.writeUUID(entry.getKey());
            Set<UUID> members = entry.getValue();
            buf.writeInt(members.size());
            for (UUID member : members) {
                buf.writeUUID(member);
            }
        }
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ClientTotemSavedData clientData = ClientTotemSavedData.get();
            clientData.updateWhitelistPlayers(this.whitelistPlayers);
        });
        context.get().setPacketHandled(true);
    }
}
