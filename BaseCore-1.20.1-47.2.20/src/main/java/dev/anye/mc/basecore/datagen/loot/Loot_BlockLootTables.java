package dev.anye.mc.basecore.datagen.loot;

import dev.anye.mc.basecore.block.BlockRegister;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class Loot_BlockLootTables extends BlockLootSubProvider {
    public Loot_BlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {

        this.dropSelf(BlockRegister.BASE_CORE.get());
        this.dropSelf(BlockRegister.DEFEND.get());
        this.dropSelf(BlockRegister.Damage.get());
        this.dropSelf(BlockRegister.HASH_CHEST.get());

    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return BlockRegister.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
