package dev.anye.mc.basecore.event;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.entity.basecore.BasecoreBlockEntityRender;
import dev.anye.mc.basecore.entity.EntityTypeRegister;
import dev.anye.mc.basecore.menu.MenuTypeRegister;
import dev.anye.mc.basecore.screen.BaseCoreScreen;
import dev.anye.mc.basecore.screen.DefendScreen;
import dev.anye.mc.basecore.screen.HashChestScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = BaseCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvent {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        MenuScreens.register(MenuTypeRegister.BASECORE_MENU.get(), BaseCoreScreen::new);
        MenuScreens.register(MenuTypeRegister.DEFEND_MENU.get(), DefendScreen::new);
        MenuScreens.register(MenuTypeRegister.HashChest.get(), HashChestScreen::new);
    }
    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event){
        event.registerBlockEntityRenderer(BlockEntityRegister.BASECORE.get(), BasecoreBlockEntityRender::new);
        event.registerEntityRenderer(EntityTypeRegister.ElectromagneticPulseBomb.get(), ThrownItemRenderer::new);
    }
}
