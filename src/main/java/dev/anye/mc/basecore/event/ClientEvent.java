package dev.anye.mc.basecore.event;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.entity.basecore.BasecoreBlockEntityRender;
import dev.anye.mc.basecore.client.ItemUseProgressRenderer;
import dev.anye.mc.basecore.client.PlacementHudRenderer;
import dev.anye.mc.basecore.entity.EntityTypeRegister;
import dev.anye.mc.basecore.menu.MenuTypeRegister;
import dev.anye.mc.basecore.screen.BaseCoreScreen;
import dev.anye.mc.basecore.screen.BasecoreUpgradeScreen;
import dev.anye.mc.basecore.screen.DefendScreen;
import dev.anye.mc.basecore.screen.HashChestScreen;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = BaseCore.MOD_ID, value = Dist.CLIENT)
public class ClientEvent {
    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event)
    {
        event.register(MenuTypeRegister.BASECORE_MENU.get(), BaseCoreScreen::new);
        event.register(MenuTypeRegister.DEFEND_MENU.get(), DefendScreen::new);
        event.register(MenuTypeRegister.HashChest.get(), HashChestScreen::new);
        event.register(MenuTypeRegister.BASECORE_UPGRADE_MENU.get(), BasecoreUpgradeScreen::new);
    }
    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event){
        event.registerBlockEntityRenderer(BlockEntityRegister.BASECORE.get(), BasecoreBlockEntityRender::new);
        event.registerEntityRenderer(EntityTypeRegister.ElectromagneticPulseBomb.get(), ThrownItemRenderer::new);
    }
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        ItemUseProgressRenderer.register(event);
        PlacementHudRenderer.register(event);
    }
}