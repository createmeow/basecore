package dev.anye.mc.basecore.item.module;

import dev.anye.mc.basecore.block.entity.DefendBlockEntity;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class DefendBlockModule extends BaseModuleItem{
    private final boolean hasTick;
    public DefendBlockModule() {
        this(4,false);
    }
    public DefendBlockModule(boolean hasTick) {
        this(4,hasTick);
    }
    public DefendBlockModule(int maxLvl) {
        this(maxLvl,false);
    }
    public DefendBlockModule(int maxLvl,boolean hasTick) {
        super(maxLvl);
        this.hasTick = hasTick;
    }
    public final int getDamage(int count){
        return damage(fixLevel(count));
    }
    protected int damage(int lvl){
        return 0;
    }

    public boolean isHasTick() {
        return hasTick;
    }
    public void runTick(DefendBlockEntity baseCoreBlockEntity, Level level, BlockPos blockPos, BlockState blockState, int count){
        tick(baseCoreBlockEntity,level,blockPos,blockState,fixLevel(count));
    }
    public void tick(DefendBlockEntity baseCoreBlockEntity, Level level, BlockPos blockPos, BlockState blockState, int count){}
}
