package dev.anye.mc.basecore.block.entity;

import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.InventoryBlockEntity;
import dev.anye.mc.basecore.block.IOwner;
import dev.anye.mc.basecore.block.INet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DamageBlockEntity extends InventoryBlockEntity implements IOwner, INet {
    private UUID owner;

    public DamageBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegister.Damage.get(), pPos, pBlockState, 54);
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Override
    public @Nullable UUID getOwner() {
        return owner;
    }

    @Override
    public boolean canUse(UUID user) {
        return false;
    }

    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        if (pTag.contains("owner")) owner = pTag.getUUID("owner");
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        if (owner != null) pTag.putUUID("owner", owner);
    }

    public void setItems(net.neoforged.neoforge.items.ItemStackHandler items) {
        for (int i = 0; i < items.getSlots(); i++) {
            this.items.setStackInSlot(i, items.getStackInSlot(i));
        }
    }

    @Override
    public void updateToClient() {
    }

    @Override
    public void handlePacket(CompoundTag packet) {
    }
}