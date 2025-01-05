package net.undertaker.furtotemsmod.networking.packets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.undertaker.furtotemsmod.items.custom.TotemItem;

import java.util.function.Supplier;

public class ChangeModePacket {
    private final int modeIndex;

    public ChangeModePacket(int modeIndex) {
        this.modeIndex = modeIndex;
    }

    public static void encode(ChangeModePacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.modeIndex);
    }

    public static ChangeModePacket decode(FriendlyByteBuf buf) {
        return new ChangeModePacket(buf.readInt());
    }

    public static void handle(ChangeModePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            ServerPlayer player = contextSupplier.get().getSender();
            if (player != null) {
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() instanceof TotemItem) {
                    CompoundTag tag = stack.getOrCreateTag();
                    TotemItem.StaffMode[] modes = TotemItem.StaffMode.values();

                    if (packet.modeIndex < 0 || packet.modeIndex >= modes.length) {
                        System.err.println("Invalid modeIndex received: " + packet.modeIndex);
                        return;
                    }

                    TotemItem.StaffMode newMode = modes[packet.modeIndex];
                    tag.putString("Mode", newMode.name());
                    stack.setHoverName(Component.literal(stack.getItem().getName(stack).getString() + " (" + newMode.getDisplayName() + ")"));

                    System.out.println("Mode updated to: " + newMode.name());
                }
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
