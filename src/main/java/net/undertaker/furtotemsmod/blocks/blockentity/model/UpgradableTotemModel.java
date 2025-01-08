package net.undertaker.furtotemsmod.blocks.blockentity.model;// Made with Blockbench 4.11.0
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.undertaker.furtotemsmod.FurTotemsMod;

public class UpgradableTotemModel extends Model {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(FurTotemsMod.MOD_ID, "upgradable_totem_model"), "main");
	private final ModelPart bone;

	public UpgradableTotemModel(ModelPart root) {
		super(RenderType::entityCutout);
        this.bone = root.getChild("bone");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 21).addBox(-13.0F, -1.0F, 3.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(0, 12).addBox(-12.0F, -2.0F, 4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(16, 0).addBox(-9.0F, -9.0F, 7.25F, 2.0F, 7.0F, 1.5F, new CubeDeformation(0.0F))
		.texOffs(0, 8).addBox(-11.0F, -8.1F, 7.0F, 6.0F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 24.0F, -8.0F));

		PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 6).addBox(-1.3213F, -0.2286F, -1.1F, 1.0F, 1.0F, 2.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.8787F, -11.1464F, 8.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r2 = bone.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-0.75F, -2.0F, -0.991F, 1.5F, 4.0F, 1.99F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-6.7626F, -9.3839F, 7.995F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r3 = bone.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-0.75F, -2.0F, -0.992F, 1.5F, 4.0F, 1.99F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.2374F, -9.3839F, 7.995F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r4 = bone.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(7, 0).addBox(1.5F, -2.4727F, -1.1F, 1.5F, 1.5F, 2.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.0F, -7.7273F, 8.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r5 = bone.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(7, 0).addBox(1.5F, -2.4727F, -1.1F, 1.5F, 1.5F, 2.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -7.7273F, 8.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r6 = bone.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 6).addBox(0.3213F, -0.2286F, -1.1F, 1.0F, 1.0F, 2.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.1213F, -11.1464F, 8.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r7 = bone.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 6).addBox(0.3213F, 0.1464F, -1.1F, 1.0F, 1.0F, 2.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.1213F, -14.1464F, 8.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r8 = bone.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 0).addBox(-1.625F, -1.125F, -0.994F, 1.5F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, -10.3839F, 7.995F, 0.0F, 0.0F, 2.3562F));

		PartDefinition cube_r9 = bone.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 0).addBox(-1.625F, -1.125F, -0.994F, 1.5F, 4.0F, 1.99F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -11.3839F, 7.995F, 0.0F, 0.0F, 0.7854F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}


	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}