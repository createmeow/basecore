package dev.anye.mc.basecore.block.damage;

import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.FunctionEntityBlock;
import dev.anye.mc.basecore.block.entity.DamageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DamageBlock extends FunctionEntityBlock {
    public List<ItemStack> drops = new ArrayList<>();
    public DamageBlock() {
        super();
    }
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new DamageBlockEntity(pPos,pState);
    }

    @Override
    public void playerWillDestroy(Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @NotNull Player pPlayer) {
        if (pPlayer instanceof ServerPlayer) {
            pLevel.getBlockEntity(pPos, BlockEntityRegister.Damage.get()).ifPresent(damageBlockEntity -> {
                drops.clear();
                for (int i = 0; i < damageBlockEntity.items.getSlots();i++){
                    drops.add(damageBlockEntity.items.getStackInSlot(i));
                }
                if (pPlayer.hasCorrectToolForDrops(pState)) {
                    random(drops);
                }
            });

        }
        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    public void random(List<ItemStack> drops){
        return;
    }
    @Override
    public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
        if (drops.isEmpty()) return super.getDrops(pState, pParams);
        return drops;
    }
}
