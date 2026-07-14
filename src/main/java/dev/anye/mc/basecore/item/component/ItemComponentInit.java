package dev.anye.mc.basecore.item.component;

import dev.anye.mc.basecore.BaseCore;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemComponentInit {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, BaseCore.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PartBundleComponent>> PART_BUNDLE_COMPONENT = COMPONENTS.register("part_bundle", () ->
            DataComponentType.<PartBundleComponent>builder()
                    .persistent(PartBundleComponent.CODEC)
                    .networkSynchronized(PartBundleComponent.STREAM_CODEC)
                    .build()
    );

    public static void reg(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }
}
