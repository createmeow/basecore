package dev.anye.mc.basecore.menu;

import com.mojang.logging.LogUtils;
import dev.anye.mc.basecore.block.BlockRegister;
import dev.anye.mc.basecore.block.entity.DefendBlockEntity;
import dev.anye.mc.basecore.item.module.DefendBlockModule;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class DefendMenu extends AbstractContainerMenu {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int inventorySlotCount = 27;
    private DefendBlockEntity defendBlockEntity;
    public DefendMenu(int pContainerId, Inventory inventory , FriendlyByteBuf ex) {
        this(pContainerId,inventory, inventory.player.level().getBlockEntity(ex.readBlockPos()),new SimpleContainerData(3));
    }
    public DefendMenu(int cid, Inventory inv, BlockEntity ent, ContainerData dat){
        super(MenuTypeRegister.DEFEND_MENU.get(), cid);
        checkContainerSize(inv,inventorySlotCount);
        if (ent instanceof DefendBlockEntity defendBlockEntity1) {
            this.defendBlockEntity = defendBlockEntity1;
            addPlayerInventory(inv, 8, 84, 18);
            addPlayerHotBar(inv, 8, 142, 18);

            ItemStackHandler itemHandlerHelper = defendBlockEntity1.getItems();
            int x = 0, y = 0;
            for (int i = 0;i<itemHandlerHelper.getSlots();i++ ) {
                //i % 9
                if (y >= 9) {
                    x++;
                    y = 0;
                }
                this.addSlot(new SlotItemHandler(itemHandlerHelper, i, 7+y * 18, 20+x*18) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack itemStack) {
                        return itemStack.getItem().asItem() instanceof DefendBlockModule;
                    }
                });
                y++;
            }
            addDataSlots(dat);
        }
    }
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int ind) {
        Slot sourceSlot = slots.get(ind);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();
        if (ind < InventorySlotIndex.VANILLA_FIRST_SLOT_INDEX + InventorySlotIndex.VANILLA_SLOT_COUNT){
            if (!moveItemStackTo(sourceStack,InventorySlotIndex.INVENTORY_FIRST_SLOT_INDEX,InventorySlotIndex.INVENTORY_FIRST_SLOT_INDEX+inventorySlotCount,false)){
                return ItemStack.EMPTY;
            }
        }else if (ind < InventorySlotIndex.INVENTORY_FIRST_SLOT_INDEX+inventorySlotCount){
            if (!moveItemStackTo(sourceStack,InventorySlotIndex.VANILLA_FIRST_SLOT_INDEX,InventorySlotIndex.VANILLA_FIRST_SLOT_INDEX + InventorySlotIndex.VANILLA_SLOT_COUNT,false)){
                return ItemStack.EMPTY;
            }
        }else {
            LOGGER.error("Error Inventory slot index");
            return ItemStack.EMPTY;
        }
        if (sourceStack.getCount() == 0){
            sourceSlot.set(ItemStack.EMPTY);
        }else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(player,sourceStack);
        return copyOfSourceStack;
    }

    public void addPlayerInventory(Inventory playerInv,int x,int y,int space){
        for (int i= 0; i<3;i++){
            for (int m = 0;m<9;m++){
                this.addSlot(new Slot(playerInv,m+i*9+9,x+m*space,y+i*space));
            }
        }
    }
    public void addPlayerHotBar(Inventory inventory,int x,int y,int space){
        for (int i = 0;i<9;i++){
            this.addSlot(new Slot(inventory,i,x+i*space,y));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), defendBlockEntity.getBlockPos()),player, BlockRegister.DEFEND.get());
    }
}
