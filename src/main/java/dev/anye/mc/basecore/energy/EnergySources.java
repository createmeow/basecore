package dev.anye.mc.basecore.energy;

import com.mojang.serialization.Codec;
import dev.anye.mc.basecore.BaseCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

/**
 * 基地能源（Rs）来源。
 * <p>
 * 能源填充量通过 NeoForge 数据映射（数据包）定义，路径为
 * {@code data/<namespace>/data_maps/item/energy_value.json}，内容形如：
 * <pre>{@code
 * { "values": { "minecraft:redstone": 10, "minecraft:redstone_block": 90 } }
 * }</pre>
 * 未提供数据映射时回落到内置默认值：红石 10 Rs、红石块 90 Rs。
 */
public final class EnergySources {
    public static final DataMapType<Item, Integer> ENERGY_VALUE = DataMapType
            .builder(ResourceLocation.fromNamespaceAndPath(BaseCore.MOD_ID, "energy_value"),
                    Registries.ITEM, Codec.INT)
            .synced(Codec.INT, false)
            .build();

    /** 默认值（未安装任何数据包时生效）。 */
    public static final int DEFAULT_REDSTONE_RS = 10;
    public static final int DEFAULT_REDSTONE_BLOCK_RS = 90;

    private EnergySources() {
    }

    public static void register(RegisterDataMapTypesEvent event) {
        event.register(ENERGY_VALUE);
    }

    /** 该物品堆可提供的能源量（Rs），0 表示不是能源物品。 */
    public static int getRs(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Integer value = stack.getItemHolder().getData(ENERGY_VALUE);
        if (value != null) return Math.max(0, value);
        Item item = stack.getItem();
        if (item == Items.REDSTONE) return DEFAULT_REDSTONE_RS;
        if (item == Items.REDSTONE_BLOCK) return DEFAULT_REDSTONE_BLOCK_RS;
        return 0;
    }

    public static boolean isEnergySource(ItemStack stack) {
        return getRs(stack) > 0;
    }
}
