package dev.anye.mc.basecore.block;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.block.basecore.BaseCoreBlock;
import dev.anye.mc.basecore.block.damage.DamageBlock;
import dev.anye.mc.basecore.block.defend.DefendBlock;
import dev.anye.mc.basecore.block.hash_chest.HashChestBlock;
import dev.anye.mc.basecore.item.ItemRegister;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class BlockRegister {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BaseCore.MOD_ID);

    public static final RegistryObject<Block> BASE_CORE = BLOCKS.register("basecore", BaseCoreBlock::new);
    public static final RegistryObject<Block> DEFEND = registryBlock("defend", DefendBlock::new);
    public static final RegistryObject<Block> Damage = BLOCKS.register("damage", DamageBlock::new);
    public static final RegistryObject<HashChestBlock> HASH_CHEST = registryBlock("hash_chest", HashChestBlock::new);


    private static <T extends Block> RegistryObject<T> registryBlock(String name, Supplier<T> block){
        RegistryObject<T> toReturn = BLOCKS.register(name,block);
        registryBlockItem(name,toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registryBlockItem(String name, RegistryObject<T> block) {
        return ItemRegister.ITEMS.register(name,()->new BlockItem(block.get(),new Item.Properties()));
    }
    public static void reg(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }
}
