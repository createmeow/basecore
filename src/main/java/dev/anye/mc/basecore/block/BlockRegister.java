package dev.anye.mc.basecore.block;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.block.basecore.BaseCoreBlock;
import dev.anye.mc.basecore.block.damage.DamageBlock;
import dev.anye.mc.basecore.block.defend.DefendBlock;
import dev.anye.mc.basecore.block.hash_chest.HashChestBlock;
import dev.anye.mc.basecore.block.nothing.NothingBlock;
import dev.anye.mc.basecore.block.placeholder.PlaceholderBlock;
import dev.anye.mc.basecore.item.DefendBlockItem;
import dev.anye.mc.basecore.item.HashChestBlockItem;
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
    public static final DeferredHolder<Block, DefendBlock> DEFEND = BLOCKS.register("defend", DefendBlock::new);
    public static final DeferredHolder<Block, Block> Damage = BLOCKS.register("damage", DamageBlock::new);
    public static final DeferredHolder<Block, HashChestBlock> HASH_CHEST = BLOCKS.register("hash_chest", HashChestBlock::new);
    public static final DeferredHolder<Block, NothingBlock> NOTHING = BLOCKS.register("nothing", NothingBlock::new);
    public static final DeferredHolder<Block, PlaceholderBlock> PLACEHOLDER = BLOCKS.register("placeholder", PlaceholderBlock::new);

    // Custom BlockItems: register separately from blocks
    static {
        ItemRegister.ITEMS.register("defend", DefendBlockItem::new);
        ItemRegister.ITEMS.register("hash_chest", HashChestBlockItem::new);
        ItemRegister.ITEMS.register("placeholder", () -> new BlockItem(BlockRegister.PLACEHOLDER.get(), new Item.Properties()));
    }

    public static void reg(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }
}
