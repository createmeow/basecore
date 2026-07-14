package dev.anye.mc.basecore.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class EasyItem extends Item {
    public EasyItem(Rarity rarity,int s) {
        super(new Properties().stacksTo(s).rarity(rarity));
    }
}
