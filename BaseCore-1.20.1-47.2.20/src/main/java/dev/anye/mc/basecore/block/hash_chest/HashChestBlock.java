package dev.anye.mc.basecore.block.hash_chest;

import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.FunctionEntityBlock;
import dev.anye.mc.basecore.block.entity.HashChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HashChestBlock extends FunctionEntityBlock {
    public ItemStack dropItem;
    public HashChestBlock() {
        super();
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pPlayer instanceof ServerPlayer serverPlayer){
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (entity instanceof HashChestBlockEntity hashChestBlockEntity){

                if (serverPlayer.getUUID().equals(hashChestBlockEntity.getOwner()) && serverPlayer.isShiftKeyDown()) {
                    hashChestBlockEntity.setType();
                    serverPlayer.sendSystemMessage(Component.translatable("block_entity.basecore.hash_chest.type."+hashChestBlockEntity.getPlayerType()));
                }
                else if (hashChestBlockEntity.canUse(serverPlayer.getUUID()))
                    NetworkHooks.openScreen(serverPlayer, hashChestBlockEntity,pPos);
            }else {
                throw new IllegalStateException("Missing");
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new HashChestBlockEntity(pPos,pState);
    }

    @Override
    public void playerWillDestroy(Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @NotNull Player pPlayer) {
        if (pPlayer instanceof ServerPlayer){
            HashChestBlockEntity entity = pLevel.getBlockEntity(pPos, BlockEntityRegister.HashChest.get()).orElse(null);
            if (entity != null) {
                if (pPlayer.hasCorrectToolForDrops(pState)) {
                    dropItem = new ItemStack(pState.getBlock(), 1);
                    entity.saveToItem(dropItem);
                }
            }
        }
        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }
    @Override
    public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
        List<ItemStack> drops = new ArrayList<>();
        if(dropItem != null){
            drops.add(dropItem);
            return drops;
        }else{
            return super.getDrops(pState, pParams);
        }
    }
}
