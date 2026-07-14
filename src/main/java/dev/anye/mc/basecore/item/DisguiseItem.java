package dev.anye.mc.basecore.item;

import dev.anye.mc.basecore.effect.EffectRegister;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class DisguiseItem extends EasyItem{
    private final int effectLvl, effectTime;
    public DisguiseItem(Rarity rarity, int lvl, int time) {
        super(rarity, 1);
        this.effectLvl = lvl;
        this.effectTime = time;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pPlayer instanceof ServerPlayer serverPlayer){
            if (pUsedHand == InteractionHand.MAIN_HAND){
                ItemStack itemStack = serverPlayer.getMainHandItem();
                serverPlayer.addEffect(new MobEffectInstance(EffectRegister.Disguise, effectTime, effectLvl));
                itemStack.shrink(1);
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}