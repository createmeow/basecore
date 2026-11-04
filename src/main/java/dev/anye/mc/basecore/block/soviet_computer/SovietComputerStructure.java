package dev.anye.mc.basecore.block.soviet_computer;

import dev.anye.mc.basecore.block.BlockRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * 苏式基地计算机多方块结构（3x3x3，控制器位于最底层中心，向上生长）：
 * <ul>
 *     <li>第一层（控制器同层）：3x3，控制器居中，其余 8 格为外壳；</li>
 *     <li>第二层：{@code OAO/AOA/OAO}，四角 + 中心为外壳，四边中点留空；</li>
 *     <li>第三层：3x3 外壳。</li>
 * </ul>
 * 放置控制器时自动填充外壳；玩家在中层四边任一空位放入 1 个电致发光显示器后结构即完成，
 * 其余空位自动封上外壳；之后右键控制器（或外壳/显示器）打开 GUI。
 */
public final class SovietComputerStructure {
    /** 防止解体过程中的级联破坏。 */
    public static boolean disassembling = false;
    /** 结构建/解封显示器槽位时的内部编辑标记，避免触发“破坏部件即整机解体”。 */
    private static boolean internalEdit = false;

    private SovietComputerStructure() {
    }

    /** 需要自动填充外壳的位置（22 格）。 */
    public static List<BlockPos> casingPositions(BlockPos center) {
        List<BlockPos> list = new ArrayList<>();
        // 第一层：控制器同层的 3x3，去掉中心（控制器本身）
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                list.add(center.offset(dx, 0, dz));
            }
        }
        // 第二层：四角 + 中心（OAO/AOA/OAO 中的 O）
        for (int dx : new int[]{-1, 1}) {
            for (int dz : new int[]{-1, 1}) {
                list.add(center.offset(dx, 1, dz));
            }
        }
        list.add(center.offset(0, 1, 0));
        // 第三层：3x3
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                list.add(center.offset(dx, 2, dz));
            }
        }
        return list;
    }

    /** 需要放置电致发光显示器的四个空位（第二层四边中点）。 */
    public static List<BlockPos> displayPositions(BlockPos center) {
        List<BlockPos> list = new ArrayList<>();
        list.add(center.offset(1, 1, 0));
        list.add(center.offset(-1, 1, 0));
        list.add(center.offset(0, 1, 1));
        list.add(center.offset(0, 1, -1));
        return list;
    }

    /** 结构是否完整：外壳齐全，且四边空位中至少有 1 个显示器（其余空位已封上外壳）。 */
    public static boolean isComplete(Level level, BlockPos center) {
        if (!(level.getBlockState(center).getBlock() instanceof SovietComputerBlock)) return false;
        for (BlockPos pos : casingPositions(center)) {
            if (!(level.getBlockState(pos).getBlock() instanceof SovietComputerCasingBlock)) return false;
        }
        boolean hasDisplay = false;
        for (BlockPos pos : displayPositions(center)) {
            Block block = level.getBlockState(pos).getBlock();
            if (block instanceof ElectroluminescentDisplayBlock) {
                hasDisplay = true;
            } else if (!(block instanceof SovietComputerCasingBlock)) {
                return false;
            }
        }
        return hasDisplay;
    }

    /**
     * 目标位置能否容纳整台计算机（3x3x3 空间必须为空或可替换）。
     * 放置物品前调用，避免玩家消耗物品后才发现空间不足。
     */
    public static boolean canFill(Level level, BlockPos center) {
        for (BlockPos pos : casingPositions(center)) {
            BlockState existing = level.getBlockState(pos);
            if (existing.getBlock() instanceof SovietComputerCasingBlock) continue;
            if (!existing.isAir() && !existing.canBeReplaced()) return false;
        }
        for (BlockPos pos : displayPositions(center)) {
            BlockState existing = level.getBlockState(pos);
            if (existing.getBlock() instanceof ElectroluminescentDisplayBlock) continue;
            if (!existing.isAir() && !existing.canBeReplaced()) return false;
        }
        return true;
    }

    /**
     * 放置时自动填充外壳。若目标位置被其它方块占用则返回 false（由调用方提示玩家）。
     * 四边中点保持为空，供玩家放置显示器；一旦其中出现显示器，其余空位自动封上外壳。
     */
    public static boolean fill(Level level, BlockPos center) {
        BlockState casing = BlockRegister.SOVIET_COMPUTER_CASING.get().defaultBlockState();
        for (BlockPos pos : casingPositions(center)) {
            BlockState existing = level.getBlockState(pos);
            if (existing.getBlock() instanceof SovietComputerCasingBlock) continue;
            if (!existing.isAir() && !existing.canBeReplaced()) return false;
        }
        for (BlockPos pos : casingPositions(center)) {
            if (!(level.getBlockState(pos).getBlock() instanceof SovietComputerCasingBlock)) {
                level.setBlockAndUpdate(pos, casing);
            }
        }
        // 已有显示器时，把其余空位封上外壳（只需 1 个显示器即可工作）；
        // 没有显示器时（例如显示器被破坏），把之前封上的空位重新打开，方便玩家补放显示器。
        if (hasAnyDisplay(level, center)) {
            for (BlockPos pos : displayPositions(center)) {
                Block block = level.getBlockState(pos).getBlock();
                if (block instanceof ElectroluminescentDisplayBlock || block instanceof SovietComputerCasingBlock) continue;
                level.setBlockAndUpdate(pos, casing);
            }
        } else {
            internalEdit = true;
            try {
                for (BlockPos pos : displayPositions(center)) {
                    if (level.getBlockState(pos).getBlock() instanceof SovietComputerCasingBlock) {
                        level.removeBlock(pos, false);
                    }
                }
            } finally {
                internalEdit = false;
            }
        }
        return true;
    }

    /** 中层四边空位中是否已放入至少 1 个显示器。 */
    public static boolean hasAnyDisplay(Level level, BlockPos center) {
        for (BlockPos pos : displayPositions(center)) {
            if (level.getBlockState(pos).getBlock() instanceof ElectroluminescentDisplayBlock) return true;
        }
        return false;
    }

    /** 解体：移除外壳与显示器（显示器会掉落）。 */
    public static void disassemble(Level level, BlockPos center) {
        if (disassembling) return;
        disassembling = true;
        try {
            for (BlockPos pos : casingPositions(center)) {
                if (level.getBlockState(pos).getBlock() instanceof SovietComputerCasingBlock) {
                    level.removeBlock(pos, false);
                }
            }
            for (BlockPos pos : displayPositions(center)) {
                Block block = level.getBlockState(pos).getBlock();
                if (block instanceof ElectroluminescentDisplayBlock) {
                    level.destroyBlock(pos, true);
                } else if (block instanceof SovietComputerCasingBlock) {
                    // 已封上的空位
                    level.removeBlock(pos, false);
                }
            }
        } finally {
            disassembling = false;
        }
    }

    /**
     * 外壳被破坏时，定位并摧毁所属控制器（整机解体，并掉落保留基地数据的计算机物品）。
     * <p>
     * 显示器只是组装/外观件，不携带任何基地数据，因此破坏显示器<strong>不会</strong>触发整机解体：
     * 结构变为未完成，控制器与其数据原样留在原地，补放一块显示器即可恢复。
     * 控制器位于结构最底层中心，因此在被破坏位置的 3x3 范围内、下方 0~2 层查找。
     */
    public static void destroyControllerForPart(Level level, BlockPos partPos) {
        if (disassembling || internalEdit) return;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -2; dy <= 0; dy++) {
                    BlockPos center = partPos.offset(dx, dy, dz);
                    if (!casingPositions(center).contains(partPos) && !displayPositions(center).contains(partPos)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(center);
                    if (state.getBlock() instanceof SovietComputerBlock
                            && level.getBlockEntity(center) instanceof SovietComputerBlockEntity computer) {
                        // 保留 NBT（owner/血量/电路）后再解体，避免外壳被破坏时丢失内容
                        ItemStack drop = SovietComputerBlock.createDropStack(level, computer, state);
                        level.removeBlock(center, false);
                        if (!drop.isEmpty()) Block.popResource(level, center, drop);
                        return;
                    }
                }
            }
        }
    }

    /**
     * 右键外壳/显示器时转发给所属控制器：结构完整则打开 GUI，否则提示。
     */
    public static InteractionResult interact(Level level, BlockPos pos, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        SovietComputerBlockEntity computer = findController(level, pos);
        if (computer == null) {
            return InteractionResult.PASS;
        }
        BlockPos center = computer.getBlockPos();
        if (!isComplete(level, center)) {
            serverPlayer.sendSystemMessage(Component.translatable("error.basecore.soviet_computer.incomplete"));
            return InteractionResult.FAIL;
        }
        if (!computer.canUse(serverPlayer.getUUID())) {
            serverPlayer.sendSystemMessage(Component.translatable("error.basecore.permission.right_click_block"));
            return InteractionResult.FAIL;
        }
        serverPlayer.openMenu(computer, buf -> buf.writeBlockPos(center));
        return InteractionResult.sidedSuccess(false);
    }

    /** 查找某位置所属的苏式基地计算机方块实体。 */
    public static SovietComputerBlockEntity findController(Level level, BlockPos pos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -2; dy <= 0; dy++) {
                    BlockPos center = pos.offset(dx, dy, dz);
                    BlockEntity be = level.getBlockEntity(center);
                    if (be instanceof SovietComputerBlockEntity computer) {
                        if (casingPositions(center).contains(pos) || displayPositions(center).contains(pos)) {
                            return computer;
                        }
                    }
                }
            }
        }
        return null;
    }

    /** 判断某个方块是否属于苏式计算机结构的一部分。 */
    public static boolean isPartOfStructure(Block block) {
        return block instanceof SovietComputerBlock
                || block instanceof SovietComputerCasingBlock
                || block instanceof ElectroluminescentDisplayBlock;
    }
}
