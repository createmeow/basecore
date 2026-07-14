package dev.anye.mc.basecore.item.module;

import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.item.EasyItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseModuleItem extends EasyItem {
    private final int maxLevel;
    protected BaseModuleItem(int maxLevel) {
        super(Rarity.RARE,maxLevel);
        this.maxLevel = maxLevel;
    }
    public int fixLevel(int level ){
        return Math.min(level, maxLevel);
    }

    public final int getRange(int count){
        return range(fixLevel(count));
    }

    protected int range(int count){return 0;}


    public final int getHealth(int count){
        return health(fixLevel(count));
    }


    protected int health(int count){return 0;}

}
