package dev.anye.mc.basecore.block.basecore;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.FunctionEntityBlock;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.config.BasecoreConfig;
import dev.anye.mc.basecore.menu.upgrade.BasecoreUpgradeMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class BaseCoreBlock extends FunctionEntityBlock {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String OWNER_KEY = "owner";
    private static final MapCodec<BaseCoreBlock> CODEC = simpleCodec(p -> new BaseCoreBlock());
    public ItemStack dropItem;
    public BaseCoreBlock() {
        super(net.minecraft.world.level.block.Blocks.IRON_BLOCK.properties().sound(SoundType.HEAVY_CORE));
    }

    @Override
    public MapCodec<? extends FunctionEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHit) {
        if (pPlayer instanceof ServerPlayer serverPlayer){
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (entity instanceof BaseCoreBlockEntity baseCoreBlockEntity){
                if (BasecoreConfig.isComponentMode()) {
                    // Component mode: check permission first
                    if (!baseCoreBlockEntity.canUse(serverPlayer.getUUID())) {
                        return InteractionResult.FAIL;
                    }
                    // Open custom upgrade shop UI
                    serverPlayer.openMenu(new net.minecraft.world.MenuProvider() {
                        @Override
                        public net.minecraft.network.chat.Component getDisplayName() {
                            return net.minecraft.network.chat.Component.translatable("block.basecore.basecore");
                        }
                        @Override
                        public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int i, net.minecraft.world.entity.player.Inventory inv, Player player) {
                            return new BasecoreUpgradeMenu(i, inv, baseCoreBlockEntity, false);
                        }
                    }, buf -> {
                        buf.writeBlockPos(pPos);
                        buf.writeBoolean(false);
                    });
                } else {
                    // Modular mode: open container GUI (existing behavior)
                    serverPlayer.openMenu(baseCoreBlockEntity, buf -> buf.writeBlockPos(pPos));
                }
            }else {
                throw new IllegalStateException("Missing");
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BaseCoreBlockEntity(blockPos,blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide){
            return null;
        }
        return createTickerHelper(pBlockEntityType, BlockEntityRegister.BASECORE.get(), ((level, blockPos, blockState, blockEntityIndex) -> blockEntityIndex.tick(level,blockPos,blockState)));
    }
    @Override
    public BlockState playerWillDestroy(Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @NotNull Player pPlayer) {
        if (pPlayer instanceof ServerPlayer){
            BaseCoreBlockEntity entity = pLevel.getBlockEntity(pPos, BlockEntityRegister.BASECORE.get()).orElse(null);
            if (entity != null) {
                if (pPlayer.hasCorrectToolForDrops(pState)) {
                    dropItem = new ItemStack(pState.getBlock(), 1);
                    entity.saveToItem(dropItem, pLevel.registryAccess());
                }
            }
        }
        return super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
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