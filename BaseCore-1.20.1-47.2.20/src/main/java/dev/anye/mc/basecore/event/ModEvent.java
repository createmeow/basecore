package dev.anye.mc.basecore.event;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.net.Net;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = BaseCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvent {
    @SubscribeEvent
    public static void commonSetup(final FMLCommonSetupEvent event)
    {
        Net.reg();
    }
}
