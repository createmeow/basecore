package dev.anye.mc.basecore.item.component;

import dev.anye.mc.basecore.cap.PartHolder;
import dev.anye.mc.basecore.item.ItemRegister;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * PartBundleItem holds multiple parts in a single stack.
 * Right-click on a PartItem to merge parts into this bundle.
 * Right-click a bundle to split out PartItems.
 */
public class PartBundleItem extends Item {
    public PartBundleItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    public static int getCount(ItemStack stack) {
        return stack.getOrDefault(ItemComponentInit.PART_BUNDLE_COMPONENT, PartBundleComponent.of(0)).count();
    }

    public static void setCount(ItemStack stack, int count) {
        stack.set(ItemComponentInit.PART_BUNDLE_COMPONENT, PartBundleComponent.of(count));
    }

    public static ItemStack create(int count) {
        ItemStack stack = new ItemStack(ItemRegister.PART_BUNDLE.get());
        setCount(stack, count);
        return stack;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack clickedStack, ItemStack otherStack, Slot slot,
                                              ClickAction clickType, Player player, SlotAccess cursorStackReference) {
        // Right-click on empty slot: split out one max stack of parts
        if (clickType == ClickAction.SECONDARY && otherStack.isEmpty()) {
            int bundleCount = getCount(clickedStack);
            if (bundleCount <= 0) return false;

            int splitCount = Math.min(bundleCount, 64);
            ItemStack partStack = new ItemStack(ItemRegister.PART.get(), splitCount);
            cursorStackReference.set(partStack);

            int remaining = bundleCount - splitCount;
            if (remaining <= 0) {
                slot.set(ItemStack.EMPTY);
            } else {
                setCount(clickedStack, remaining);
                slot.set(clickedStack);
            }
            return true;
        }

        // Left-click with PartItem: merge into bundle
        if (clickType == ClickAction.PRIMARY && otherStack.getItem() instanceof PartItem) {
            int bundleCount = getCount(clickedStack);
            int addCount = otherStack.getCount();
            setCount(clickedStack, bundleCount + addCount);
            slot.set(clickedStack);
            cursorStackReference.set(ItemStack.EMPTY);
            return true;
        }

        // Left-click with another PartBundle: merge both bundles
        if (clickType == ClickAction.PRIMARY && otherStack.getItem() instanceof PartBundleItem) {
            int bundleCount = getCount(clickedStack);
            int otherCount = getCount(otherStack);
            setCount(clickedStack, bundleCount + otherCount);
            slot.set(clickedStack);
            cursorStackReference.set(ItemStack.EMPTY);
            return true;
        }

        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        int count = getCount(heldStack);
        if (count <= 0) return InteractionResultHolder.pass(heldStack);

        if (!level.isClientSide) {
            PartHolder.modify(player, count);
            player.displayClientMessage(Component.literal("§a+" + count + " §7零件"), true);
        }
        return InteractionResultHolder.success(ItemStack.EMPTY);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.basecore.part_bundle.count", getCount(stack)));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
