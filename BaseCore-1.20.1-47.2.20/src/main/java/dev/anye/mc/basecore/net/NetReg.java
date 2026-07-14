package dev.anye.mc.basecore.net;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.net.easy_net.EasyNet;
import dev.anye.mc.basecore.net.easy_net.EasyNetRegister;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class NetReg {
    public static final DeferredRegister<EasyNet> EASY_NET = DeferredRegister.create(EasyNetRegister.KEY, BaseCore.MOD_ID);
    public static final RegistryObject<EasyNet> BasecoreNet = EASY_NET.register("basecore_block", BasecoreBlockNet::new);
    public static final RegistryObject<EasyNet> ActivityBasecoreNet = EASY_NET.register("activity_basecore", ActivityBasecoreNet::new);
    public static void reg(IEventBus eventBus){
        EASY_NET.register(eventBus);
    }
}
