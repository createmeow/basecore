package dev.anye.mc.basecore.item.module.basecore;

import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.item.module.BasecoreModuleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RangeModuleItemItem extends BasecoreModuleItem {
    public RangeModuleItemItem() {
        super(12,false);
    }
    @Override
    protected int range(int count) {
        return count *6;
    }

}
