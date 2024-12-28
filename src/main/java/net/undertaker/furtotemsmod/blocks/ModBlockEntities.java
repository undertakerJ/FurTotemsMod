package net.undertaker.furtotemsmod.blocks;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.undertaker.furtotemsmod.FurTotemsMod;
import net.undertaker.furtotemsmod.blocks.blockentity.SmallTotemBlockEntity;
import net.undertaker.furtotemsmod.blocks.blockentity.UpgradableTotemBlockEntity;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, FurTotemsMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<SmallTotemBlockEntity>> SMALL_TOTEM = BLOCK_ENTITIES.register("small_totem",
            () -> BlockEntityType.Builder.of(SmallTotemBlockEntity::new, ModBlocks.SMALL_TOTEM.get()).build(null));

public static final RegistryObject<BlockEntityType<UpgradableTotemBlockEntity>> UPGRADABLE_TOTEM = BLOCK_ENTITIES.register("upgradable_totem",
            () -> BlockEntityType.Builder.of(UpgradableTotemBlockEntity::new, ModBlocks.UPGRADABLE_TOTEM.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
