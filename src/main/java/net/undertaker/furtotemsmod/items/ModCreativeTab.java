package net.undertaker.furtotemsmod.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.undertaker.furtotemsmod.FurTotemsMod;
import net.undertaker.furtotemsmod.blocks.ModBlocks;

public class ModCreativeTab  {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FurTotemsMod.MOD_ID);


    public static final RegistryObject<CreativeModeTab> FURTOTEMS_TAB =
            CREATIVE_MODE_TABS.register(
                    "furtotemstab",
                    () ->
                            CreativeModeTab.builder()
                                    .icon(() -> new ItemStack(ModItems.DIAMOND_STAFF_ITEM.get()))
                                    .title(Component.translatable("itemGroup.furtotemstab"))
                                    .displayItems(
                                            (itemDisplayParameters, output) -> {
                                                // items
                                                ModItems.ITEMS.getEntries().stream()
                                                        .map(RegistryObject::get)
                                                        .forEach(output::accept);
                                                // blocks
                                                ModBlocks.BLOCKS.getEntries().stream()
                                                        .map(RegistryObject::get)
                                                        .forEach(output::accept);
                                            })
                                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
