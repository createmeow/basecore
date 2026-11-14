package dev.anye.mc.basecore.item.component;

import dev.anye.mc.basecore.BaseCore;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
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

    /**
     * 标记物品应在苏式基地计算机（黑黄主题）界面中显示为黄色着色版材质
     * {@code 原材质名_yellow.png}。为 Unit 类型，仅作为标志存在。
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> SOVIET_YELLOW_COMPONENT = COMPONENTS.register("soviet_yellow", () ->
            DataComponentType.<Unit>builder()
                    .persistent(Unit.CODEC)
                    .networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
                    .build()
    );

    public static void reg(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }
}
