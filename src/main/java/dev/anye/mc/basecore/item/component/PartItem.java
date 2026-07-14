package dev.anye.mc.basecore.item.component;

import dev.anye.mc.basecore.cap.PartHolder;
import dev.anye.mc.basecore.item.ItemRegister;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class PartItem extends Item {
    public PartItem() {
        super(new Properties().stacksTo(99).rarity(Rarity.COMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        int count = heldStack.getCount();

        if (!level.isClientSide) {
            PartHolder.modify(player, count);
        }

        return InteractionResultHolder.success(ItemStack.EMPTY);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack clickedStack, ItemStack otherStack, Slot slot,
                                              ClickAction clickType, Player player, SlotAccess cursorStackReference) {
        // Left-click with another PartItem
        if (clickType == ClickAction.PRIMARY && otherStack.getItem() instanceof PartItem) {
            int space = clickedStack.getMaxStackSize() - clickedStack.getCount();
            if (space <= 0) {
                // Target stack is full → merge excess into a PartBundle
                int total = clickedStack.getCount() + otherStack.getCount();
                ItemStack bundle = PartBundleItem.create(total);
                slot.set(bundle);
                cursorStackReference.set(ItemStack.EMPTY);
                return true;
            }
            int move = Math.min(space, otherStack.getCount());
            clickedStack.grow(move);
            otherStack.shrink(move);
            slot.set(clickedStack);
            cursorStackReference.set(otherStack.isEmpty() ? ItemStack.EMPTY : otherStack);
            return true;
        }

        // Left-click with a PartBundleItem: merge bundle contents into this part item
        if (clickType == ClickAction.PRIMARY && otherStack.getItem() instanceof PartBundleItem bundleItem) {
            int bundleCount = PartBundleItem.getCount(otherStack);
            int space = clickedStack.getMaxStackSize() - clickedStack.getCount();
            int mergeCount = Math.min(space, bundleCount);
            if (mergeCount <= 0) return false;

            clickedStack.grow(mergeCount);
            slot.set(clickedStack);

            int remaining = bundleCount - mergeCount;
            if (remaining <= 0) {
                cursorStackReference.set(ItemStack.EMPTY);
            } else {
                PartBundleItem.setCount(otherStack, remaining);
                cursorStackReference.set(otherStack);
            }
            return true;
        }

        return false;
    }
}
