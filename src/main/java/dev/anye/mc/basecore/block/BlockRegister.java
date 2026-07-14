package dev.anye.mc.basecore.block;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.block.basecore.BaseCoreBlock;
import dev.anye.mc.basecore.block.damage.DamageBlock;
import dev.anye.mc.basecore.block.defend.DefendBlock;
import dev.anye.mc.basecore.block.hash_chest.HashChestBlock;
import dev.anye.mc.basecore.block.nothing.NothingBlock;
import dev.anye.mc.basecore.item.ItemRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockRegister {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, BaseCore.MOD_ID);

    public static final DeferredHolder<Block, BaseCoreBlock> BASE_CORE = BLOCKS.register("basecore", BaseCoreBlock::new);
    public static final DeferredHolder<Block, DefendBlock> DEFEND = registryBlock("defend", DefendBlock::new);
    public static final DeferredHolder<Block, Block> Damage = BLOCKS.register("damage", DamageBlock::new);
    public static final DeferredHolder<Block, HashChestBlock> HASH_CHEST = registryBlock("hash_chest", HashChestBlock::new);
    public static final DeferredHolder<Block, NothingBlock> NOTHING = BLOCKS.register("nothing", NothingBlock::new);

    private static <T extends Block> DeferredHolder<Block, T> registryBlock(String name, Supplier<T> block){
        DeferredHolder<Block, T> toReturn = BLOCKS.register(name, block);
        registryBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredHolder<Item, BlockItem> registryBlockItem(String name, DeferredHolder<Block, T> block) {
        return ItemRegister.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
    public static void reg(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }
}