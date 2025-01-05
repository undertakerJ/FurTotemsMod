package net.undertaker.furtotemsmod.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.undertaker.furtotemsmod.blocks.blockentity.UpgradableTotemBlockEntity;
import net.undertaker.furtotemsmod.items.custom.TotemItem;


public class TotemBlockEntityRenderer implements BlockEntityRenderer<UpgradableTotemBlockEntity> {

  private static final TotemBlockEntityRenderer INSTANCE = new TotemBlockEntityRenderer();
  public static TotemBlockEntityRenderer getInstance() {
    return INSTANCE;
  }
  @Override
  public void render(UpgradableTotemBlockEntity upgradableTotemBlockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
    BlockPos pos = upgradableTotemBlockEntity.getBlockPos();
    RenderSystem.disableCull();
    RenderSystem.disableBlend();
    long time = Minecraft.getInstance().level.getGameTime();
    BeaconRenderer.renderBeaconBeam(
            poseStack,
            multiBufferSource,
            BeaconRenderer.BEAM_LOCATION,
            Minecraft.getInstance().getFrameTime(),
            1.0F,
            time,
            0,
            256,
            new float[] {0.0F, 0.5F, 1.0F},
            0.15F,
            0.2F
    );
    RenderSystem.enableBlend();
    RenderSystem.enableCull();
  }

  @Override
  public boolean shouldRenderOffScreen(UpgradableTotemBlockEntity pBlockEntity) {
    return true;
  }
  @Override
  public boolean shouldRender(UpgradableTotemBlockEntity pBlockEntity, Vec3 pCameraPos) {
    double distance = pCameraPos.distanceTo(Vec3.atCenterOf(pBlockEntity.getBlockPos()));
    return distance < 512;
  }

}
