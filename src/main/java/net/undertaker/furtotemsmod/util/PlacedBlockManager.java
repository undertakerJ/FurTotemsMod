package net.undertaker.furtotemsmod.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.undertaker.furtotemsmod.Config;
import net.undertaker.furtotemsmod.FurTotemsMod;
import net.undertaker.furtotemsmod.data.TotemSavedData;

@Mod.EventBusSubscriber(modid = FurTotemsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlacedBlockManager {

  private static final Map<BlockPos, Long> placedBlocks = new HashMap<>();

  public static void addBlock(Level level, BlockPos pos) {
    long gameTime = level.getGameTime();
    placedBlocks.put(pos, gameTime);
  }

  public static void processBlocks(Level level) {
    long currentTime = level.getGameTime();
    List<BlockPos> toRemove = new ArrayList<>();

    int breakDelay = Config.BLOCK_BREAK_DELAY.get() * 20;

    for (Map.Entry<BlockPos, Long> entry : placedBlocks.entrySet()) {
      BlockPos pos = entry.getKey();
      long placedTime = entry.getValue();

      if (currentTime - placedTime >= breakDelay) {
        ServerLevel serverLevel = (ServerLevel) level;
        TotemSavedData data = TotemSavedData.get(serverLevel);

        BlockPos nearestTotem = data.getNearestTotem(pos);
        if (nearestTotem == null
            || nearestTotem.distSqr(pos)
                > Math.pow(data.getTotemData(nearestTotem).getRadius(), 2)) {
          level.destroyBlock(pos, true);
          toRemove.add(pos);
        }
      } else {
        ServerLevel serverLevel = (ServerLevel) level;
        TotemSavedData data = TotemSavedData.get(serverLevel);

        BlockPos nearestTotem = data.getNearestTotem(pos);
        if (nearestTotem == null
            || nearestTotem.distSqr(pos)
                > Math.pow(data.getTotemData(nearestTotem).getRadius(), 2)) {
          double elapsedTime = currentTime - placedTime;
          int destroyStage = (int) Math.min(9, (elapsedTime / breakDelay) * 9);

          level.destroyBlockProgress(pos.hashCode(), pos, destroyStage);
        }
      }
    }

    toRemove.forEach(placedBlocks::remove);
  }



  public static void destroyBlocksInRadius(ServerLevel level, BlockPos totemPos, int radius) {
    List<BlockPos> blocksToDestroy = new ArrayList<>();

    for (BlockPos pos : placedBlocks.keySet()) {
      if (pos.distSqr(totemPos) <= radius * radius) {
        blocksToDestroy.add(pos);
      }
    }

    for (BlockPos pos : blocksToDestroy) {
      level.destroyBlock(pos, true);
      placedBlocks.remove(pos);
    }
  }

  @SubscribeEvent
  public static void levelTick(TickEvent.LevelTickEvent event) {
    if (event.level.isClientSide() || event.phase != TickEvent.Phase.END) return;

    Level level = event.level;
    processBlocks(level);
  }

  @SubscribeEvent
  public static void decayBlocks(BlockEvent.EntityPlaceEvent event) {
    if (event.getLevel().isClientSide()) return;

    Level level = (Level) event.getLevel();
    BlockPos pos = event.getPos();
    Block block = event.getPlacedBlock().getBlock();

  if (isCorrectBlock(block.defaultBlockState())) {
      addBlock(level, pos);
    }
  }

  private static boolean isCorrectBlock(BlockState state) {
    return !state.is(BlockTags.FIRE)
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
        && !state.is(Blocks.RED_MUSHROOM);
  }
}
