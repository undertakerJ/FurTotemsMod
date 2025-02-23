package net.undertaker.furtotemsmod.render;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.undertaker.furtotemsmod.blocks.ModBlockEntities;
import net.undertaker.furtotemsmod.blocks.blockentity.model.UpgradableTotemModel;

import static net.undertaker.furtotemsmod.FurTotemsMod.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(
                ModBlockEntities.UPGRADABLE_TOTEM.get(),
                TotemBlockEntityRenderer::new);
    }
    @SubscribeEvent
    public static void registerModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                UpgradableTotemModel.LAYER_LOCATION,
                UpgradableTotemModel::createBodyLayer
        );
    }
}