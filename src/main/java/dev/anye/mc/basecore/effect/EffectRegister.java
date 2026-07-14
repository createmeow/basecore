package dev.anye.mc.basecore.effect;

import dev.anye.mc.basecore.BaseCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EffectRegister {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, BaseCore.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> Disguise = MOB_EFFECTS.register("disguise", () -> new DisguiseEffect(MobEffectCategory.BENEFICIAL, 0x98D982));

    public static void reg(IEventBus eventBus){
        MOB_EFFECTS.register(eventBus);
    }
}