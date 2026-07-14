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
        super(BlockRegister.BASE_CORE.get(), new Properties().rarity(Rarity.RARE));
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext pContext) {
        if (pContext.getPlayer() instanceof ServerPlayer serverPlayer) {
            CustomData blockEntityData = pContext.getItemInHand().get(DataComponents.BLOCK_ENTITY_DATA);
            CompoundTag compoundTag = blockEntityData != null ? blockEntityData.copyTag() : null;
            if (compoundTag != null && compoundTag.hasUUID(BaseCoreBlock.OWNER_KEY)) {
                if (compoundTag.getUUID(BaseCoreBlock.OWNER_KEY).equals(serverPlayer.getUUID())) {
                    return super.useOn(pContext);
                }
                serverPlayer.hurt(serverPlayer.damageSources().fellOutOfWorld(),2.0f);
            }else {
                return super.useOn(pContext);
            }
        }
        return InteractionResult.FAIL;
    }
/*
    @Override
    protected boolean canPlace(BlockPlaceContext pContext, BlockState pState) {
        if (!pContext.getLevel().isClientSide()) {
            if (ActivityBasecores.check(pContext.getLevel(), pContext.getClickedPos(), pContext.getItemInHand()))
                return super.canPlace(pContext, pState);
            if (pContext.getPlayer() != null) {
                pContext.getPlayer().sendSystemMessage(Component.translatable("error.basecore.permission.range_has_basecore"));
            }
        }
        return false;
    }

    @Override
    public InteractionResult place(BlockPlaceContext pContext) {
        if (super.place(pContext) == InteractionResult.sidedSuccess(pContext.getLevel().isClientSide) && pContext.getLevel() instanceof ServerLevel serverLevel){
            serverLevel.getBlockEntity(pContext.getClickedPos(), BlockEntityRegister.BASECORE.get()).ifPresent(baseCoreBlockEntity -> baseCoreBlockEntity.setOwner(pContext.getPlayer().getUUID()));
            return InteractionResult.sidedSuccess(pContext.getLevel().isClientSide);
        }
        return InteractionResult.FAIL;
    }


 */
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