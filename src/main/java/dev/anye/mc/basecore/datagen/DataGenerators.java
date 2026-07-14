package dev.anye.mc.basecore.datagen;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.datagen.loot.Loot_BlockLootTables;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = BaseCore.MOD_ID)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new DataGen_BlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new DataGen_ItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new DataGen_Recipe(packOutput, lookupProvider));
        BlockTagsProvider blockTagsProvider = new DataGen_BlockTag(packOutput, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new DataGen_ItemTag(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(), new DataGen_LootTableProvider(packOutput, lookupProvider));
    }
}