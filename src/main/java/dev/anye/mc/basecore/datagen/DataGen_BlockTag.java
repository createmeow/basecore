package dev.anye.mc.basecore.datagen;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.block.BlockRegister;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DataGen_BlockTag extends BlockTagsProvider {
    public DataGen_BlockTag(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, BaseCore.MOD_ID, existingFileHelper);
    }
    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE)
                .add(BlockRegister.BASE_CORE.get())
                .add(BlockRegister.DEFEND.get())
                .add(BlockRegister.HASH_CHEST.get())
                .add(BlockRegister.Damage.get());
        tag(net.minecraft.tags.BlockTags.NEEDS_IRON_TOOL)
                .add(BlockRegister.BASE_CORE.get())
                .add(BlockRegister.DEFEND.get())
                .add(BlockRegister.HASH_CHEST.get());
    }
}