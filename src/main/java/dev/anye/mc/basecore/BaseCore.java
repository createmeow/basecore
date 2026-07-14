package dev.anye.mc.basecore;

import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.BlockRegister;
import dev.anye.mc.basecore.config.BasecoreConfig;
import dev.anye.mc.basecore.datagen.condition.ComponentModeDisabledCondition;
import dev.anye.mc.basecore.effect.EffectRegister;
import dev.anye.mc.basecore.entity.EntityTypeRegister;
import dev.anye.mc.basecore.event.ClientForgeEvent;
import dev.anye.mc.basecore.event.ForgeEvent;
import dev.anye.mc.basecore.init.DataAttachmentInit;
import dev.anye.mc.basecore.item.CreativeTabs;
import dev.anye.mc.basecore.item.ItemRegister;
import dev.anye.mc.basecore.item.component.ItemComponentInit;
import dev.anye.mc.basecore.menu.MenuTypeRegister;
import dev.anye.mc.basecore.net.easy_net.EasyNetRegister;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(BaseCore.MOD_ID)
public class BaseCore {
    public static final String MOD_ID = "basecore";

    public BaseCore(IEventBus modEventBus) {
        // Load config
        BasecoreConfig.load();

        BlockRegister.reg(modEventBus);
        BlockEntityRegister.reg(modEventBus);
        ItemRegister.reg(modEventBus);
        CreativeTabs.reg(modEventBus);
        MenuTypeRegister.reg(modEventBus);
        EntityTypeRegister.reg(modEventBus);
        EffectRegister.reg(modEventBus);

        // Register data attachments & components
        DataAttachmentInit.reg(modEventBus);
        ItemComponentInit.reg(modEventBus);

        NeoForge.EVENT_BUS.register(ForgeEvent.class);
        NeoForge.EVENT_BUS.register(ClientForgeEvent.class);

        modEventBus.addListener(RegisterPayloadHandlersEvent.class, EasyNetRegister::register);

        // Register recipe condition codec for component mode
        modEventBus.addListener(RegisterEvent.class, event -> {
            event.register(ResourceKey.createRegistryKey(ResourceLocation.parse("neoforge:condition_codecs")),
                    ResourceLocation.parse("basecore:component_mode_disabled"),
                    () -> ComponentModeDisabledCondition.CODEC);
        });
    }
}
