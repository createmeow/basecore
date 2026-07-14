package dev.anye.mc.basecore.datagen;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.datagen.loot.Loot_BlockLootTables;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DataGen_LootTableProvider extends LootTableProvider {
    public DataGen_LootTableProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, Set.of(), List.of(
                new SubProviderEntry(Loot_BlockLootTables::new, LootContextParamSets.BLOCK)
        ), pRegistries);
    }
}