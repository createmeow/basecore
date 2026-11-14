package dev.anye.mc.basecore.item.module;

import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.item.EasyItem;
import dev.anye.mc.basecore.item.component.ItemComponentInit;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseModuleItem extends EasyItem {
    private final int maxLevel;
    protected BaseModuleItem(int maxLevel) {
        // 模块默认标记为苏式黄色显示：在苏式基地计算机界面中切换为 _yellow 材质
        super(new Properties().stacksTo(maxLevel).rarity(Rarity.RARE)
                .component(ItemComponentInit.SOVIET_YELLOW_COMPONENT, Unit.INSTANCE));
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

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getDefendMaxCount() {
        return 0;
    }

    /**
     * Module items render their own description + "适用于" section in the hover text,
     * so we disable the generic {@link EasyItem} description to avoid duplicates.
     */
    @Override
    protected boolean shouldAddHoverDescription() {
        return false;
    }
}