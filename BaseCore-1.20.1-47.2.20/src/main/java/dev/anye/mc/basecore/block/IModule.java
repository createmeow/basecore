package dev.anye.mc.basecore.block;

import dev.anye.mc.basecore.item.module.BaseModuleItem;

public interface IModule<T extends BaseModuleItem> {
     int getModuleLvl(T item);
}
