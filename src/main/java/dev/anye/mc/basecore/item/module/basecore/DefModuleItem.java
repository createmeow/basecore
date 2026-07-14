package dev.anye.mc.basecore.item.module.basecore;

import dev.anye.mc.basecore.item.module.BasecoreModuleItem;

public class DefModuleItem extends BasecoreModuleItem {
    public DefModuleItem() {
        super(16);
    }
    @Override
    protected int health(int count) {
        return 250 * count;
    }

    @Override
    public int getDefendMaxCount() {
        return 2;
    }
}