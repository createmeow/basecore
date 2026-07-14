package dev.anye.mc.basecore.block.entity;

import dev.anye.mc.basecore.block.BlockEntityRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class NothingBlockEntity extends BlockEntity {
    private BlockState originalBlockState = Blocks.AIR.defaultBlockState();
    private CompoundTag originalBlockEntityData = null;
    private int countdown = 300;

    public NothingBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegister.NOTHING.get(), pPos, pBlockState);
    }

    public void setOriginalData(BlockState blockState, CompoundTag blockEntityData) {
        this.originalBlockState = blockState;
        this.originalBlockEntityData = blockEntityData;
        this.countdown = 300;
        setChanged();
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (level.isClientSide()) return;
        if (countdown > 0) {
            countdown--;
            if (countdown <= 0) {
                restoreOriginal(level, blockPos);
            }
        }
    }

    private void restoreOriginal(Level level, BlockPos blockPos) {
        level.setBlock(blockPos, originalBlockState, Block.UPDATE_ALL);
        if (originalBlockEntityData != null && !originalBlockEntityData.isEmpty()) {
            BlockEntity be = level.getBlockEntity(blockPos);
            if (be != null) {
                be.handleUpdateTag(originalBlockEntityData, level.registryAccess());
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        pTag.put("originalBlockState", NbtUtils.writeBlockState(originalBlockState));
        if (originalBlockEntityData != null) {
            pTag.put("originalBlockEntityData", originalBlockEntityData);
        }
        pTag.putInt("countdown", countdown);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        if (pTag.contains("originalBlockState")) {
            originalBlockState = NbtUtils.readBlockState(
                    pRegistries.lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK),
                    pTag.getCompound("originalBlockState"));
        }
        if (pTag.contains("originalBlockEntityData")) {
            originalBlockEntityData = pTag.getCompound("originalBlockEntityData");
        }
        if (pTag.contains("countdown")) {
            countdown = pTag.getInt("countdown");
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, pRegistries);
        return tag;
    }

    public int getCountdown() {
        return countdown;
    }
}