package dev.anye.mc.basecore.block.soviet_computer;

import com.mojang.serialization.MapCodec;
import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.FunctionEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 苏式基地计算机控制器方块。放置后自动填充 3x3x3 外壳，
 * 玩家在中层四边空位放满电致发光显示器后右键打开专用 GUI。
 */
public class SovietComputerBlock extends FunctionEntityBlock {
    private static final MapCodec<SovietComputerBlock> CODEC = simpleCodec(p -> new SovietComputerBlock());
    private final List<ItemStack> dropItem = new ArrayList<>();

    public SovietComputerBlock() {
        super(Blocks.IRON_BLOCK.properties().sound(SoundType.METAL).strength(5.0F, 15.0F));
    }

    @Override
    public @NotNull MapCodec<? extends FunctionEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SovietComputerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, BlockEntityRegister.SOVIET_COMPUTER.get(),
                (lvl, pos, st, be) -> be.tick(lvl, pos, st));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof ServerPlayer player
                && level.getBlockEntity(pos) instanceof SovietComputerBlockEntity computer) {
            computer.setOwner(player.getUUID());
        }
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (!isEnabledForInteraction(serverPlayer)) {
                return InteractionResult.FAIL;
            }
            if (!(level.getBlockEntity(pos) instanceof SovietComputerBlockEntity computer)) {
                return InteractionResult.FAIL;
            }
            if (!SovietComputerStructure.isComplete(level, pos)) {
                serverPlayer.sendSystemMessage(Component.translatable("error.basecore.soviet_computer.incomplete"));
                return InteractionResult.FAIL;
            }
            if (!computer.canUse(serverPlayer.getUUID())) {
                serverPlayer.sendSystemMessage(Component.translatable("error.basecore.permission.right_click_block"));
                return InteractionResult.FAIL;
            }
            // 按当前玩法打开界面：组件玩法为零件升级页，模块玩法为原容器页
            openGui(serverPlayer, computer, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** 苏式计算机玩法是否启用；未启用时给玩家提示并返回 false。 */
    public static boolean isEnabledForInteraction(ServerPlayer player) {
        if (dev.anye.mc.basecore.config.BasecoreConfig.isSovietComputerEnabled()) return true;
        player.sendSystemMessage(Component.translatable("error.basecore.soviet_computer.disabled"));
        return false;
    }

    /**
     * 按当前玩法打开苏式计算机界面：
     * 组件模式 -> 零件升级界面（dev.anye.mc.basecore.menu.upgrade.BasecoreUpgradeMenu）；模块模式 -> 原容器界面。
     * 控制器、外壳与显示器被右键时都走这里，保证三种位置行为一致。
     */
    public static void openGui(ServerPlayer serverPlayer, SovietComputerBlockEntity computer, BlockPos pos) {
        if (dev.anye.mc.basecore.config.BasecoreConfig.isComponentMode()) {
            serverPlayer.openMenu(new net.minecraft.world.MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("block.basecore.soviet_computer");
                }

                @Override
                public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                        int containerId, net.minecraft.world.entity.player.Inventory inv,
                        Player player) {
                    return new dev.anye.mc.basecore.menu.upgrade.BasecoreUpgradeMenu(
                            containerId, inv, computer, false);
                }
            }, buf -> {
                buf.writeBlockPos(pos);
                buf.writeBoolean(false);
            });
        } else {
            serverPlayer.openMenu(computer, buf -> buf.writeBlockPos(pos));
        }
    }

    /** 构造保留完整数据的掉落物（owner / 血量 / 范围 / 成员 / 电路 / 进度）。 */
    public static ItemStack createDropStack(Level level, SovietComputerBlockEntity computer, BlockState state) {
        ItemStack item = new ItemStack(state.getBlock(), 1);
        CompoundTag tag = new CompoundTag();
        computer.saveAdditional(tag, level.registryAccess());
        BlockEntity.addEntityType(tag, BlockEntityRegister.SOVIET_COMPUTER.get());
        item.set(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA,
                net.minecraft.world.item.component.CustomData.of(tag));
        return item;
    }

    @Override
    public BlockState playerWillDestroy(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        if (player instanceof ServerPlayer && player.hasCorrectToolForDrops(state)
                && level.getBlockEntity(pos) instanceof SovietComputerBlockEntity computer) {
            dropItem.clear();
            dropItem.add(createDropStack(level, computer, state));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof SovietComputerBlockEntity computer) {
                dev.anye.mc.basecore.basecore.BasecoreServerHelper.delEntityAndSend(computer);
            }
            if (!SovietComputerStructure.disassembling) {
                SovietComputerStructure.disassemble(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (!dropItem.isEmpty()) return new ArrayList<>(dropItem);
        return super.getDrops(state, params);
    }
}
