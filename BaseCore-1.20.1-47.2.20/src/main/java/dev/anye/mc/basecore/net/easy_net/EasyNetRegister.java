package dev.anye.mc.basecore.net.easy_net;

import dev.anye.mc.basecore.BaseCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class EasyNetRegister {
    public static final ResourceLocation KEY = new ResourceLocation(BaseCore.MOD_ID, "easy_net");
    public static final DeferredRegister<EasyNet> EASY_NET = DeferredRegister.create(KEY, BaseCore.MOD_ID);
    public static final Supplier<IForgeRegistry<EasyNet>> REGISTRY = EASY_NET.makeRegistry(RegistryBuilder::new);


    public static void register(IEventBus eventBus){
        EASY_NET.register(eventBus);
    }
}
