package dev.anye.mc.basecore.block.entity;

import dev.anye.mc.basecore.block.*;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.item.module.BaseModuleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.*;

public abstract class ToDamageBlockEntity<T extends BaseModuleItem> extends InventoryBlockEntity implements IHealth, IModule<T>, IOwner, INet {
    protected final Map<T, Long> moduleCooldowns = new HashMap<>();
    public ToDamageBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, int inventorySize) {
        super(pType, pPos, pBlockState, inventorySize);
    }
    public ItemStackHandler getItems(){
        return items;
    }

    public void setItems(ItemStackHandler items) {
        for (int i = 0; i < items.getSlots(); i++) {
            this.items.setStackInSlot(i, items.getStackInSlot(i));
        }
    }
}