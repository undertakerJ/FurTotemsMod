package net.undertaker.furtotemsmod.items.custom;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.undertaker.furtotemsmod.Config;
import net.undertaker.furtotemsmod.FurTotemsMod;
import net.undertaker.furtotemsmod.attributes.ModAttributes;
import net.undertaker.furtotemsmod.blocks.ModBlocks;
import net.undertaker.furtotemsmod.blocks.blockentity.UpgradableTotemBlockEntity;
import net.undertaker.furtotemsmod.data.ClientTotemSavedData;
import net.undertaker.furtotemsmod.data.TotemSavedData;
import net.undertaker.furtotemsmod.networking.ModNetworking;
import net.undertaker.furtotemsmod.networking.packets.ChangeModePacket;
import net.undertaker.furtotemsmod.render.ClientTotemRadiusRender;

@Mod.EventBusSubscriber(modid = FurTotemsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TotemItem extends Item {
  public TotemItem(Properties pProperties) {
    super(pProperties);
  }

  private static final String MODE_KEY = "Mode";

  public enum StaffMode {
    PLACE_TOTEM(Component.translatable("action.furtotemsmod.place_totem").getString()),
    REMOVE_TOTEM(Component.translatable("action.furtotemsmod.remove_totem").getString()),
    ADD_MEMBER(Component.translatable("action.furtotemsmod.add_member").getString()),
    REMOVE_MEMBER(Component.translatable("action.furtotemsmod.remove_member").getString()),
    ADD_BLACKLIST(Component.translatable("action.furtotemsmod.add_blacklist").getString()),
    REMOVE_BLACKLIST(Component.translatable("action.furtotemsmod.remove_blacklist").getString());

    private final String displayName;

    StaffMode(String displayName) {
      this.displayName = displayName;
    }

    public String getDisplayName() {
      return displayName;
    }
  }

  @Override
  public InteractionResult interactLivingEntity(
      ItemStack pStack, Player player, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
    Level level = player.getLevel();
    BlockPos pos = player.blockPosition();

    if (level.isClientSide || player == null) return InteractionResult.FAIL;
    ItemStack heldItem = player.getMainHandItem();
    CompoundTag tag = heldItem.getOrCreateTag();
    StaffMode currentMode = getModeFromTag(tag);
    return switch (currentMode) {
      case ADD_MEMBER -> handleAddMember(level, pos, player, pInteractionTarget);
      case REMOVE_MEMBER -> handleRemoveMember(level, pos, player, pInteractionTarget);
      case ADD_BLACKLIST -> handleAddToBlacklist(level, pos, player, pInteractionTarget);
      case REMOVE_BLACKLIST -> handleRemoveFromBlacklist(level, pos, player, pInteractionTarget);
      default -> InteractionResult.FAIL;
    };
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    Level level = context.getLevel();
    BlockPos pos = context.getClickedPos();
    Player player = context.getPlayer();

    if (level.isClientSide || player == null) return InteractionResult.FAIL;

    ItemStack heldItem = context.getItemInHand();
    CompoundTag tag = heldItem.getOrCreateTag();
    StaffMode currentMode = getModeFromTag(tag);

    return switch (currentMode) {
      case PLACE_TOTEM -> handlePlaceTotem(context, level, pos, player);
      case REMOVE_TOTEM -> handleRemoveTotem(level, pos, player);
      default -> InteractionResult.FAIL;
    };
  }

  @OnlyIn(Dist.CLIENT)
  @SubscribeEvent
  public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
    Player player = Minecraft.getInstance().player;
    if (player != null && player.isShiftKeyDown()) {
      ItemStack stack = player.getMainHandItem();
      if (stack.getItem() instanceof TotemItem) {
        CompoundTag tag = stack.getOrCreateTag();

        StaffMode currentMode;
        try {
          currentMode = StaffMode.valueOf(tag.getString("Mode"));
        } catch (IllegalArgumentException | NullPointerException e) {
          currentMode = StaffMode.PLACE_TOTEM;
        }

        int newIndex =
            (currentMode.ordinal()
                    + (event.getScrollDelta() > 0 ? 1 : -1)
                    + StaffMode.values().length)
                % StaffMode.values().length;
        StaffMode newMode = StaffMode.values()[newIndex];

        tag.putString("Mode", newMode.name());

        ModNetworking.sendToServer(new ChangeModePacket(newIndex));

        player.displayClientMessage(
            Component.literal(
                Component.translatable("message.furtotemsmod.mode_changed").getString()
                    + newMode.getDisplayName()),
            true);

        event.setCanceled(true);
      }
    }
  }

  private static StaffMode getModeFromTag(CompoundTag tag) {
    if (tag.contains(MODE_KEY)) {
      try {
        return StaffMode.valueOf(tag.getString(MODE_KEY));
      } catch (IllegalArgumentException ignored) {
      }
    }
    return StaffMode.PLACE_TOTEM;
  }

  public static Block getConfiguredBlock() {
      String blockId = Config.TOTEM_CONSUMED_BLOCK.get();
      Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
      return block != null ? block : Blocks.AIR;
  }

  private InteractionResult handlePlaceTotem(
      UseOnContext context, Level level, BlockPos pos, Player player) {
    if (!player.isShiftKeyDown()) return InteractionResult.FAIL;
    if (level.isClientSide) return InteractionResult.FAIL;
    ItemStack heldItem = context.getItemInHand();
    if (!(heldItem.getItem() instanceof TotemItem)) {
      player.displayClientMessage(
          Component.translatable("message.furtotemsmod.must_use_totem_staff"), true);
      return InteractionResult.FAIL;
    }

    TotemSavedData data = TotemSavedData.get((ServerLevel) level);
    double maxBigTotems = player.getAttributeValue(ModAttributes.BIG_TOTEM_COUNT.get());
    int currentBigTotems = data.getPlayerTotemCount(player.getUUID()).getBigTotems();
    if (currentBigTotems >= maxBigTotems) {
      player.displayClientMessage(
          Component.translatable("message.furtotemsmod.too_many_large_totems"), true);
      return InteractionResult.FAIL;
    }

    if (Config.PREVENT_TOTEM_NEAR_SPAWNER.get()) {
      int radius = Config.SPAWNER_CHECK_RADIUS.get();
      BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
      for (int dx = -radius; dx <= radius; dx++) {
        for (int dy = -radius; dy <= radius; dy++) {
          for (int dz = -radius; dz <= radius; dz++) {
            mutablePos.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
            if (level.getBlockState(mutablePos).getBlock() instanceof SpawnerBlock) {
              player.displayClientMessage(
                  Component.translatable("message.furtotemsmod.nearby_spawner"), true);
              return InteractionResult.FAIL;
            }
          }
        }
      }
    }

    UpgradableTotemBlockEntity.MaterialType initialType =
        UpgradableTotemBlockEntity.MaterialType.COPPER;
    Block requiredBlock = getConfiguredBlock();
    if (!player.getInventory().contains(requiredBlock.asItem().getDefaultInstance())) {
      player.displayClientMessage(
          Component.literal(
              Component.translatable("message.furtotemsmod.missing_required_block").getString()
                  + initialType.name()),
          true);
      return InteractionResult.FAIL;
    }

    BlockPos totemPos = pos.above();
    if (data.isOverlapping(totemPos, initialType.getRadius(), player.getUUID())) {
      player.displayClientMessage(
          Component.translatable("message.furtotemsmod.totem_overlaps_another_zone"), true);
      return InteractionResult.FAIL;
    }
    BlockState totemBlockState = ModBlocks.UPGRADABLE_TOTEM.get().defaultBlockState();

    level.setBlockAndUpdate(totemPos, totemBlockState);
    if (level.getBlockState(totemPos).is(ModBlocks.UPGRADABLE_TOTEM.get())
        && level.getBlockEntity(totemPos) instanceof UpgradableTotemBlockEntity totemEntity) {
      totemEntity.setOwner(player.getUUID());
      totemEntity.upgrade(initialType);

      data.addTotem(totemPos, player.getUUID(), initialType.getRadius(), "Upgradable");

      removeItemFromInventory(player, requiredBlock.asItem(), 1);

      updateTotemCoordinatesOnStaff(heldItem, level, player.getUUID());

      player.displayClientMessage(
          Component.literal(
              Component.translatable("message.furtotemsmod.totem_set_with_level").getString()
                  + initialType.name()),
          true);

      if (Config.PLAYER_TOTEM_DEBUFFS.get()) {
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 60, 1));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 60, 0));
      }
      return InteractionResult.SUCCESS;
    } else {
      level.removeBlock(totemPos, false);
      player.displayClientMessage(
          Component.translatable("message.furtotemsmod.failed_to_place_totem_entity"), true);
    }

    return InteractionResult.FAIL;
  }

  private InteractionResult handleRemoveTotem(Level level, BlockPos pos, Player player) {
    ServerLevel serverLevel = (ServerLevel) level;
    TotemSavedData data = TotemSavedData.get(serverLevel);
    if (!player.isShiftKeyDown()) return InteractionResult.FAIL;
    TotemSavedData.TotemData totemData = data.getTotemData(pos);
    if (totemData == null) {
      player.displayClientMessage(
          Component.translatable("message.furtotemsmod.no_totem_to_remove"), true);
      return InteractionResult.FAIL;
    }

    if (!totemData.getOwner().equals(player.getUUID())) {
      player.displayClientMessage(
          Component.translatable("message.furtotemsmod.not_owner_of_totem"), true);
      return InteractionResult.FAIL;
    }

    level.destroyBlock(pos, false);

    data.removeTotem(serverLevel ,pos);

    player.displayClientMessage(
        Component.translatable("message.furtotemsmod.totem_removed_successfully"), true);
    return InteractionResult.SUCCESS;
  }

  private InteractionResult handleAddMember(
      Level level, BlockPos pos, Player player, LivingEntity pInteractionTarget) {
    ServerLevel serverLevel = (ServerLevel) level;
    TotemSavedData data = TotemSavedData.get(serverLevel);
    if (!player.isShiftKeyDown()) return InteractionResult.FAIL;
    if (!(pInteractionTarget instanceof Player)) {
      player.displayClientMessage(Component.translatable("message.furtotemsmod.not_player"), true);
      return InteractionResult.FAIL;
    }
    if (data.isPlayerMember(player.getUUID(), pInteractionTarget.getUUID())) {
      player.displayClientMessage(
          Component.translatable("message.furtotemsmod.player_already_member"), true);
      return InteractionResult.FAIL;
    }

    data.addMemberToTotem(player.getUUID(), pInteractionTarget.getUUID());

    player.displayClientMessage(Component.translatable("message.furtotemsmod.adding_member"), true);
    return InteractionResult.SUCCESS;
  }

  public static void updateTotemCoordinatesOnStaff(ItemStack staff, Level level, UUID playerUUID) {
    if (!(level instanceof ServerLevel serverLevel)) return;

    TotemSavedData data = TotemSavedData.get(serverLevel);
    List<BlockPos> playerTotems = data.getAllTotemsOwnedBy(playerUUID);

    CompoundTag tag = staff.getOrCreateTag();
    ListTag coordinates = new ListTag();

    for (BlockPos pos : playerTotems) {
      CompoundTag coordinateTag = new CompoundTag();
      coordinateTag.putInt("x", pos.getX());
      coordinateTag.putInt("y", pos.getY());
      coordinateTag.putInt("z", pos.getZ());
      coordinates.add(coordinateTag);
    }

    tag.put("TotemCoordinates", coordinates);
    staff.setTag(tag);
  }

  private InteractionResult handleRemoveMember(
      Level level, BlockPos pos, Player player, LivingEntity pInteractionTarget) {
    ServerLevel serverLevel = (ServerLevel) level;
    TotemSavedData data = TotemSavedData.get(serverLevel);
    if (!player.isShiftKeyDown()) return InteractionResult.FAIL;
    if (!(pInteractionTarget instanceof Player)) {
      player.displayClientMessage(Component.translatable("message.furtotemsmod.not_player"), true);
      return InteractionResult.FAIL;
    }
    if (!data.isPlayerMember(player.getUUID(), pInteractionTarget.getUUID())) {
      player.displayClientMessage(
          Component.translatable("message.furtotemsmod.player_not_member"), true);
      return InteractionResult.FAIL;
    }

    data.removeMemberFromTotem(player.getUUID(), pInteractionTarget.getUUID());

    player.displayClientMessage(
        Component.translatable("message.furtotemsmod.removing_member"), true);
    return InteractionResult.SUCCESS;
  }

  private InteractionResult handleAddToBlacklist(
      Level level, BlockPos pos, Player player, LivingEntity pInteractionTarget) {
    ServerLevel serverLevel = (ServerLevel) level;
    TotemSavedData data = TotemSavedData.get(serverLevel);
    if (!player.isShiftKeyDown()) return InteractionResult.FAIL;
    if (!(pInteractionTarget instanceof Player)) {
      player.displayClientMessage(Component.translatable("message.furtotemsmod.not_player"), true);
      return InteractionResult.FAIL;
    }
    if (data.isBlacklisted(player.getUUID(), pInteractionTarget.getUUID())) {
      player.displayClientMessage(
          Component.translatable("message.furtotemsmod.player_already_in_blacklist"), true);
      return InteractionResult.FAIL;
    }

    data.addToBlacklist(player.getUUID(), pInteractionTarget.getUUID());

    player.displayClientMessage(
        Component.translatable("message.furtotemsmod.adding_to_blacklist"), true);
    return InteractionResult.SUCCESS;
  }

  private InteractionResult handleRemoveFromBlacklist(
      Level level, BlockPos pos, Player player, LivingEntity pInteractionTarget) {
    ServerLevel serverLevel = (ServerLevel) level;
    TotemSavedData data = TotemSavedData.get(serverLevel);
    if (!player.isShiftKeyDown()) return InteractionResult.FAIL;
    if (!(pInteractionTarget instanceof Player)) {
      player.displayClientMessage(Component.translatable("message.furtotemsmod.not_player"), true);
      return InteractionResult.FAIL;
    }
    if (!data.isBlacklisted(player.getUUID(), pInteractionTarget.getUUID())) {
      player.displayClientMessage(
          Component.translatable("message.furtotemsmod.player_not_in_blacklist"), true);
      return InteractionResult.FAIL;
    }
    data.removeFromBlacklist(player.getUUID(), pInteractionTarget.getUUID());

    player.displayClientMessage(
        Component.translatable("message.furtotemsmod.removing_from_blacklist"), true);
    return InteractionResult.SUCCESS;
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

  @Override
  public void inventoryTick(
      ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
    if (!pLevel.isClientSide || !(pEntity instanceof Player player)) return;
    if (player.getMainHandItem().getItem() instanceof TotemItem) {
      ClientTotemRadiusRender.getInstance().enableRadiusRendering();
    } else {
      ClientTotemRadiusRender.getInstance().disableRadiusRendering();
    }
  }

  @Override
  public void appendHoverText(
          ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
    ClientTotemSavedData clientData = ClientTotemSavedData.get();
    Map<BlockPos, TotemSavedData.TotemData> totemDataMap = clientData.getTotemDataMap();

    if (Screen.hasShiftDown()) {
      CompoundTag tag = stack.getOrCreateTag();
      StaffMode currentMode;
      try {
        currentMode = StaffMode.valueOf(tag.getString("Mode"));
      } catch (IllegalArgumentException | NullPointerException e) {
        currentMode = StaffMode.PLACE_TOTEM;
      }

      tooltip.add(
              Component.translatable("message.furtotemsmod.mode_description." + currentMode.name())
                      .withStyle(ChatFormatting.GRAY));

    } else {
      tooltip.add(
              Component.translatable("message.furtotemsmod.hold_shift_for_details")
                      .withStyle(ChatFormatting.DARK_GRAY));

      long bigTotemCount = totemDataMap.values().stream()
              .filter(data -> "Upgradable".equals(data.getType()))
              .count();

      tooltip.add(
              Component.translatable("message.furtotemsmod.big_totem_count", bigTotemCount)
                      .withStyle(ChatFormatting.AQUA));

      if (!totemDataMap.isEmpty()) {
        tooltip.add(
                Component.translatable("message.furtotemsmod.totem_locations")
                        .withStyle(ChatFormatting.GOLD));
        for (Map.Entry<BlockPos, TotemSavedData.TotemData> entry : totemDataMap.entrySet()) {
          if ("Upgradable".equals(entry.getValue().getType())) {
            tooltip.add(Component.literal(entry.getKey().toShortString()));
          }
        }
      } else {
        tooltip.add(
                Component.translatable("message.furtotemsmod.no_totems_found")
                        .withStyle(ChatFormatting.RED));
      }
    }
  }

}
