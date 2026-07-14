package dev.anye.mc.basecore.item.module.basecore;

import dev.anye.mc.basecore.item.module.BasecoreModuleItem;

/** 安全模块：防止玩家窃取基地核心，每个基地核心最多1个 */
public class SecureModuleItem extends BasecoreModuleItem {
    public SecureModuleItem() {
        super(1);
    }
}
