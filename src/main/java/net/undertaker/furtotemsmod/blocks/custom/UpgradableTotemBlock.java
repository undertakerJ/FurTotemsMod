package net.undertaker.furtotemsmod.blocks.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.undertaker.furtotemsmod.blocks.blockentity.UpgradableTotemBlockEntity;
import net.undertaker.furtotemsmod.items.ModItems;
import net.undertaker.furtotemsmod.util.ParticleUtils;
import net.undertaker.furtotemsmod.util.TotemSavedData;

public class UpgradableTotemBlock extends BaseEntityBlock {

  public UpgradableTotemBlock(Properties properties) {
    super(properties);
  }

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
      totemEntity.setOwner(null);
      totemEntity.upgrade(UpgradableTotemBlockEntity.MaterialType.COPPER);
      radiusTotem = totemEntity.getRadius();
      totemEntity.setChanged();
    }
  }

  @Override
  public void onRemove(
      BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
    super.onRemove(state, level, pos, newState, isMoving);

    if (!level.isClientSide() && level.getBlockEntity(pos) instanceof UpgradableTotemBlockEntity) {

      ServerLevel serverLevel = (ServerLevel) level;
      TotemSavedData data = TotemSavedData.get(serverLevel);
      data.removeTotem(pos);
    }
  }

  @Override
  public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
    if (!level.isClientSide && level.getBlockEntity(pos) instanceof UpgradableTotemBlockEntity totemEntity) {
      ItemStack heldItem = player.getItemInHand(hand);
      CompoundTag tag = heldItem.getOrCreateTag();

      UpgradableTotemBlockEntity.MaterialType currentType = totemEntity.getMaterialType();
      Item heldItemType = heldItem.getItem();

      UpgradableTotemBlockEntity.MaterialType materialType = null;

      if (heldItemType == ModItems.COPPER_STAFF_ITEM.get()) {
        materialType = UpgradableTotemBlockEntity.MaterialType.COPPER;
      } else if (heldItemType == ModItems.IRON_STAFF_ITEM.get()) {
        materialType = UpgradableTotemBlockEntity.MaterialType.IRON;
      } else if (heldItemType == ModItems.GOLD_STAFF_ITEM.get()) {
        materialType = UpgradableTotemBlockEntity.MaterialType.GOLD;
      } else if (heldItemType == ModItems.DIAMOND_STAFF_ITEM.get()) {
        materialType = UpgradableTotemBlockEntity.MaterialType.DIAMOND;
      } else if (heldItemType == ModItems.NETHERITE_STAFF_ITEM.get()) {
        materialType = UpgradableTotemBlockEntity.MaterialType.NETHERITE;
      }

      if (materialType == null) {
        player.displayClientMessage(Component.literal("Посох не настроен для улучшения!"), true);
        return InteractionResult.FAIL;
      }

      if (materialType.ordinal() > currentType.ordinal()) {
        applyUpgrade(level, pos, player, totemEntity, materialType);
        player.displayClientMessage(Component.literal("Тотем улучшен до уровня: " + materialType.name()), true);
        return InteractionResult.SUCCESS;
      } else if (materialType.ordinal() <= currentType.ordinal()) {
        player.displayClientMessage(Component.literal("Этот уровень уже применен или выше!"), true);
        return InteractionResult.FAIL;
      }
    }

    return super.use(state, level, pos, player, hand, hit);
  }


  private void applyUpgrade(
      Level level,
      BlockPos pos,
      Player player,
      UpgradableTotemBlockEntity totemEntity,
      UpgradableTotemBlockEntity.MaterialType newType) {
    if (totemEntity.getOwner() == null) {
      totemEntity.setOwner(player.getUUID());
    }

    if (level instanceof ServerLevel serverLevel) {
      TotemSavedData data = TotemSavedData.get(serverLevel);

      if (data.isOverlapping(pos, newType.getRadius(), player.getUUID())) {
        player.displayClientMessage(
            Component.literal("Улучшение невозможно: зоны тотемов пересекаются!"), true);
        return;
      }

      totemEntity.upgrade(newType);
      data.addTotem(pos, totemEntity.getOwner(), totemEntity.getRadius(), "Upgradable");
      radiusTotem = totemEntity.getRadius();
      totemEntity.setChanged();
      player.displayClientMessage(
              Component.literal("Тотем улучшен до уровня " + newType.name() + "!"), true);
    }
  }

  private int radiusTotem;

  @Override
  public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
    if (!pLevel.isClientSide) return;

    double centerX = pPos.getX() + 0.5;
    double centerY = pPos.getY() + 0.5;
    double centerZ = pPos.getZ() + 0.5;

    ParticleUtils.spawnCircularParticles(pLevel, centerX, centerY, centerZ, radiusTotem);
  }
}
