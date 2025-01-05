package net.undertaker.furtotemsmod.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.undertaker.furtotemsmod.data.ClientTotemSavedData;
import net.undertaker.furtotemsmod.data.TotemSavedData;

@OnlyIn(Dist.CLIENT)
public class ClientTotemRadiusRender {
  private static final ClientTotemRadiusRender INSTANCE = new ClientTotemRadiusRender();
  private boolean isRenderingEnabled = false;

  public static ClientTotemRadiusRender getInstance() {
    return INSTANCE;
  }

  public void enableRadiusRendering() {
    isRenderingEnabled = true;
  }

  public void disableRadiusRendering() {
    isRenderingEnabled = false;
  }

  public void render() {
    if (!isRenderingEnabled) return;

    Minecraft mc = Minecraft.getInstance();
    Player player = mc.player;

    if (player == null) return;

    ClientTotemSavedData data = ClientTotemSavedData.get();
    for (Map.Entry<BlockPos, TotemSavedData.TotemData> entry : data.getTotemDataMap().entrySet()) {
      BlockPos pos = entry.getKey();
      int radius = entry.getValue().getRadius();
      Level level = mc.level;
      if(level == null) return;
      if (mc.level.dimension() != player.level.dimension()) return;
      renderRadiusParticles(level, pos, radius, player.tickCount);

    }
  }


  private void renderRadiusParticles(Level level, BlockPos pos, int radius, int tick) {
    ParticleOptions particleType = ParticleTypes.ENCHANT;
    for (double angle = tick; angle < 360 + tick; angle += 5) {
      double radians = Math.toRadians(angle);
      double x = pos.getX() + 0.5 + radius * Math.cos(radians);
      double z = pos.getZ() + 0.5 + radius * Math.sin(radians);
      double y = pos.getY() + 0.5;

      level.addParticle(particleType, x, y, z, 0, 0.05, 0);
    }
  }
}
