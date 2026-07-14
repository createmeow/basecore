package dev.anye.mc.basecore.block.defend;

import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.FunctionEntityBlock;
import dev.anye.mc.basecore.block.entity.DefendBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DefendBlock extends FunctionEntityBlock {
    public final List<ItemStack> dropItem = new ArrayList<>();
    public DefendBlock() {
        super();
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pPlayer instanceof ServerPlayer serverPlayer){
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (entity instanceof DefendBlockEntity baseCoreBlockEntity){
                if (serverPlayer.getUUID().equals(baseCoreBlockEntity.getOwner()) && serverPlayer.isShiftKeyDown()) {
                    baseCoreBlockEntity.setType();
                    serverPlayer.sendSystemMessage(Component.translatable("block_entity.basecore.defend.type."+baseCoreBlockEntity.getPlayerType()));
                }
                else NetworkHooks.openScreen(serverPlayer,baseCoreBlockEntity,pPos);
            }else {
                throw new IllegalStateException("Missing");
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new DefendBlockEntity(pPos,pState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide){
            return null;
        }
        return createTickerHelper(pBlockEntityType, BlockEntityRegister.Defend.get(), ((level, blockPos, blockState, defendBlockEntity) -> defendBlockEntity.tick(level,blockPos,blockState)));
    }
    @Override
    public void playerWillDestroy(Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @NotNull Player pPlayer) {
        if (pPlayer instanceof ServerPlayer){
            DefendBlockEntity entity = pLevel.getBlockEntity(pPos, BlockEntityRegister.Defend.get()).orElse(null);
            if (entity != null) {
                if (pPlayer.hasCorrectToolForDrops(pState)) {
                    dropItem.clear();
                    /*
                    ItemStackHandler itemStackHandler = entity.items;
                    for (int i =0; i < itemStackHandler.getSlots();i++){
                        dropItem.add(itemStackHandler.getStackInSlot(i));
                    }

                     */

                    ItemStack item = new ItemStack(pState.getBlock(), 1);
                    entity.saveToItem(item);
                    CompoundTag nbt = item.getTagElement("BlockEntityTag");
                    if (nbt != null)
                        nbt.remove("tick");
                    dropItem.add(item);
                }
            }
        }
        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }
    @Override
    public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
        return dropItem;
    }
}
