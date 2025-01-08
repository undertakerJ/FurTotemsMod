package net.undertaker.furtotemsmod.blocks.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.undertaker.furtotemsmod.blocks.blockentity.UpgradableTotemBlockEntity;
import net.undertaker.furtotemsmod.data.TotemSavedData;
import net.undertaker.furtotemsmod.items.ModItems;
import net.undertaker.furtotemsmod.items.custom.TotemItem;
import net.undertaker.furtotemsmod.networking.ModNetworking;
import net.undertaker.furtotemsmod.networking.packets.SyncTotemMaterialPacket;

public class UpgradableTotemBlock extends BaseEntityBlock {

  public UpgradableTotemBlock(Properties properties) {
    super(properties);
  }

  public static final VoxelShape SHAPE = Block.box(3,0,3, 13, 14, 13);

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new UpgradableTotemBlockEntity(pos, state);
  }

  @Override
  public void onPlace(
          BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
    super.onPlace(state, level, pos, oldState, isMoving);

    if (!level.isClientSide()
            && level.getBlockEntity(pos) instanceof UpgradableTotemBlockEntity totemEntity) {
      if (totemEntity.getOwner() == null) {
        totemEntity.setOwner(null);
      }
      if (totemEntity.getMaterialType() == null) {
        totemEntity.upgrade(UpgradableTotemBlockEntity.MaterialType.COPPER);
      }
      totemEntity.setChanged();
    }
  }

  @Override
  public InteractionResult use(
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit) {
    if (!level.isClientSide
        && level.getBlockEntity(pos) instanceof UpgradableTotemBlockEntity totemEntity) {
      ItemStack heldItem = player.getItemInHand(hand);

      if (heldItem.getItem() instanceof TotemItem staffItem) {
        applyUpgrade(level, pos, player, totemEntity, staffItem);
        return InteractionResult.SUCCESS;
      }

      player.displayClientMessage(
          Component.translatable("message.furtotemsmod.must_use_totem_staff"), true);
      return InteractionResult.FAIL;
    }

    return super.use(state, level, pos, player, hand, hit);
  }

  private UpgradableTotemBlockEntity.MaterialType getMaxUpgradeTypeForStaff(TotemItem staff) {
    if (staff == ModItems.COPPER_STAFF_ITEM.get()) {
      return UpgradableTotemBlockEntity.MaterialType.COPPER;
    } else if (staff == ModItems.IRON_STAFF_ITEM.get()) {
      return UpgradableTotemBlockEntity.MaterialType.IRON;
    } else if (staff == ModItems.GOLD_STAFF_ITEM.get()) {
      return UpgradableTotemBlockEntity.MaterialType.GOLD;
    } else if (staff == ModItems.DIAMOND_STAFF_ITEM.get()) {
      return UpgradableTotemBlockEntity.MaterialType.DIAMOND;
    } else if (staff == ModItems.NETHERITE_STAFF_ITEM.get()) {
      return UpgradableTotemBlockEntity.MaterialType.NETHERITE;
    }
    return UpgradableTotemBlockEntity.MaterialType.COPPER;
  }

  @Override
  public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
    return SHAPE;
  }

  private void applyUpgrade(
      Level level,
      BlockPos pos,
      Player player,
      UpgradableTotemBlockEntity totemEntity,
      TotemItem staffItem) {

    UpgradableTotemBlockEntity.MaterialType currentType = totemEntity.getMaterialType();
    UpgradableTotemBlockEntity.MaterialType maxType = getMaxUpgradeTypeForStaff(staffItem);
    UpgradableTotemBlockEntity.MaterialType nextType;

    if (currentType.ordinal() >= maxType.ordinal()) {
      nextType = UpgradableTotemBlockEntity.MaterialType.COPPER;
    } else {
      nextType = UpgradableTotemBlockEntity.MaterialType.values()[currentType.ordinal() + 1];
    }

    if (level instanceof ServerLevel serverLevel) {
      if (totemEntity.getOwner() == null) {
        totemEntity.setOwner(player.getUUID());
      }

      TotemSavedData data = TotemSavedData.get(serverLevel);

      if (data.isOverlapping(pos, nextType.getRadius(), player.getUUID())) {
        player.displayClientMessage(
                Component.translatable("message.furtotemsmod.upgrade_impossible_zones_overlap"), true);
        return;
      }


      totemEntity.upgrade(nextType);
      totemEntity.setChanged();

      ModNetworking.sendToAllPlayers(new SyncTotemMaterialPacket(pos, nextType), serverLevel);

            player.displayClientMessage(
          Component.literal(
              Component.translatable("message.furtotemsmod.totem_upgraded_to_level").getString()
                  + nextType.name()),
          true);
    }
  }
}
