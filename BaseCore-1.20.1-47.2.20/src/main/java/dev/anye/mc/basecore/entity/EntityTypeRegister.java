package dev.anye.mc.basecore.entity;

import dev.anye.mc.basecore.BaseCore;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityTypeRegister {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BaseCore.MOD_ID);

    public static final RegistryObject<EntityType<ElectromagneticPulseBomb>> ElectromagneticPulseBomb = ENTITIES.register("electromagnetic_pulse_bomb",
            () -> EntityType.Builder.<ElectromagneticPulseBomb>of(ElectromagneticPulseBomb::new, MobCategory.MISC)
                    .sized(0.5f,0.5f)
                    .clientTrackingRange(5)
                    .updateInterval(10)
                    .build("electromagnetic_pulse_bomb"));

    public static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.Builder<T> builder){
        return ENTITIES.register(name,() -> builder.build(name));
    }


    public static void reg(IEventBus eventBus){
        ENTITIES.register(eventBus);
    }
}
