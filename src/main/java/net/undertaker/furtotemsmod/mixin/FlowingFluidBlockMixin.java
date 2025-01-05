package net.undertaker.furtotemsmod.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.undertaker.furtotemsmod.data.TotemSavedData;
import org.spongepowered.asm.mixin.Mixin;
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
            if (totemData != null && nearestTotem.distSqr(pos) <= Math.pow(totemData.getRadius(), 2)) {

                ci.cancel();

            }
        }
    }

}
