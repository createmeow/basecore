package dev.anye.mc.basecore.block.soviet_computer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

/**
 * 电致发光显示器方块。放入苏式基地计算机中层四边的任一空位即可完成组装（只需 1 个）。
 * <p>
 * 显示器只是外观/组装件，本身不保存任何基地数据（没有方块实体）；基地的血量、成员、范围、
 * 电路等内容全部保存在计算机控制器里。因此破坏显示器只会让结构变为“未完成”，
 * 计算机方块及其数据原样留在原地，换一块新显示器即可继续工作。
 * 右键显示器等同于右键控制器（打开 GUI），仅作为显示面板使用。
 */
public class ElectroluminescentDisplayBlock extends Block {
    public ElectroluminescentDisplayBlock() {
        super(Properties.ofFullCopy(Blocks.GLOWSTONE).sound(SoundType.GLASS).lightLevel(state -> 12));
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return SovietComputerStructure.interact(level, pos, player);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        // 显示器按方块注册了物品能力（转发给控制器），能力结果按坐标缓存，需手动失效。
        if (!level.isClientSide) level.invalidateCapabilities(pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            level.invalidateCapabilities(pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
