package dev.anye.mc.basecore.block.placeholder;

import com.mojang.serialization.MapCodec;
import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.entity.PlaceholderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 占位方块：放置进度期间的无碰撞透明占位。
 * 防水采用原版光源方块（Light Block）的含水（waterlogging）机制：
 * - 实现 {@link SimpleWaterloggedBlock}（{@link net.minecraft.world.level.block.LiquidBlockContainer} 子接口），
 *   {@code canPlaceLiquid} 接受水、{@code placeLiquid} 把水写入 {@link #WATERLOGGED} 状态，
 *   水流/水桶只会让占位方块"含水"，**绝不会替换/冲掉方块本身**（岩浆不适用，恒拒绝）；
 * - 属性加 {@code forceSolidOn()}：legacySolid=true → blocksMotion()=true，挡住 Create 注液器/管道等
 *   按 `!blocksMotion()` 判定的流体灌入路径；
 * - {@link #canBeReplaced(BlockState, Fluid)} 恒返回 false：桶倒水走 canBeReplaced(Fluid) 变体
 *   （默认实现 `canBeReplaced() || !isSolid()`，无碰撞箱方块恒 true），显式拦截作双重保险。
 * 占位方块被流体替换会导致进行中的放置异常（物品已消耗、进度丢失、ACTIVE_PLACEMENTS 状态残留）。
 */
public class PlaceholderBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    private static final MapCodec<PlaceholderBlock> CODEC = simpleCodec(p -> new PlaceholderBlock());
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public PlaceholderBlock() {
        super(Properties.of()
                .strength(-1.0F)
                .noCollission()
                .noOcclusion()
                .noTerrainParticles()
                .forceSolidOn()
                .sound(SoundType.HEAVY_CORE));
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.empty();
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.empty();
    }

    @Override
    protected @NotNull VoxelShape getVisualShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.empty();
    }

    @Override
    protected @NotNull VoxelShape getInteractionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return Shapes.empty();
    }

    @Override
    protected boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return true;
    }

    /**
     * 邻块变化时若占位方块处于含水状态，调度一次水 tick（仿光源方块）。
     */
    @Override
    protected BlockState updateShape(
            BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    /**
     * 占位方块不可被其它方块替换，放置进度期间保持稳定。
     */
    @Override
    protected boolean canBeReplaced(BlockState pState, BlockPlaceContext pContext) {
        return false;
    }

    /**
     * 任何流体（水/岩浆桶、Create 管道等）都不可替换占位方块。
     * 默认实现为 `canBeReplaced() || !isSolid()`，无碰撞箱方块恒为 true；
     * 此处显式拦截（配合 forceSolidOn 双重保险，水经 SimpleWaterloggedBlock 含水而非替换）。
     */
    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PlaceholderBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide) return null;
        return createTickerHelper(pBlockEntityType, BlockEntityRegister.PLACEHOLDER.get(),
                (level, blockPos, blockState, be) -> be.tick(level, blockPos, blockState));
    }
}
