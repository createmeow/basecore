package dev.anye.mc.basecore.item;

import dev.anye.mc.basecore.basecore.BasecoreServerHelper;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class ProtectionSignalShieldItem extends EasyItem{
    public ProtectionSignalShieldItem() {
        super(Rarity.UNCOMMON, 1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pPlayer instanceof ServerPlayer serverPlayer){
            ItemStack itemStack = serverPlayer.getItemInHand(pUsedHand);
            if (itemStack.is(this)) {
                BaseCoreBlockEntity baseCoreBlockEntity = BasecoreServerHelper.getBasecore(serverPlayer);
                if (baseCoreBlockEntity == null) {
                    serverPlayer.sendSystemMessage(Component.translatable("error.basecore.pssi.no_basecore"));
                    return InteractionResultHolder.fail(itemStack);
                }
                if (baseCoreBlockEntity.canUse(serverPlayer.getUUID())) {
                    serverPlayer.sendSystemMessage(Component.translatable("error.basecore.pssi.has_permission"));
                    return InteractionResultHolder.fail(itemStack);
                }
                if (baseCoreBlockEntity.getEntityData().getInterferenceTime() > 0){
                    serverPlayer.sendSystemMessage(Component.translatable("error.basecore.pssi.interfered"));
                    return InteractionResultHolder.fail(itemStack);
                }
                itemStack.shrink(1);
                baseCoreBlockEntity.getEntityData().setInterferenceTime(6000);
                baseCoreBlockEntity.updateToClient();
                giveRandomItems(serverPlayer,2);
                return InteractionResultHolder.success(itemStack);
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }
    public void giveRandomItems(ServerPlayer serverPlayer,int c){
        for (int i = 0;i<c; i++){
            serverPlayer.getInventory().placeItemBackInInventory(new ItemStack(Items.STONE));
        }
    }
}
