package dev.anye.mc.basecore.item;

import dev.anye.mc.basecore.block.BlockRegister;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

public class HashChestBlockItem extends BlockItem {
    public HashChestBlockItem() {
        super(BlockRegister.HASH_CHEST.get(), new Properties().rarity(Rarity.EPIC));
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext pContext) {
        if (pContext.getPlayer() instanceof ServerPlayer serverPlayer) {
            if (PlacementHelper.startPlacement(serverPlayer, pContext, pContext.getItemInHand(), BlockRegister.HASH_CHEST.get())) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }
}
