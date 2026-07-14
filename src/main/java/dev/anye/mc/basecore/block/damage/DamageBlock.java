package dev.anye.mc.basecore.block.damage;

import com.mojang.serialization.MapCodec;
import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.FunctionEntityBlock;
import dev.anye.mc.basecore.block.entity.DamageBlockEntity;
import dev.anye.mc.basecore.config.BasecoreConfig;
import dev.anye.mc.basecore.item.ItemRegister;
import dev.anye.mc.basecore.item.module.BasecoreModuleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DamageBlock extends FunctionEntityBlock {
    private static final MapCodec<DamageBlock> CODEC = simpleCodec(p -> new DamageBlock());
    private static final Random RANDOM = new Random();
    public List<ItemStack> drops = new ArrayList<>();
    public DamageBlock() {
        super();
    }

    @Override
    public MapCodec<? extends FunctionEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DamageBlockEntity(blockPos, blockState);
    }

    @Override
    public BlockState playerWillDestroy(Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @NotNull Player pPlayer) {
        pLevel.getBlockEntity(pPos, BlockEntityRegister.Damage.get()).ifPresent(damageBlockEntity -> {
            drops.clear();
            for (int i = 0; i < damageBlockEntity.items.getSlots(); i++) {
                ItemStack stack = damageBlockEntity.items.getStackInSlot(i);
                if (stack.isEmpty()) continue;

                if (BasecoreConfig.isComponentMode() && stack.getItem() instanceof BasecoreModuleItem) {
                    // In component mode, convert modules to parts (零件)
                    int partCount = 50 + RANDOM.nextInt(51); // 50-100
                    drops.add(new ItemStack(ItemRegister.PART.get(), partCount));
                } else {
                    drops.add(stack.copy());
                }
            }
            // Add salvage drops: 1-2 random items from chip/wire/antenna/copper_wire
            addSalvageDrops();
        });
        return super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    private void addSalvageDrops() {
        Item[] salvageItems = {
                ItemRegister.Chip.get(),
                ItemRegister.Wire.get(),
                ItemRegister.Antenna.get(),
                ItemRegister.Copper_Wire.get(),
        };
        int count = 1 + RANDOM.nextInt(2); // 1-2
        for (int i = 0; i < count; i++) {
            drops.add(new ItemStack(salvageItems[RANDOM.nextInt(salvageItems.length)], 1));
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
        if (drops.isEmpty()) return super.getDrops(pState, pParams);
        return drops;
    }
}