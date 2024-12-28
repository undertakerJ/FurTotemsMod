package net.undertaker.furtotemsmod.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.undertaker.furtotemsmod.FurTotemsMod;
import net.undertaker.furtotemsmod.blocks.blockentity.UpgradableTotemBlockEntity;
import net.undertaker.furtotemsmod.blocks.custom.SmallTotemBlock;
import net.undertaker.furtotemsmod.blocks.custom.UpgradableTotemBlock;

public class ModBlocks {

  public static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(ForgeRegistries.BLOCKS, FurTotemsMod.MOD_ID);

  public static final RegistryObject<Block> SMALL_TOTEM =
      BLOCKS.register(
          "small_totem",
          () ->
              new SmallTotemBlock(
                  Block.Properties.of(Material.WOOD)
                      .strength(2.0F)
                      .noOcclusion()
                      .explosionResistance(999999)));

public static final RegistryObject<Block> UPGRADABLE_TOTEM =
      BLOCKS.register(
          "upgradable_totem",
          () ->
              new UpgradableTotemBlock(
                  Block.Properties.of(Material.STONE)
                      .strength(2.0F)
                      .noOcclusion()
                      .explosionResistance(999999)));

  public static void register(IEventBus eventBus) {
    BLOCKS.register(eventBus);
  }
}
