package dev.anye.mc.basecore.datagen;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.block.BlockRegister;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class DataGen_ItemModelProvider extends ItemModelProvider {
    public DataGen_ItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, BaseCore.MOD_ID, existingFileHelper);
    }
    @Override
    protected void registerModels() {
        // Item models already exist in src/main/resources/assets/basecore/models/item/
        // They reference block textures directly, no need to generate them here.
    }
}