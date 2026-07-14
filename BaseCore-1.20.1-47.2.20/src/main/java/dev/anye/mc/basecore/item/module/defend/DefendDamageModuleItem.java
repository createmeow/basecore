package dev.anye.mc.basecore.item.module.defend;

import dev.anye.mc.basecore.item.module.DefendBlockModule;

public class DefendDamageModuleItem extends DefendBlockModule {
    public DefendDamageModuleItem(){
        super(2);
    }
    @Override
    protected int damage(int lvl) {
        return lvl;
    }
}
