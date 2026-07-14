package dev.anye.mc.basecore.block;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.block.entity.DamageBlockEntity;
import dev.anye.mc.basecore.block.entity.DefendBlockEntity;
import dev.anye.mc.basecore.block.entity.HashChestBlockEntity;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityRegister {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BaseCore.MOD_ID);

    public static final RegistryObject<BlockEntityType<BaseCoreBlockEntity>> BASECORE = BLOCK_ENTITIES.register("basecore",()->BlockEntityType.Builder.of(BaseCoreBlockEntity::new, BlockRegister.BASE_CORE.get()).build(null));
    public static final RegistryObject<BlockEntityType<DamageBlockEntity>> Damage = BLOCK_ENTITIES.register("damage",()->BlockEntityType.Builder.of(DamageBlockEntity::new, BlockRegister.Damage.get()).build(null));
    public static final RegistryObject<BlockEntityType<DefendBlockEntity>> Defend = BLOCK_ENTITIES.register("defend",()->BlockEntityType.Builder.of(DefendBlockEntity::new, BlockRegister.DEFEND.get()).build(null));
    public static final RegistryObject<BlockEntityType<HashChestBlockEntity>> HashChest = BLOCK_ENTITIES.register("hash_chest",()->BlockEntityType.Builder.of(HashChestBlockEntity::new, BlockRegister.HASH_CHEST.get()).build(null));



    public static void reg(IEventBus eventBus){
        BLOCK_ENTITIES.register(eventBus);
    }
}
