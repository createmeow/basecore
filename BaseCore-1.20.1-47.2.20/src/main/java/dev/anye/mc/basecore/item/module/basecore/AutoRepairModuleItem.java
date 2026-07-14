package dev.anye.mc.basecore.item.module.basecore;

import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.item.module.BasecoreModuleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AutoRepairModuleItem extends BasecoreModuleItem {
    private int tick = 0;
    public AutoRepairModuleItem(){
        super(true);
    }
    @Override
    public void tick(BaseCoreBlockEntity baseCoreBlockEntity, Level level, BlockPos blockPos, BlockState blockState, int count) {
        if (tick >= 300){
            tick = 0;
            baseCoreBlockEntity.addHealth(repair(count));
        }else tick++;
    }
    public int repair(int lvl){
        return switch (lvl){
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 4;
            case 4 -> 6;
            default -> 0;
        };
    }
}
