package dev.anye.mc.basecore.menu.upgrade;

import dev.anye.mc.basecore.item.module.BasecoreModuleItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Represents ONE module type in the upgrade UI.
 * Shows current count / max, and allows buy [+1] or sell-back [-1].
 */
public class UpgradeEntry {
    private final BasecoreModuleItem module;
    private final int partCost;
    private final ItemStack displayStack;
    private final int maxCount;
    private final int currentCount;

    public UpgradeEntry(BasecoreModuleItem module, int partCost, ItemStack displayStack,
                        int maxCount, int currentCount) {
        this.module = module;
        this.partCost = partCost;
        this.displayStack = displayStack;
        this.maxCount = maxCount;
        this.currentCount = currentCount;
    }

    public BasecoreModuleItem getModule() { return module; }
    public int getPartCost() { return partCost; }
    public ItemStack getDisplayStack() { return displayStack; }
    public int getMaxCount() { return maxCount; }
    public int getCurrentCount() { return currentCount; }
    public boolean canBuy() { return maxCount <= 0 || currentCount < maxCount; }
    public boolean canSell() { return currentCount > 0; }

    public Component getDisplayName() {
        return displayStack.getHoverName();
    }
}
