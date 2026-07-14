package dev.anye.mc.basecore.item;

import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.entity.DefendBlockEntity;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;

public class ProsthodonticItem extends EasyItem {
    public ProsthodonticItem() {
        super(Rarity.UNCOMMON, 64);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        if (pContext.getPlayer() instanceof ServerPlayer serverPlayer) {
            ItemStack itemStack = pContext.getItemInHand();
            if (itemStack.is(this)) {
                var be = serverPlayer.level().getBlockEntity(pContext.getClickedPos());
                if (be == null) return InteractionResult.FAIL;

                if (be instanceof BaseCoreBlockEntity baseCoreBlockEntity) {
                    if (!baseCoreBlockEntity.canUse(serverPlayer.getUUID())) return InteractionResult.FAIL;
                    if (baseCoreBlockEntity.getHealth() >= baseCoreBlockEntity.getMaxHealth()) {
                        serverPlayer.sendSystemMessage(Component.translatable("error.basecore.prosthodontic.health_full"));
                        return InteractionResult.SUCCESS;
                    }
                    itemStack.shrink(1);
                    baseCoreBlockEntity.addHealth(50);
                    return InteractionResult.SUCCESS;
                }

                if (be instanceof DefendBlockEntity defendBlockEntity) {
                    if (!defendBlockEntity.canUse(serverPlayer.getUUID())) return InteractionResult.FAIL;
                    if (defendBlockEntity.getHealth() >= defendBlockEntity.getMaxHealth()) {
                        serverPlayer.sendSystemMessage(Component.translatable("error.basecore.prosthodontic.health_full"));
                        return InteractionResult.SUCCESS;
                    }
                    itemStack.shrink(1);
                    defendBlockEntity.addHealth(50);
                    return InteractionResult.SUCCESS;
                }

                return InteractionResult.FAIL;
            }
        }
        return super.useOn(pContext);
    }
}