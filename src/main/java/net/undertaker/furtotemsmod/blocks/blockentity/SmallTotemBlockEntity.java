package net.undertaker.furtotemsmod.blocks.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.undertaker.furtotemsmod.blocks.ModBlockEntities;
import java.util.UUID;

public class SmallTotemBlockEntity extends BlockEntity {

    public static final int RADIUS = 5;
    public static UUID ownerUUID;

    public SmallTotemBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SMALL_TOTEM.get(), pos, state);
    }

    public static void setOwner(UUID owner) {
        ownerUUID = owner;
    }

    public static UUID getOwner() {
        return ownerUUID;
    }

    public static int getRadius() {
        return RADIUS;
    }

}
