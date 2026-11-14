package dev.anye.mc.basecore.datagen.condition;

import com.mojang.serialization.MapCodec;
import dev.anye.mc.basecore.config.BasecoreConfig;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * 苏式基地计算机玩法开关配方条件：当 {@code balance.soviet_computer_enabled} 为 false 时
 * 所有相关配方都不会加载。
 * <p>使用方式：{@code "neoforge:conditions":[{"type":"basecore:soviet_computer_enabled"}]}
 */
public record SovietComputerEnabledCondition() implements ICondition {
    public static final SovietComputerEnabledCondition INSTANCE = new SovietComputerEnabledCondition();
    public static final MapCodec<SovietComputerEnabledCondition> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public boolean test(IContext context) {
        return BasecoreConfig.isSovietComputerEnabled();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
