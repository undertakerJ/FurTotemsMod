package net.undertaker.furtotemsmod.networking.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.undertaker.furtotemsmod.data.ClientTotemSavedData;
import net.undertaker.furtotemsmod.data.TotemSavedData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class SyncTotemsPacket {
    private final Map<BlockPos, TotemSavedData.TotemData> totemDataMap;

    public SyncTotemsPacket(Map<BlockPos, TotemSavedData.TotemData> totemDataMap) {
        this.totemDataMap = totemDataMap;
    }

    public SyncTotemsPacket(FriendlyByteBuf buf) {
        int totemMapSize = buf.readInt();
        this.totemDataMap = new HashMap<>();
        for (int i = 0; i < totemMapSize; i++) {
            BlockPos pos = buf.readBlockPos();
            UUID owner = buf.readUUID();
            int radius = buf.readInt();
            String type = buf.readUtf();
            TotemSavedData.TotemData data = new TotemSavedData.TotemData(owner, radius, type);
            this.totemDataMap.put(pos, data);
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(totemDataMap.size());
        for (Map.Entry<BlockPos, TotemSavedData.TotemData> entry : totemDataMap.entrySet()) {
            buf.writeBlockPos(entry.getKey());
            TotemSavedData.TotemData data = entry.getValue();
            buf.writeUUID(data.getOwner());
            buf.writeInt(data.getRadius());
            buf.writeUtf(data.getType());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ClientTotemSavedData clientData = ClientTotemSavedData.get();
            clientData.updateTotemData(this.totemDataMap);
        });
        context.get().setPacketHandled(true);
    }
}
