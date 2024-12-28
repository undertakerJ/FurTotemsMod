package net.undertaker.furtotemsmod;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.undertaker.furtotemsmod.attributes.ModAttributes;
import net.undertaker.furtotemsmod.blocks.ModBlockEntities;
import net.undertaker.furtotemsmod.blocks.ModBlocks;
import net.undertaker.furtotemsmod.items.ModItems;
import org.slf4j.Logger;

@Mod(FurTotemsMod.MOD_ID)
public class FurTotemsMod {
  public static final String MOD_ID = "furtotemsmod";

  public FurTotemsMod(FMLJavaModLoadingContext context) {
    IEventBus modEventBus = context.getModEventBus();

    modEventBus.addListener(this::commonSetup);

    ModBlocks.register(modEventBus);
    ModItems.register(modEventBus);
    ModBlockEntities.register(modEventBus);
    ModAttributes.register(modEventBus);

    MinecraftForge.EVENT_BUS.register(this);

    context.registerConfig(
        ModConfig.Type.SERVER, Config.SERVER_CONFIG, "furtotemsmod-config-server.toml");
  }
  public static final Logger LOGGER = LogUtils.getLogger();
  private void commonSetup(final FMLCommonSetupEvent event) {}

  @SubscribeEvent
  public void onServerStarting(ServerStartingEvent event) {}

  @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
  public static class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {}
  }
}
