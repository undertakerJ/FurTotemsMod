package net.undertaker.furtotemsmod.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.undertaker.furtotemsmod.FurTotemsMod;
import net.undertaker.furtotemsmod.blocks.custom.SmallTotemBlock;
import net.undertaker.furtotemsmod.blocks.custom.UpgradableTotemBlock;
import net.undertaker.furtotemsmod.blocks.custom.WhiteFoxPlushie;

public class ModBlocks {

  public static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(ForgeRegistries.BLOCKS, FurTotemsMod.MOD_ID);

  public static final RegistryObject<Block> SMALL_TOTEM =
      BLOCKS.register(
          "small_totem",
          () ->
              new SmallTotemBlock(
                  Block.Properties.copy(Blocks.OAK_WOOD)
                      .strength(2.0F)
                      .noOcclusion()
                      .explosionResistance(999999)));

public static final RegistryObject<Block> UPGRADABLE_TOTEM =
      BLOCKS.register(
          "upgradable_totem",
          () ->
              new UpgradableTotemBlock(
                  Block.Properties.copy(Blocks.BEDROCK)
                      .strength(2.0F)
                      .noOcclusion()
                      .explosionResistance(999999)));
  public static final RegistryObject<Block> FOX_PLUSHIE =
      BLOCKS.register(
          "white_fox_plushie", () -> new WhiteFoxPlushie(Block.Properties.copy(Blocks.WHITE_WOOL).noOcclusion()));

  public static void register(IEventBus eventBus) {
    BLOCKS.register(eventBus);
  }
}
