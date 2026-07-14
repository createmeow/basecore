package dev.anye.mc.basecore.block;

import dev.anye.mc.basecore.BaseCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class InventoryBlockEntity extends BlockEntity {
    protected final String inventoryItemSaveKey = BaseCore.MOD_ID + ".inventory.item";

    public final ItemStackHandler items;
    public LazyOptional<IItemHandler> itemsLazyOptional = LazyOptional.empty();

    public InventoryBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState,int inventorySize) {
        super(pType, pPos, pBlockState);
        items = new ItemStackHandler(inventorySize){
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }
    //--------------------------------------------------------------------------
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        /*
        if (cap == ForgeCapabilities.ITEM_HANDLER){
            return itemsLazyOptional.cast();
        }

         */
        return super.getCapability(cap, side);
    }


    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemsLazyOptional.invalidate();
    }


    public ItemStackHandler getItems() {
        return items;
    }
    @Override
    public void onLoad() {
        super.onLoad();
        itemsLazyOptional = LazyOptional.of(() -> items);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        items.deserializeNBT(pTag.getCompound(inventoryItemSaveKey));
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.put(inventoryItemSaveKey, items.serializeNBT());
    }


    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }
}
