package dev.anye.mc.basecore.datagen;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.block.BlockRegister;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class DataGen_BlockStateProvider extends BlockStateProvider {
    public DataGen_BlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, BaseCore.MOD_ID, exFileHelper);
    }
    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(BlockRegister.BASE_CORE.get(), new ModelFile.UncheckedModelFile(BaseCore.MOD_ID + ":block/basecore"));
        simpleBlockWithItem(BlockRegister.DEFEND.get(), new ModelFile.UncheckedModelFile(BaseCore.MOD_ID + ":block/defend"));
        simpleBlockWithItem(BlockRegister.Damage.get(), new ModelFile.UncheckedModelFile(BaseCore.MOD_ID + ":block/damage"));
        simpleBlockWithItem(BlockRegister.HASH_CHEST.get(), new ModelFile.UncheckedModelFile(BaseCore.MOD_ID + ":block/hash_chest"));
    }
}