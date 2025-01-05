package net.undertaker.furtotemsmod.blocks.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.undertaker.furtotemsmod.blocks.blockentity.SmallTotemBlockEntity;
import net.undertaker.furtotemsmod.data.TotemSavedData;


public class SmallTotemBlock extends BaseEntityBlock {
    public static final int MAX_LIFETIME = 6000;
    public SmallTotemBlock(Properties properties) {
        super(properties);
    }
    private long placedTime;

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new SmallTotemBlockEntity(pos, state);
    }


    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 20);
            placedTime = level.getGameTime();
        }
    }
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        long currentTime = level.getGameTime();
        TotemSavedData data = TotemSavedData.get(level);

        double elapsedTime = currentTime - placedTime;

        int destroyStage = (int) Math.min(9, (elapsedTime / MAX_LIFETIME) * 9);

        level.destroyBlockProgress(pos.hashCode(), pos, destroyStage);


        if (elapsedTime < MAX_LIFETIME) {
            level.scheduleTick(pos, this, 20);
        } else {
            level.destroyBlock(pos, false);
           data.removeTotem(pos);
        }
    }
    }
