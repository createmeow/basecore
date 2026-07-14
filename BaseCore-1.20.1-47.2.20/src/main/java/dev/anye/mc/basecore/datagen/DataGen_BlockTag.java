package dev.anye.mc.basecore.datagen;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.block.BlockRegister;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DataGen_BlockTag extends BlockTagsProvider {
    public DataGen_BlockTag(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, BaseCore.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                BlockRegister.BASE_CORE.get(),
                BlockRegister.DEFEND.get(),
                BlockRegister.Damage.get(),
                BlockRegister.HASH_CHEST.get()
        );
        this.tag(BlockTags.NEEDS_IRON_TOOL).add(
                BlockRegister.BASE_CORE.get(),
                BlockRegister.DEFEND.get(),
                BlockRegister.Damage.get(),
                BlockRegister.HASH_CHEST.get()
        );
    }
}
