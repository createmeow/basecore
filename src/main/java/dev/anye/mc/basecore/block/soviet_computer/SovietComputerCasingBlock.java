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
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            SovietComputerStructure.destroyControllerForPart(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
