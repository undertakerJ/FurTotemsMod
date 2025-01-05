package net.undertaker.furtotemsmod.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.undertaker.furtotemsmod.FurTotemsMod;
import net.undertaker.furtotemsmod.blocks.blockentity.UpgradableTotemBlockEntity;
import net.undertaker.furtotemsmod.data.ClientTotemSavedData;

@Mod.EventBusSubscriber(modid = FurTotemsMod.MOD_ID, value = Dist.CLIENT)
public class ClientRenderEvents {
    @SubscribeEvent
    public static void onRenderWorldLast(RenderLevelLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        if (player == null) return;
        ClientTotemSavedData data = ClientTotemSavedData.get();


        ClientTotemRadiusRender.getInstance().render();
    }
}
