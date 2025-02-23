package net.undertaker.furtotemsmod.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.undertaker.furtotemsmod.data.TotemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FlowingFluid.class)
public class FlowingFluidBlockMixin {
  @Inject(
      method =
          "spread(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/FluidState;)V",
      at = @At("HEAD"),
      cancellable = true)
  public void spread(LevelAccessor level, BlockPos pos, FluidState state, CallbackInfo ci) {
      if (!(level instanceof ServerLevel serverLevel)) return;

      TotemSavedData data = TotemSavedData.get(serverLevel);
      BlockPos nearestTotem = data.getNearestTotem(pos);

      if (nearestTotem != null) {
          TotemSavedData.TotemData totemData = data.getTotemData(nearestTotem);
          if (totemData != null) {
              double radius = totemData.getRadius();
              double radiusSquared = radius * radius;
              double currentDistanceSquared = nearestTotem.distSqr(pos);

              if (currentDistanceSquared <= radiusSquared) {
                  for (Direction direction : Direction.values()) {
                      BlockPos targetPos = pos.relative(direction);
                      double targetDistanceSquared = nearestTotem.distSqr(targetPos);

                      if (furTotemsMod$isOnBorder(targetDistanceSquared, radiusSquared) || targetDistanceSquared > radiusSquared) {
                          ci.cancel();
                          return;
                      }
                  }
              }
          }
      }
  }

    @Unique
    private boolean furTotemsMod$isOnBorder(double distanceSquared, double radiusSquared) {
        return Math.abs(distanceSquared - radiusSquared) <= 0.5;
    }

}
