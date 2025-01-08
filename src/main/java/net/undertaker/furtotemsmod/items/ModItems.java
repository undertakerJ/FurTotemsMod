package net.undertaker.furtotemsmod.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.undertaker.furtotemsmod.FurTotemsMod;
import net.undertaker.furtotemsmod.blocks.ModBlocks;
import net.undertaker.furtotemsmod.items.custom.TotemItem;

public class ModItems {
  public static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(ForgeRegistries.ITEMS, FurTotemsMod.MOD_ID);

  public static final RegistryObject<Item> SMALL_TOTEM =
      ITEMS.register(
          "small_totem",
          () ->
              new BlockItem(
                  ModBlocks.SMALL_TOTEM.get(),
                  new Item.Properties().tab(ModCreativeTab.FURTOTEMS_TAB)));
  public static final RegistryObject<Item> UPGRADABLE_TOTEM =
      ITEMS.register(
          "upgradable_totem",
          () ->
              new BlockItem(
                  ModBlocks.UPGRADABLE_TOTEM.get(),
                  new Item.Properties().tab(ModCreativeTab.FURTOTEMS_TAB)));
  public static final RegistryObject<Item> WHITE_FOX_PLUSHIE =
      ITEMS.register(
          "white_fox_plushie",
          () ->
              new BlockItem(
                  ModBlocks.FOX_PLUSHIE.get(),
                  new Item.Properties()));

  public static final RegistryObject<Item> COPPER_STAFF_ITEM =
      ITEMS.register(
          "copper_staff_item",
          () -> new TotemItem(new Item.Properties().tab(ModCreativeTab.FURTOTEMS_TAB).stacksTo(1)));
  public static final RegistryObject<Item> IRON_STAFF_ITEM =
      ITEMS.register(
          "iron_staff_item",
          () -> new TotemItem(new Item.Properties().tab(ModCreativeTab.FURTOTEMS_TAB).stacksTo(1)));
  public static final RegistryObject<Item> GOLD_STAFF_ITEM =
      ITEMS.register(
          "gold_staff_item",
          () -> new TotemItem(new Item.Properties().tab(ModCreativeTab.FURTOTEMS_TAB).stacksTo(1)));
  public static final RegistryObject<Item> DIAMOND_STAFF_ITEM =
      ITEMS.register(
          "diamond_staff_item",
          () -> new TotemItem(new Item.Properties().tab(ModCreativeTab.FURTOTEMS_TAB).stacksTo(1)));
  public static final RegistryObject<Item> NETHERITE_STAFF_ITEM =
      ITEMS.register(
          "netherite_staff_item",
          () -> new TotemItem(new Item.Properties().tab(ModCreativeTab.FURTOTEMS_TAB).stacksTo(1)));

  public static final RegistryObject<Item> IRON_TOTEMIC_EMPOWERMENT =
      ITEMS.register(
          "iron_totemic_empowerment",
          () -> new Item(new Item.Properties().tab(ModCreativeTab.FURTOTEMS_TAB).stacksTo(1)));

  public static final RegistryObject<Item> GOLD_TOTEMIC_EMPOWERMENT =
      ITEMS.register(
          "gold_totemic_empowerment",
          () -> new Item(new Item.Properties().tab(ModCreativeTab.FURTOTEMS_TAB).stacksTo(1)));

  public static final RegistryObject<Item> DIAMOND_TOTEMIC_EMPOWERMENT =
      ITEMS.register(
          "diamond_totemic_empowerment",
          () -> new Item(new Item.Properties().tab(ModCreativeTab.FURTOTEMS_TAB).stacksTo(1)));

  public static final RegistryObject<Item> NETHERITE_TOTEMIC_EMPOWERMENT =
      ITEMS.register(
          "netherite_totemic_empowerment",
          () -> new Item(new Item.Properties().tab(ModCreativeTab.FURTOTEMS_TAB).stacksTo(1)));

  public static void register(IEventBus eventBus) {
    ITEMS.register(eventBus);
  }
}
