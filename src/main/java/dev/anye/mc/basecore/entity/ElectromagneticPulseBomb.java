package dev.anye.mc.basecore.entity;

import dev.anye.mc.basecore.basecore.BasecoreServerHelper;
import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.BlockRegister;
import dev.anye.mc.basecore.block.IHealth;
import dev.anye.mc.basecore.block.InventoryBlockEntity;
import dev.anye.mc.basecore.block.entity.DamageBlockEntity;
import dev.anye.mc.basecore.block.entity.DefendBlockEntity;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

import java.util.HashMap;
import java.util.Map;

public class ElectromagneticPulseBomb extends ThrowableItemProjectile {
    public ElectromagneticPulseBomb(EntityType<? extends ElectromagneticPulseBomb> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ElectromagneticPulseBomb(Level pLevel, LivingEntity pShooter) {
        super(EntityTypeRegister.ElectromagneticPulseBomb.get(), pShooter, pLevel);
    }

    @Override
    protected Item getDefaultItem() {
        return dev.anye.mc.basecore.item.ItemRegister.ElectromagneticPulseBombItem.get();
    }

    @Override
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        if (!this.level().isClientSide) {
            BlockPos centerPos = BlockPos.containing(pResult.getLocation());
            ServerLevel serverLevel = (ServerLevel) level();

            Map<BlockPos, BlockEntity> baseCores = getNearbyBlockEntities(
                    serverLevel,
                    centerPos,
                    1.5,
                    blockState -> blockState.is(BlockRegister.BASE_CORE.get())
            );

            baseCores.forEach((blockPos, blockEntity) -> {
                if (!(blockEntity instanceof BaseCoreBlockEntity baseCoreBlockEntity)) return;

                if (this.getOwner() instanceof ServerPlayer serverPlayer) {
                    if (baseCoreBlockEntity.canUse(serverPlayer.getUUID())) return;
                }

                baseCoreBlockEntity.damage(50);
                baseCoreBlockEntity.getEntityData().addInterferenceTime(300);
                baseCoreBlockEntity.updateToClient();

                if (baseCoreBlockEntity.getHealth() <= 0) {
                    DamageBlockEntity damageBlockEntity = new DamageBlockEntity(
                            blockPos, BlockRegister.Damage.get().defaultBlockState());
                    damageBlockEntity.setItems(((InventoryBlockEntity) blockEntity).getItems());
                    serverLevel.setBlock(blockPos, BlockRegister.Damage.get().defaultBlockState(), Block.UPDATE_ALL);
                    BasecoreServerHelper.delEntityAndSend(baseCoreBlockEntity);
                    serverLevel.setBlockEntity(damageBlockEntity);
                }
            });

            Map<BlockPos, BlockEntity> defends = getNearbyBlockEntities(
                    serverLevel,
                    centerPos,
                    1.5,
                    blockState -> blockState.is(BlockRegister.DEFEND.get())
            );

            defends.forEach((blockPos, blockEntity) -> {
                if (!(blockEntity instanceof DefendBlockEntity defendBlockEntity)) return;

                if (this.getOwner() instanceof ServerPlayer serverPlayer) {
                    if (defendBlockEntity.canUse(serverPlayer.getUUID())) return;
                }

                defendBlockEntity.damage(50);

                if (defendBlockEntity.getHealth() <= 0) {
                    DamageBlockEntity damageBlockEntity = new DamageBlockEntity(
                            blockPos, BlockRegister.Damage.get().defaultBlockState());
                    damageBlockEntity.setItems(defendBlockEntity.getItems());
                    serverLevel.setBlock(blockPos, BlockRegister.Damage.get().defaultBlockState(), Block.UPDATE_ALL);
                    serverLevel.setBlockEntity(damageBlockEntity);
                }
            });

            serverLevel.explode(this, pResult.getLocation().x, pResult.getLocation().y, pResult.getLocation().z, 2.0f, Level.ExplosionInteraction.NONE);
            this.discard();
        }
    }

    private static Map<BlockPos, BlockEntity> getNearbyBlockEntities(ServerLevel level, BlockPos center, double range, java.util.function.Predicate<BlockState> blockPredicate) {
        Map<BlockPos, BlockEntity> foundEntities = new HashMap<>();
        int minX = center.getX() - (int) Math.ceil(range);
        int minY = center.getY() - (int) Math.ceil(range);
        int minZ = center.getZ() - (int) Math.ceil(range);
        int maxX = center.getX() + (int) Math.ceil(range);
        int maxY = center.getY() + (int) Math.ceil(range);
        int maxZ = center.getZ() + (int) Math.ceil(range);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (blockPredicate.test(state)) {
                        BlockEntity be = level.getBlockEntity(pos);
                        if (be != null) {
                            foundEntities.put(pos, be);
                        }
                    }
                }
            }
        }
        return foundEntities;
    }
}
