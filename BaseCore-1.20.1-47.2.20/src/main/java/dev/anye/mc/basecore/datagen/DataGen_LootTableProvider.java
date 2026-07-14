package dev.anye.mc.basecore.datagen;

import dev.anye.mc.basecore.datagen.loot.Loot_BlockLootTables;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;

public class DataGen_LootTableProvider {
    public static LootTableProvider create(PackOutput output){
        return new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(Loot_BlockLootTables::new, LootContextParamSets.BLOCK)
        ));
    }
}
