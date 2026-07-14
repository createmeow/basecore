package dev.anye.mc.basecore.datagen.condition;

import com.mojang.serialization.MapCodec;
import dev.anye.mc.basecore.config.BasecoreConfig;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Recipe condition that returns false (recipe disabled) when the game is in component mode.
 * Used to hide module crafting recipes in component mode.
 */
public record ComponentModeDisabledCondition() implements ICondition {
    public static final MapCodec<ComponentModeDisabledCondition> CODEC = MapCodec.unit(new ComponentModeDisabledCondition());

    @Override
    public boolean test(IContext context) {
        return !BasecoreConfig.isComponentMode();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
