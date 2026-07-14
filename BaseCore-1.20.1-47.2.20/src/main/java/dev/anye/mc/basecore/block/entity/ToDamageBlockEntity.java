package dev.anye.mc.basecore.block.entity;

import dev.anye.mc.basecore.block.IHealth;
import dev.anye.mc.basecore.block.IModule;
import dev.anye.mc.basecore.block.IOwner;
import dev.anye.mc.basecore.block.InventoryBlockEntity;
import dev.anye.mc.basecore.item.module.BaseModuleItem;
import dev.anye.mc.basecore.item.module.BasecoreModuleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ToDamageBlockEntity<M extends BaseModuleItem> extends InventoryBlockEntity implements IOwner, IHealth , IModule<M> {
    public ToDamageBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, int inventorySize) {
        super(pType, pPos, pBlockState, inventorySize);
    }

    @Override
    public int getModuleLvl(M item) {
        return 0;
    }
}
