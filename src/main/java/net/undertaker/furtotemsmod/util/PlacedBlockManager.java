package net.undertaker.furtotemsmod.util;

import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.undertaker.furtotemsmod.FurConfig;
import net.undertaker.furtotemsmod.FurTotemsMod;
import net.undertaker.furtotemsmod.blocks.ModBlocks;
import net.undertaker.furtotemsmod.data.TotemSavedData;

@Mod.EventBusSubscriber(modid = FurTotemsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlacedBlockManager {

  public static void addBlock(Level level, BlockPos pos, TotemSavedData.TotemData totemData) {
    long gameTime = level.getGameTime();
    ServerLevel serverLevel = (ServerLevel) level;
    TotemSavedData data = TotemSavedData.get(serverLevel);
    if (totemData != null) {
      if (FurConfig.BLOCK_DESTROY_IN_ZONE.get()) {
        data.getPlacedBlocksInZone()
            .put(pos, new TotemSavedData.BlockInZoneEntry(totemData, gameTime));
      }
    } else {
      data.getPlacedBlocks().put(pos, gameTime);
    }
    data.setDirty();
  }

  public static void processBlocks(ServerLevel serverLevel) {
    long currentTime = serverLevel.getGameTime();
    List<BlockPos> toRemove = new ArrayList<>();
    TotemSavedData data = TotemSavedData.get(serverLevel);
    int breakDelay = FurConfig.BLOCK_BREAK_DELAY.get() * 20;

    for (Map.Entry<BlockPos, Long> entry : data.getPlacedBlocks().entrySet()) {
      BlockPos pos = entry.getKey();
      long placedTime = entry.getValue();
      if (currentTime - placedTime >= breakDelay && !isBlockInTotemRadius(serverLevel, pos)) {
          serverLevel.destroyBlock(pos, true);
          toRemove.add(pos);
        }
      if (!isBlockInTotemRadius(serverLevel, pos)) {
          double elapsedTime = currentTime - placedTime;
          int destroyStage = (int) Math.min(9, (elapsedTime / (double) breakDelay) * 9);
          serverLevel.destroyBlockProgress(pos.hashCode(), pos, destroyStage);
      }
    }
    toRemove.forEach(data.getPlacedBlocks()::remove);
    data.setDirty();
  }

  public static void processBlocksOutZone(ServerLevel serverLevel) {
    long currentTime = serverLevel.getGameTime();
    TotemSavedData data = TotemSavedData.get(serverLevel);
    int delayTicks = FurConfig.DELAY_BLOCK_DESTROY_IN_ZONE.get() * 20;

    List<BlockPos> toRemove = new ArrayList<>();
    for (Map.Entry<BlockPos, Long> entry : data.getPlacedBlocksOutZone().entrySet()) {
      BlockPos pos = entry.getKey();
      long placedTime = entry.getValue();
      if (isBlockInTotemRadius(serverLevel, pos)) {
        TotemSavedData.TotemData nearestTotemData = TotemSavedData.getNearestTotemData(serverLevel, pos);
        if (nearestTotemData != null) {
          serverLevel.destroyBlockProgress(pos.hashCode(), pos, -1);
          data.getPlacedBlocksOutZone().remove(pos);
          data.getPlacedBlocksInZone().put(pos, new TotemSavedData.BlockInZoneEntry(nearestTotemData, currentTime));
        }
        continue;
      }
      if (!isBlockInTotemRadius(serverLevel, pos)) {
        if (currentTime - placedTime >= delayTicks) {
          serverLevel.destroyBlock(pos, true);
          toRemove.add(pos);
        } else {
          double elapsedTime = currentTime - placedTime;
          int destroyStage = (int) Math.min(9, (elapsedTime / (double) delayTicks) * 9);
          serverLevel.destroyBlockProgress(pos.hashCode(), pos, destroyStage);
        }
      }
    }
    toRemove.forEach(data.getPlacedBlocksOutZone()::remove);
    data.setDirty();
  }

  public static void onTotemDestroyed(ServerLevel level, TotemSavedData.TotemData destroyedTotem) {
    if (!FurConfig.BLOCK_DESTROY_IN_ZONE.get()) return;
    TotemSavedData data = TotemSavedData.get(level);
    long currentTime = level.getGameTime();

    for (Map.Entry<BlockPos, TotemSavedData.BlockInZoneEntry> entry : new ArrayList<>(data.getPlacedBlocksInZone().entrySet())) {
      BlockPos pos = entry.getKey();

      data.getPlacedBlocksInZone().remove(pos);
      data.getPlacedBlocksOutZone().put(pos, currentTime);
    }
    restoreDelayedTasks(level);
    data.setDirty();
  }


  private static boolean isBlockInTotemRadius(ServerLevel level, BlockPos pos) {
    TotemSavedData data = TotemSavedData.get(level);
    BlockPos nearestTotem = data.getNearestTotem(pos);
    if (nearestTotem != null) {
      double radius = data.getTotemData(nearestTotem).getRadius();
      return nearestTotem.distSqr(pos) <= Math.pow(radius, 2);
    }
    return false;
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public static void levelTick(TickEvent.LevelTickEvent event) {
    if (event.phase != TickEvent.Phase.END) return;
    if (event.level instanceof ServerLevel level) {
      processBlocks(level);
      processBlocksOutZone(level);
    }
  }

  @SubscribeEvent
  public static void decayBlocks(BlockEvent.EntityPlaceEvent event) {
    if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
    String currentDimension = serverLevel.dimension().location().toString();
    if (FurConfig.DISABLE_BLOCK_BREAK_DIMENSIONS.get().contains(currentDimension)) return;

    BlockPos pos = event.getPos();
    Block block = event.getPlacedBlock().getBlock();
    Set<Block> blacklistedDecayBlocks = getBlacklistedDecayBlocks();
    if (blacklistedDecayBlocks.contains(block)) return;
    if (isCorrectBlock(block.defaultBlockState())) {
      TotemSavedData data = TotemSavedData.get(serverLevel);
      BlockPos nearestTotem = data.getNearestTotem(pos);

      TotemSavedData.TotemData totemData =
          nearestTotem != null
                  && nearestTotem.distSqr(pos)
                      <= Math.pow(data.getTotemData(nearestTotem).getRadius(), 2)
              ? data.getTotemData(nearestTotem)
              : null;
      if (FurConfig.CREATIVE_IGNORE_TOTEMS.get()
          && event.getEntity() instanceof Player player
          && player.isCreative()) {
        return;
      }
      addBlock(serverLevel, pos, totemData);
    }
  }

  public static void restoreDelayedTasks(ServerLevel level) {
    TotemSavedData data = TotemSavedData.get(level);
    long currentTime = level.getGameTime();
    int delayTicks = FurConfig.DELAY_BLOCK_DESTROY_IN_ZONE.get() * 20;

    for (Map.Entry<BlockPos, Long> entry : new ArrayList<>(data.getPlacedBlocksOutZone().entrySet())) {
      BlockPos pos = entry.getKey();
      long placedTime = entry.getValue();
      long elapsed = currentTime - placedTime;
      int remaining = (int) Math.max(delayTicks - elapsed, 0);
      if (remaining <= 0 && !isBlockInTotemRadius(level, pos)) {
        level.destroyBlock(pos, true);
        data.getPlacedBlocksOutZone().remove(pos);
      }  if (remaining > 0 && !isBlockInTotemRadius(level, pos)) {
        processBlocksOutZone(level);
        break;
      }
    }
    data.setDirty();
  }

  public static Set<Block> getBlacklistedDecayBlocks() {
    return FurConfig.BLACKLIST_DECAY_BLOCKS.get().stream()
        .map(blockId -> ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId)))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  private static boolean isCorrectBlock(BlockState state) {
    return !state.is(BlockTags.FIRE) &&
            !state.is(BlockTags.CROPS)
        && !state.is(Blocks.BEEHIVE)
        && !state.is(Blocks.BEE_NEST)
        && !state.is(Blocks.COMMAND_BLOCK)
        && !state.is(Blocks.CHAIN_COMMAND_BLOCK)
        && !state.is(Blocks.REPEATING_COMMAND_BLOCK)
        && !state.is(Blocks.STRUCTURE_BLOCK)
        && !state.is(Blocks.JIGSAW)
        && !state.is(Blocks.SPAWNER)
        && !state.is(BlockTags.SAPLINGS)
        && !state.is(Blocks.BEDROCK)
        && !state.is(Blocks.END_PORTAL_FRAME)
        && !state.is(Blocks.END_GATEWAY)
        && !state.is(Blocks.BARRIER)
        && !state.is(BlockTags.FLOWERS)
        && !state.is(Blocks.TORCH)
        && !state.is(Blocks.SOUL_TORCH)
        && !state.is(Blocks.WALL_TORCH)
        && !state.is(Blocks.LANTERN)
        && !state.is(Blocks.SOUL_WALL_TORCH)
        && !state.is(Blocks.TALL_GRASS)
        && !state.is(Blocks.WATER)
        && !state.is(Blocks.LAVA)
        && !state.is(Blocks.FARMLAND)
        && !state.is(BlockTags.CORAL_PLANTS)
        && !state.is(Blocks.FERN)
        && !state.is(Blocks.LARGE_FERN)
        && !state.is(Blocks.VINE)
        && !state.is(Blocks.DEAD_BUSH)
        && !state.is(Blocks.CACTUS)
        && !state.is(Blocks.SUGAR_CANE)
        && !state.is(Blocks.BAMBOO)
        && !state.is(Blocks.WHEAT)
        && !state.is(Blocks.CARROTS)
        && !state.is(Blocks.POTATOES)
        && !state.is(Blocks.BEETROOTS)
        && !state.is(Blocks.MELON_STEM)
        && !state.is(Blocks.PUMPKIN_STEM)
        && !state.is(Blocks.SWEET_BERRY_BUSH)
        && !state.is(Blocks.CAVE_VINES_PLANT)
        && !state.is(Blocks.CAVE_VINES)
        && !state.is(Blocks.KELP)
        && !state.is(Blocks.SEAGRASS)
        && !state.is(Blocks.TALL_SEAGRASS)
        && !state.is(Blocks.MUSHROOM_STEM)
        && !state.is(Blocks.BROWN_MUSHROOM)
        && !state.is(Blocks.RED_MUSHROOM)
        && !state.is(ModBlocks.UPGRADABLE_TOTEM.get())
        && !state.is(ModBlocks.SMALL_TOTEM.get());
  }
}
