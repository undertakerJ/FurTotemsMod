package net.undertaker.furtotemsmod.blocks.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.undertaker.furtotemsmod.blocks.blockentity.SmallTotemBlockEntity;
import net.undertaker.furtotemsmod.data.TotemSavedData;


public class SmallTotemBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final int MAX_LIFETIME = 6000;
    public SmallTotemBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }
    private long placedTime;

    public static final VoxelShape SHAPE = Block.box(3,0,3, 13, 14, 13);

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }


    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

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
           data.removeTotem(level, pos);
        }
    }
    }
