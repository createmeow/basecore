package dev.anye.mc.basecore.datagen;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.item.EasyItem;
import dev.anye.mc.basecore.item.ItemRegister;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class DataGen_ItemModelProvider extends ItemModelProvider {
    public DataGen_ItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, BaseCore.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //simpleItem(ItemRegister.RangeModule);
        //simpleItem(ItemRegister.StrengthModule);
        ItemRegister.ITEMS.getEntries().forEach(itemRegistryObject -> {
            itemRegistryObject.ifPresent(item -> {
                if (item instanceof EasyItem){
                    simpleItem(itemRegistryObject);
                }
            });
        });
    }
    private ItemModelBuilder simpleItem(RegistryObject<Item> item){
        return withExistingParent(item.getId().getPath(),new ResourceLocation("item/generated")).texture("layer0",new ResourceLocation(BaseCore.MOD_ID,"item/"+item.getId().getPath()));
    }
}
