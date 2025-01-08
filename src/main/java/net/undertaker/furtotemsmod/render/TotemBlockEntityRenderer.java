package net.undertaker.furtotemsmod.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.undertaker.furtotemsmod.FurTotemsMod;
import net.undertaker.furtotemsmod.blocks.blockentity.UpgradableTotemBlockEntity;
import net.undertaker.furtotemsmod.blocks.blockentity.model.UpgradableTotemModel;
import net.undertaker.furtotemsmod.items.custom.TotemItem;


public class TotemBlockEntityRenderer implements BlockEntityRenderer<UpgradableTotemBlockEntity> {
  private final UpgradableTotemModel model;

  public TotemBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    this.model = new UpgradableTotemModel(context.bakeLayer(UpgradableTotemModel.LAYER_LOCATION));
  }

  @Override
  public void render(UpgradableTotemBlockEntity upgradableTotemBlockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
    if (Minecraft.getInstance().player == null) return;
      UpgradableTotemBlockEntity.MaterialType materialType = upgradableTotemBlockEntity.getMaterialType();
      ResourceLocation texture = new ResourceLocation(FurTotemsMod.MOD_ID, materialType.getTexture());

      poseStack.pushPose();
      poseStack.translate(0.5, 1.5, 0.5);
      poseStack.scale(-1.0F, -1.0F, 1.0F);
      VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutout(texture));
      model.renderToBuffer(poseStack, vertexConsumer, i, i1, 1.0F, 1.0F, 1.0F, 1.0F);
      poseStack.popPose();

    if(!(Minecraft.getInstance().player.getMainHandItem().getItem() instanceof TotemItem)) return;
    BeaconRenderer.renderBeaconBeam(
            poseStack,
            multiBufferSource,
            BeaconRenderer.BEAM_LOCATION,
            Minecraft.getInstance().getFrameTime(),
            1.0F,
            upgradableTotemBlockEntity.getLevel().getGameTime(),
            1,
            256,
            new float[] {0.0F, 0.5F, 1.0F},
            0.15F,
            0.2F
    );
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
