package dev.anye.mc.basecore.effect;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.item.BasecoreItem;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EffectRegister {
    public static final DeferredRegister<MobEffect> ITEMS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, BaseCore.MOD_ID);

    public static final RegistryObject<MobEffect> Disguise = ITEMS.register("disguise",DisguiseEffect::new);



    public static void reg(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
