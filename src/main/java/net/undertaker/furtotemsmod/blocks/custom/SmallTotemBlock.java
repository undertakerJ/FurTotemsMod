package net.undertaker.furtotemsmod.blocks.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.undertaker.furtotemsmod.blocks.blockentity.SmallTotemBlockEntity;
import net.undertaker.furtotemsmod.util.ParticleUtils;


public class SmallTotemBlock extends BaseEntityBlock {
    public SmallTotemBlock(Properties properties) {
        super(properties);
    }


    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new SmallTotemBlockEntity(pos, state);
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        if (!pLevel.isClientSide) return;

        double centerX = pPos.getX() + 0.5;
        double centerY = pPos.getY() + 0.5;
        double centerZ = pPos.getZ() + 0.5;

        ParticleUtils.spawnCircularParticles(pLevel, centerX, centerY, centerZ, SmallTotemBlockEntity.getRadius());
    }

}