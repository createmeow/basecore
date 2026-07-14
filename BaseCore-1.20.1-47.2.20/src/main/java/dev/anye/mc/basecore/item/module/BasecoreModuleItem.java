package dev.anye.mc.basecore.item.module;

import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BasecoreModuleItem extends BaseModuleItem {
    private final boolean hasTick;

    public BasecoreModuleItem() {
        this(4,false);
    }
    public BasecoreModuleItem(boolean hasTick) {
        this(4,hasTick);
    }
    public BasecoreModuleItem(int maxLvl) {
        this(maxLvl,false);
    }
    public BasecoreModuleItem(int maxLvl,boolean hasTick) {
        super(maxLvl);
        this.hasTick = hasTick;
    }
    public boolean isHasTick() {
        return hasTick;
    }

    public void runTick(BaseCoreBlockEntity baseCoreBlockEntity, Level level, BlockPos blockPos, BlockState blockState, int count){
        tick(baseCoreBlockEntity,level,blockPos,blockState,fixLevel(count));
    }
    protected void tick(BaseCoreBlockEntity baseCoreBlockEntity, Level level, BlockPos blockPos, BlockState blockState, int count){}

}
