package dev.anye.mc.basecore.datagen;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.block.BlockRegister;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class DataGen_BlockStateProvider extends BlockStateProvider {
    public DataGen_BlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, BaseCore.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        //simpleBlock(Blocks.INDEX.get(), new ModelFile.UncheckedModelFile(modLoc("block/index")));
        simpleBlockWithItem(BlockRegister.BASE_CORE.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/basecore")));
        simpleBlockWithItem(BlockRegister.DEFEND.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/defend")));
        simpleBlockWithItem(BlockRegister.HASH_CHEST.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/defend")));
        simpleBlock(BlockRegister.Damage.get());
        /*
        simpleBlockWithItem(NUBlocks.THE_EIGHT_TRIGRAMS.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/the_eight_trigrams")));
        simpleBlockWithItem(NUBlocks.EQUIPMENT_ENHANCER.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/equipment_enhancer")));

         */
    }











    private void blockWithItem(RegistryObject<Block> blockRegistryObject){
        simpleBlockWithItem(blockRegistryObject.get(),cubeAll(blockRegistryObject.get()));
    }
}
