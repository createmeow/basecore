package dev.anye.mc.basecore.block.entity;

import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.InventoryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

public class DamageBlockEntity extends InventoryBlockEntity {
    public DamageBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegister.Damage.get(), pPos, pBlockState,54);
    }
    public void setItems(ItemStackHandler itemStackHandler) {
        for (int i = 0;i < itemStackHandler.getSlots();i++){
            this.items.setStackInSlot(i,itemStackHandler.getStackInSlot(i));
        }
    }
}
