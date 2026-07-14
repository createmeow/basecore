package dev.anye.mc.basecore.entity;

import dev.anye.mc.basecore.BaseCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EntityTypeRegister {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, BaseCore.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ElectromagneticPulseBomb>> ElectromagneticPulseBomb = ENTITY_TYPES.register("electromagnetic_pulse_bomb", () -> EntityType.Builder.<ElectromagneticPulseBomb>of(ElectromagneticPulseBomb::new, MobCategory.MISC).sized(0.5f,0.5f).clientTrackingRange(100).build("electromagnetic_pulse_bomb"));

    public static void reg(IEventBus eventBus){
        ENTITY_TYPES.register(eventBus);
    }
}