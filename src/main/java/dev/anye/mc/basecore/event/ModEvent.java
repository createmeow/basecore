package dev.anye.mc.basecore.event;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.net.NetReg;
import dev.anye.mc.basecore.net.NetRegUpgrade;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.bus.api.SubscribeEvent;

@EventBusSubscriber(modid = BaseCore.MOD_ID)
public class ModEvent {
    @SubscribeEvent
    public static void commonSetup(final FMLCommonSetupEvent event)
    {
        NetReg.registerNets();
        NetRegUpgrade.registerNets();
    }
}