package dev.anye.mc.basecore.item;

import dev.anye.mc.basecore.block.BlockRegister;
import dev.anye.mc.basecore.block.basecore.BaseCoreBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BasecoreItem extends BlockItem {
    public BasecoreItem() {
        super(BlockRegister.BASE_CORE.get(), new Properties().rarity(Rarity.EPIC));
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext pContext) {
        if (pContext.getPlayer() instanceof ServerPlayer serverPlayer) {
            CustomData blockEntityData = pContext.getItemInHand().get(DataComponents.BLOCK_ENTITY_DATA);
            CompoundTag compoundTag = blockEntityData != null ? blockEntityData.copyTag() : null;
            if (compoundTag != null && compoundTag.hasUUID(BaseCoreBlock.OWNER_KEY)) {
                if (!compoundTag.getUUID(BaseCoreBlock.OWNER_KEY).equals(serverPlayer.getUUID())) {
                    serverPlayer.hurt(serverPlayer.damageSources().fellOutOfWorld(), 2.0f);
                    return InteractionResult.FAIL;
                }
            }
            // Start 30s placement
            if (PlacementHelper.startPlacement(serverPlayer, pContext, pContext.getItemInHand(), BlockRegister.BASE_CORE.get())) {
                return InteractionResult.SUCCESS;
            }
            return super.useOn(pContext);
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltip, TooltipFlag pFlag) {
        CustomData blockEntityData = pStack.get(DataComponents.BLOCK_ENTITY_DATA);
        CompoundTag compoundTag = blockEntityData != null ? blockEntityData.copyTag() : null;
        if (compoundTag != null) {
            if (compoundTag.hasUUID(BaseCoreBlock.OWNER_KEY)) {
                String ownerName = compoundTag.getString("owner_name");
                if (!ownerName.isEmpty()) {
                    pTooltip.add(Component.translatable("tooltip.basecore.basecore.owner_name", ownerName));
                } else {
                    LocalPlayer localPlayer = Minecraft.getInstance().player;
                    if (localPlayer != null && compoundTag.getUUID(BaseCoreBlock.OWNER_KEY).equals(Minecraft.getInstance().player.getUUID())){
                        pTooltip.add(Component.translatable("tooltip.basecore.basecore.owner"));
                    }else pTooltip.add(Component.translatable("tooltip.basecore.basecore.not_owner"));
                }
            } else pTooltip.add(Component.translatable("tooltip.basecore.basecore.no_owner"));
            pTooltip.add(Component.translatable("tooltip.basecore.basecore.range",compoundTag.getInt("range")));
            pTooltip.add(Component.translatable("tooltip.basecore.basecore.health",compoundTag.getInt("max_health")));
        }
        super.appendHoverText(pStack, pContext, pTooltip, pFlag);
    }
}