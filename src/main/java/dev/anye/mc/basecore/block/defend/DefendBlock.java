package dev.anye.mc.basecore.block.defend;

import com.mojang.serialization.MapCodec;
import dev.anye.mc.basecore.basecore.BasecoreServerHelper;
import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.FunctionEntityBlock;
import dev.anye.mc.basecore.block.entity.DefendBlockEntity;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.config.BasecoreConfig;
import dev.anye.mc.basecore.menu.upgrade.BasecoreUpgradeMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DefendBlock extends FunctionEntityBlock {
    private static final MapCodec<DefendBlock> CODEC = simpleCodec(p -> new DefendBlock());
    public final List<ItemStack> dropItem = new ArrayList<>();
    public DefendBlock() {
        super();
    }

    @Override
    public MapCodec<? extends FunctionEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHit) {
        if (pPlayer instanceof ServerPlayer serverPlayer){
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (entity instanceof DefendBlockEntity defendBlockEntity){
                if (serverPlayer.getUUID().equals(defendBlockEntity.getOwner()) && serverPlayer.isShiftKeyDown()) {
                    defendBlockEntity.setType();
                    serverPlayer.sendSystemMessage(Component.translatable("block_entity.basecore.defend.type."+defendBlockEntity.getPlayerType()));
                }
                else {
                    if (BasecoreConfig.isComponentMode()) {
                        // Component mode: check permission first
                        if (!defendBlockEntity.canUse(serverPlayer.getUUID())) {
                            return InteractionResult.FAIL;
                        }
                        // Open custom upgrade shop UI for defense turret
                        BaseCoreBlockEntity baseCore = BasecoreServerHelper.getBasecore(serverPlayer.level(), pPos.getCenter());
                        if (baseCore != null) {
                            serverPlayer.openMenu(new net.minecraft.world.MenuProvider() {
                                @Override
                                public net.minecraft.network.chat.Component getDisplayName() {
                                    return net.minecraft.network.chat.Component.translatable("block.basecore.defend");
                                }
                                @Override
                                public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int i, net.minecraft.world.entity.player.Inventory inv, Player player) {
                                    return new BasecoreUpgradeMenu(i, inv, baseCore, true, pPos);
                                }
                            }, buf -> {
                                buf.writeBlockPos(baseCore.getBlockPos());
                                buf.writeBoolean(true);
                                buf.writeBlockPos(pPos); // turret position
                            });
                        } else {
                            serverPlayer.sendSystemMessage(Component.translatable("error.basecore.pssi.no_basecore"));
                        }
                    } else {
                        defendBlockEntity.showHealth();
                        defendBlockEntity.updateToPlayer(serverPlayer);
                        serverPlayer.openMenu(defendBlockEntity, buf -> buf.writeBlockPos(pPos));
                    }
                }
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
    public BlockState playerWillDestroy(Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @NotNull Player pPlayer) {
        if (pPlayer instanceof ServerPlayer){
            DefendBlockEntity entity = pLevel.getBlockEntity(pPos, BlockEntityRegister.Defend.get()).orElse(null);
            if (entity != null) {
                if (pPlayer.hasCorrectToolForDrops(pState)) {
                    dropItem.clear();

                    ItemStack item = new ItemStack(pState.getBlock(), 1);

                    // Save minimal data: only owner, health, and modules (inventory items)
                    CompoundTag minimalTag = new CompoundTag();
                    if (entity.getOwner() != null) minimalTag.putUUID("owner", entity.getOwner());
                    minimalTag.putInt("health", entity.getHealth());
                    // Save block entity type id (required for data component encoding in 1.21+)
                    BlockEntity.addEntityType(minimalTag, BlockEntityRegister.Defend.get());
                    // Save inventory (modules)
                    minimalTag.put("basecore.inventory.item", entity.getItems().serializeNBT(pLevel.registryAccess()));

                    item.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(minimalTag));
                    dropItem.add(item);
                }
            }
        }
        return super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }
    @Override
    public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
        return dropItem;
    }
}