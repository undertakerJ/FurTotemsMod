package net.undertaker.furtotemsmod.items.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.undertaker.furtotemsmod.blocks.ModBlocks;
import net.undertaker.furtotemsmod.blocks.blockentity.UpgradableTotemBlockEntity;
import net.undertaker.furtotemsmod.items.ModItems;

public class TotemStaffItem extends Item {

  public TotemStaffItem(Properties pProperties) {
    super(pProperties);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    Level level = context.getLevel();
    BlockPos pos = context.getClickedPos().above();
    Player player = context.getPlayer();

    if (level.isClientSide || player == null) return InteractionResult.FAIL;
    if (!player.isShiftKeyDown()) return InteractionResult.FAIL;
    ItemStack heldItem = context.getItemInHand();
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
      player.displayClientMessage(Component.literal("Посох не соответствует ни одному материалу!"), true);
      return InteractionResult.FAIL;
    }

    // Проверка наличия требуемого блока
    if (!player.getInventory().contains(new ItemStack(materialType.getRequiredBlock()))) {
      player.displayClientMessage(Component.literal("У вас нет нужного блока: " + materialType.name()), true);
      return InteractionResult.FAIL;
    }

    // Установка блока тотема
    BlockState totemBlockState = ModBlocks.UPGRADABLE_TOTEM.get().defaultBlockState();
    level.setBlock(pos, totemBlockState, 3);

    if (level.getBlockEntity(pos) instanceof UpgradableTotemBlockEntity totemEntity) {
      totemEntity.setOwner(player.getUUID());
      totemEntity.upgrade(materialType);

      // Удаление блока из инвентаря
      removeItemFromInventory(player, materialType.getRequiredBlock().asItem(), 1);
      player.displayClientMessage(Component.literal("Тотем установлен с уровнем: " + materialType.name()), true);
      return InteractionResult.SUCCESS;
    }

    player.displayClientMessage(Component.literal("Не удалось установить тотем!"), true);
    return InteractionResult.FAIL;
  }



  private void removeItemFromInventory(Player player, Item item, int count) {
    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
      ItemStack stack = player.getInventory().getItem(i);
      if (stack.is(item)) {
        stack.shrink(count);
        if (stack.isEmpty()) {
          player.getInventory().setItem(i, ItemStack.EMPTY);
        }
        break;
      }
    }
  }

}
