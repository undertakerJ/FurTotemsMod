package net.undertaker.furtotemsmod.attributes;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.undertaker.furtotemsmod.FurTotemsMod;

@Mod.EventBusSubscriber(modid = FurTotemsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModAttributes {
    public static final DeferredRegister<Attribute> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, FurTotemsMod.MOD_ID);

    public static void register(IEventBus eventBus){
        REGISTRY.register(eventBus);
    }

    public static final RegistryObject<Attribute> SMALL_TOTEM_COUNT = create("small_totem_count", 2,100d);

    public static final RegistryObject<Attribute> BIG_TOTEM_COUNT = create("big_totem_count", 1,50d);


    private static RegistryObject<Attribute> create(String name, double defaultValue, double maxValue) {
        String descriptionId = "attribute.%s.%s".formatted(FurTotemsMod.MOD_ID, name);
        return REGISTRY.register(
                name, () -> new RangedAttribute(descriptionId, defaultValue, 0d, maxValue).setSyncable(true));
    }

    @SubscribeEvent
    public static void attachAttributes(EntityAttributeModificationEvent event) {
        REGISTRY.getEntries().stream()
                .map(RegistryObject::get)
                .forEach(attribute -> {
                    event.getTypes()
                            .forEach(type -> event.add(type, attribute));
                });
    }
}
