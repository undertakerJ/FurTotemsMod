package net.undertaker.furtotemsmod.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.undertaker.furtotemsmod.FurTotemsMod;
import net.undertaker.furtotemsmod.blocks.blockentity.UpgradableTotemBlockEntity;

@OnlyIn(Dist.CLIENT)
public class TotemEntityRenderer implements BlockEntityRenderer<UpgradableTotemBlockEntity> {

  @Override
  public void render(UpgradableTotemBlockEntity upgradableTotemBlockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {

  }
}
