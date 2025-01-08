package net.undertaker.furtotemsmod.items;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeTab {
  public static final CreativeModeTab FURTOTEMS_TAB =
      new CreativeModeTab("furtotemstab") {
        @Override
        public ItemStack makeIcon() {
          return new ItemStack(ModItems.UPGRADABLE_TOTEM.get());
        }
      };
}
