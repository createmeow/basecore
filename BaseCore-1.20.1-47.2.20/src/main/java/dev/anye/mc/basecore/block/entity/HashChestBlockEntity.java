package dev.anye.mc.basecore.block.entity;

import dev.anye.mc.basecore.basecore.BasecoreServerHelper;
import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.IOwner;
import dev.anye.mc.basecore.block.InventoryBlockEntity;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.menu.HashChestMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class HashChestBlockEntity extends InventoryBlockEntity implements MenuProvider, IOwner {
    public static final int InventorySize = 54;
    private UUID owner = null;
    private int type = 0;
    public HashChestBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegister.HashChest.get(), pPos, pBlockState, InventorySize);
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    public void setType() {
        this.type ++;
        if (this.type > 2 ) this.type = 0;
    }

    @Override
    public boolean canUse(UUID user) {
        if (owner == null) return true;
        BaseCoreBlockEntity baseCoreBlockEntity = BasecoreServerHelper.getBasecore(this.level, this.getBlockPos().getCenter());
        if (baseCoreBlockEntity == null) return true;
        if (type == 0) return user.equals(owner);
        if (type == 1) return baseCoreBlockEntity.canUse(user);
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putInt("type",type);
        if (owner != null) pTag.putUUID("owner",owner);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        type = pTag.getInt("type");
        if (pTag.hasUUID("owner")) owner = pTag.getUUID("owner");
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.basecore.hash_chest");
    }

    public int getPlayerType() {
        return type;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new HashChestMenu(pContainerId,pPlayerInventory,this);
    }

    public void setOwner(UUID owner) {
        if (this.owner == null) this.owner = owner;
    }
}
