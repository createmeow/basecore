package dev.anye.mc.basecore.datagen;

import dev.anye.mc.basecore.BaseCore;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = BaseCore.MOD_ID,bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event){
        DataGenerator dataGenerator = event.getGenerator();
        PackOutput packOutput = dataGenerator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        dataGenerator.addProvider(event.includeClient(),new DataGen_Recipe(packOutput));
        dataGenerator.addProvider(event.includeClient(),DataGen_LootTableProvider.create(packOutput));

        dataGenerator.addProvider(event.includeClient(),new DataGen_BlockStateProvider(packOutput,existingFileHelper));
        dataGenerator.addProvider(event.includeClient(),new DataGen_ItemModelProvider(packOutput,existingFileHelper));

        DataGen_BlockTag blockTag = dataGenerator.addProvider(event.includeClient(), new DataGen_BlockTag(packOutput,lookup,existingFileHelper));
        dataGenerator.addProvider(event.includeClient(),new DataGen_ItemTag(packOutput,lookup,blockTag.contentsGetter(),existingFileHelper));


    }
}
