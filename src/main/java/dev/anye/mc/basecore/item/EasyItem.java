package dev.anye.mc.basecore.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class EasyItem extends Item {
    public EasyItem(Rarity rarity, int s) {
        super(new Properties().stacksTo(s).rarity(rarity));
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltip, TooltipFlag pFlag) {
        String descKey = pStack.getDescriptionId() + ".desc";
        Component desc = Component.translatable(descKey);
        if (!desc.getString().equals(descKey)) {
            pTooltip.add(desc);
        }
        super.appendHoverText(pStack, pContext, pTooltip, pFlag);
    }
}
