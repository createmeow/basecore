package dev.anye.mc.basecore.datagen.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.anye.mc.basecore.config.BasecoreConfig;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * 联动开关配方条件：当对应联动的配置项关闭时隐藏相关配方。
 * <p>使用方式：{@code {"type":"basecore:integration_enabled","integration":"create"}}
 */
public record IntegrationEnabledCondition(String integration) implements ICondition {
    public static final MapCodec<IntegrationEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("integration").forGetter(IntegrationEnabledCondition::integration)
            ).apply(instance, IntegrationEnabledCondition::new));

    @Override
    public boolean test(IContext context) {
        if ("create".equalsIgnoreCase(integration)) {
            return BasecoreConfig.isCreateIntegrationEnabled();
        }
        if ("cold_sweat".equalsIgnoreCase(integration)) {
            return BasecoreConfig.isColdSweatIntegrationEnabled();
        }
        return true;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
