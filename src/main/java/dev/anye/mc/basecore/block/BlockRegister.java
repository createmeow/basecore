package dev.anye.mc.basecore.block;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.block.basecore.BaseCoreBlock;
import dev.anye.mc.basecore.block.damage.DamageBlock;
import dev.anye.mc.basecore.block.defend.DefendBlock;
import dev.anye.mc.basecore.block.hash_chest.HashChestBlock;
import dev.anye.mc.basecore.block.nothing.NothingBlock;
import dev.anye.mc.basecore.block.placeholder.PlaceholderBlock;
import dev.anye.mc.basecore.block.soviet_computer.ElectroluminescentDisplayBlock;
import dev.anye.mc.basecore.block.soviet_computer.SovietComputerBlock;
import dev.anye.mc.basecore.block.soviet_computer.SovietComputerCasingBlock;
import dev.anye.mc.basecore.block.stress_supply.StressSupplyBlock;
import dev.anye.mc.basecore.item.DefendBlockItem;
import dev.anye.mc.basecore.item.HashChestBlockItem;
import dev.anye.mc.basecore.item.ItemRegister;
import dev.anye.mc.basecore.item.SovietComputerBlockItem;
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

    // 机械动力联动内容
    public static final DeferredHolder<Block, StressSupplyBlock> STRESS_SUPPLY = BLOCKS.register("stress_supply", StressSupplyBlock::new);
    public static final DeferredHolder<Block, SovietComputerBlock> SOVIET_COMPUTER = BLOCKS.register("soviet_computer", SovietComputerBlock::new);
    public static final DeferredHolder<Block, SovietComputerCasingBlock> SOVIET_COMPUTER_CASING = BLOCKS.register("soviet_computer_casing", SovietComputerCasingBlock::new);
    public static final DeferredHolder<Block, ElectroluminescentDisplayBlock> ELECTROLUMINESCENT_DISPLAY = BLOCKS.register("electroluminescent_display", ElectroluminescentDisplayBlock::new);

    // Custom BlockItems: register separately from blocks
    static {
        ItemRegister.ITEMS.register("defend", DefendBlockItem::new);
        ItemRegister.ITEMS.register("hash_chest", HashChestBlockItem::new);
        ItemRegister.ITEMS.register("placeholder", () -> new BlockItem(BlockRegister.PLACEHOLDER.get(), new Item.Properties()));
        ItemRegister.ITEMS.register("stress_supply", () -> new BlockItem(STRESS_SUPPLY.get(), new Item.Properties()));
        ItemRegister.ITEMS.register("soviet_computer", SovietComputerBlockItem::new);
        ItemRegister.ITEMS.register("electroluminescent_display", () -> new BlockItem(ELECTROLUMINESCENT_DISPLAY.get(), new Item.Properties()));
    }

    public static void reg(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }
}
