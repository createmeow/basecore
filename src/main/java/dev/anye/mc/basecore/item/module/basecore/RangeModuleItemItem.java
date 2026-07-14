package dev.anye.mc.basecore.item.module.basecore;

import dev.anye.mc.basecore.item.module.BasecoreModuleItem;

public class RangeModuleItemItem extends BasecoreModuleItem {
    public RangeModuleItemItem() {
        super(12, false);
    }

    @Override
    protected int range(int count) {
        return count * 6;
    }

    @Override
    public int getDefendMaxCount() {
        return 4;
    }
}