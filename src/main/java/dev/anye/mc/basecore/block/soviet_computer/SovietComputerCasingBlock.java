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
 * 苏式基地计算机外壳。由控制器放置时自动填充，破坏后会导致整台计算机解体；
 * 右键外壳等同于右键控制器（打开 GUI），因为控制器位于结构内部。
 */
public class SovietComputerCasingBlock extends Block {
    public SovietComputerCasingBlock() {
        super(Properties.ofFullCopy(Blocks.IRON_BLOCK).sound(SoundType.METAL).strength(4.0F, 12.0F));
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return SovietComputerStructure.interact(level, pos, player);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        // 外壳按方块注册了物品能力（转发给控制器），能力结果会按坐标缓存，
        // 因此放置/移除时必须手动失效，否则漏斗仍会读到旧的空结果。
        if (!level.isClientSide) level.invalidateCapabilities(pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            level.invalidateCapabilities(pos);
            SovietComputerStructure.destroyControllerForPart(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
