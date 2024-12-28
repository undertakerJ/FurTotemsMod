package net.undertaker.furtotemsmod.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.undertaker.furtotemsmod.Config;
import net.undertaker.furtotemsmod.FurTotemsMod;

@Mod.EventBusSubscriber(modid = FurTotemsMod.MOD_ID)
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
                || nearestTotem.distSqr(pos) > Math.pow(data.getTotemData(nearestTotem).getRadius(), 2)) {

          level.destroyBlock(pos, true);
          toRemove.add(pos);
        }
      } else {
        ServerLevel serverLevel = (ServerLevel) level;
        TotemSavedData data = TotemSavedData.get(serverLevel);

        BlockPos nearestTotem = data.getNearestTotem(pos);
        if (nearestTotem == null
                || nearestTotem.distSqr(pos) > Math.pow(data.getTotemData(nearestTotem).getRadius(), 2)) {
        double elapsedTime = currentTime - placedTime;
        int destroyStage = (int) Math.min(9, (elapsedTime / breakDelay) * 9);

        level.destroyBlockProgress(pos.hashCode(), pos, destroyStage);
      }}
    }

    toRemove.forEach(placedBlocks::remove);
  }



  @SubscribeEvent
  public static void levelTick(TickEvent.LevelTickEvent event) {
    if (event.level.isClientSide() || event.phase != TickEvent.Phase.END) return;

    Level level = event.level;
    PlacedBlockManager.processBlocks(level);
  }
}
