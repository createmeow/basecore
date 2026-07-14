package dev.anye.mc.basecore.item.module.basecore;

import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.item.module.BasecoreModuleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class DefModuleItem extends BasecoreModuleItem {
    public DefModuleItem(){
        super(16);
    }
    @Override
    protected int health(int count) {
        return  250 * count;
    }

}
