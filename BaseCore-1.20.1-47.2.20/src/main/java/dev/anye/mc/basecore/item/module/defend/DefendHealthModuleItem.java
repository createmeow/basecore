package dev.anye.mc.basecore.item.module.defend;

import dev.anye.mc.basecore.item.module.DefendBlockModule;

public class DefendHealthModuleItem extends DefendBlockModule {
    public DefendHealthModuleItem(){
        super(2);
    }

    @Override
    public int health(int count) {
        return 50 * count;
    }
}
