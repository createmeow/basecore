package dev.anye.mc.basecore.item.component;

import dev.anye.mc.basecore.cap.PartHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Loot-style part bundle with long-press use progress (realityvalue-style).
 * Gives a random amount of parts within [minParts, maxParts] on use completion.
 */
public class LootPartBundleItem extends Item {
    private final int minParts;
    private final int maxParts;

    public LootPartBundleItem(Rarity rarity, int minParts, int maxParts) {
        super(new Properties().stacksTo(1).rarity(rarity));
        this.minParts = minParts;
        this.maxParts = maxParts;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 48; // 2.4 seconds, matching realityvalue-style use
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            int amount = player.getRandom().nextIntBetweenInclusive(minParts, maxParts);
            PartHolder.modify(player, amount);
            player.displayClientMessage(Component.literal("§a+" + amount + " §7零件"), true);
            stack.shrink(1);
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.basecore.loot_part_bundle.range", minParts, maxParts));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
