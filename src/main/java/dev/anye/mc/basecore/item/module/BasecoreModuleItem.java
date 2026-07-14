package dev.anye.mc.basecore.item.module;

import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public abstract class BasecoreModuleItem extends BaseModuleItem {
    private final boolean hasTick;

    public BasecoreModuleItem() {
        this(4, false);
    }
    public BasecoreModuleItem(boolean hasTick) {
        this(4, hasTick);
    }
    public BasecoreModuleItem(int maxLvl) {
        this(maxLvl, false);
    }
    public BasecoreModuleItem(int maxLvl, boolean hasTick) {
        super(maxLvl);
        this.hasTick = hasTick;
    }
    public boolean isHasTick() {
        return hasTick;
    }

    public void runTick(BaseCoreBlockEntity baseCoreBlockEntity, Level level, BlockPos blockPos, BlockState blockState, int count){
        tick(baseCoreBlockEntity, level, blockPos, blockState, fixLevel(count));
    }
    protected void tick(BaseCoreBlockEntity baseCoreBlockEntity, Level level, BlockPos blockPos, BlockState blockState, int count){}

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltip, TooltipFlag pFlag) {
        // Module function description
        String descKey = pStack.getDescriptionId() + ".desc";
        Component desc = Component.translatable(descKey);
        // Only add if translation exists
        if (!desc.getString().equals(descKey)) {
            pTooltip.add(desc);
            pTooltip.add(Component.literal(""));
        }
        pTooltip.add(Component.translatable("tooltip.basecore.module.applicable"));
        int bcMax = getMaxLevel();
        if (bcMax > 0) {
            pTooltip.add(Component.literal("  - ").append(Component.translatable("block.basecore.basecore")).append(" ").append(String.valueOf(bcMax)).append(Component.translatable("tooltip.basecore.module.piece")));
        }
        int defMax = getDefendMaxCount();
        if (defMax > 0) {
            pTooltip.add(Component.literal("  - ").append(Component.translatable("block.basecore.defend")).append(" ").append(String.valueOf(defMax)).append(Component.translatable("tooltip.basecore.module.piece")));
        }
        super.appendHoverText(pStack, pContext, pTooltip, pFlag);
    }
}