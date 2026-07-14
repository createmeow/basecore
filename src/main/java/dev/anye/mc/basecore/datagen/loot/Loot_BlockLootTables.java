package dev.anye.mc.basecore.datagen.loot;

import dev.anye.mc.basecore.block.BlockRegister;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class Loot_BlockLootTables extends BlockLootSubProvider {
    public Loot_BlockLootTables(HolderLookup.Provider lookupProvider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), lookupProvider);
    }
    @Override
    protected void generate() {
        dropSelf(BlockRegister.BASE_CORE.get());
        dropSelf(BlockRegister.DEFEND.get());
        dropSelf(BlockRegister.HASH_CHEST.get());
        // Damage and Nothing blocks drop nothing (no BlockItem registered)
        add(BlockRegister.Damage.get(), noDrop());
        add(BlockRegister.NOTHING.get(), noDrop());
    }
    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return BlockRegister.BLOCKS.getEntries().stream().map(holder -> (Block) holder.get())::iterator;
    }
}