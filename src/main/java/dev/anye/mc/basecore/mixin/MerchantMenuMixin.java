package dev.anye.mc.basecore.mixin;

import dev.anye.mc.basecore.cap.PartHolder;
import dev.anye.mc.basecore.config.BasecoreConfig;
import dev.anye.mc.basecore.item.ItemRegister;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Auto-fill parts from stored balance when trading in MerchantMenu.
 * Similar to numismaticoverhaul's auto-fill for coins.
 */
@Mixin(MerchantMenu.class)
public class MerchantMenuMixin {

    @Shadow
    private net.minecraft.world.item.trading.Merchant trader;

    @Inject(method = {"moveFromInventoryToPaymentSlot"}, at = {@At("TAIL")})
    public void basecore$autofillParts(int slot, ItemCost payment, CallbackInfo ci) {
        if (!BasecoreConfig.isComponentMode()) return;

        ItemStack paymentStack = payment.itemStack();
        if (paymentStack.getItem() != ItemRegister.PART.get()) return;

        MerchantMenu handler = (MerchantMenu) (Object) this;

        // Get the player from the trader's trading player
        Player player = (Player) this.trader.getTradingPlayer();
        if (player == null) return;

        // If player already has enough parts in the slot, skip
        ItemStack currentInSlot = handler.getSlot(slot).getItem();
        int currentCount = currentInSlot.isEmpty() ? 0 : currentInSlot.getCount();
        int needed = paymentStack.getCount() - currentCount;

        if (needed <= 0) return;

        // Check stored balance
        int stored = PartHolder.getValue(player);
        if (stored < needed) return;

        // Deduct from stored balance and fill the slot
        PartHolder.modify(player, -needed);

        if (currentInSlot.isEmpty()) {
            handler.getSlot(slot).set(new ItemStack(ItemRegister.PART.get(), paymentStack.getCount()));
        } else if (currentInSlot.getItem() == ItemRegister.PART.get()) {
            currentInSlot.grow(needed);
            handler.getSlot(slot).set(currentInSlot);
        }
    }
}
