package dev.anye.mc.basecore.item;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.block.BlockRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BaseCore.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("nu_tab", () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(BlockRegister.BASE_CORE.get()))
            .title(Component.translatable("create_tab.basecore.all"))
            .displayItems((pParameters, pOutput) -> {
                pOutput.accept(BlockRegister.BASE_CORE.get());
                ItemRegister.ITEMS.getEntries().forEach(itemRegistryObject -> pOutput.accept(itemRegistryObject.get()));
            })
            .build());

    public static void reg(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}