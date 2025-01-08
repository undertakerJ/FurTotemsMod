package net.undertaker.furtotemsmod.networking.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.undertaker.furtotemsmod.blocks.blockentity.UpgradableTotemBlockEntity;

import java.util.function.Supplier;

public class SyncTotemMaterialPacket {private final BlockPos pos;
    private final UpgradableTotemBlockEntity.MaterialType materialType;

    public SyncTotemMaterialPacket(BlockPos pos, UpgradableTotemBlockEntity.MaterialType materialType) {
        this.pos = pos;
        this.materialType = materialType;
    }

    public static void encode(SyncTotemMaterialPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeInt(packet.materialType.ordinal());
    }

    public static SyncTotemMaterialPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        UpgradableTotemBlockEntity.MaterialType materialType =
                UpgradableTotemBlockEntity.MaterialType.values()[buf.readInt()];
        return new SyncTotemMaterialPacket(pos, materialType);
    }

    public static void handle(SyncTotemMaterialPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null && level.getBlockEntity(packet.pos) instanceof UpgradableTotemBlockEntity totemEntity) {
                totemEntity.setMaterialType(packet.materialType);
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
