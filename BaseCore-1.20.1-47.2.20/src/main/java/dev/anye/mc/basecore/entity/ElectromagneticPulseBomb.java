package dev.anye.mc.basecore.entity;

import dev.anye.mc.basecore.basecore.BasecoreServerHelper;
import dev.anye.mc.basecore.block.BlockRegister;
import dev.anye.mc.basecore.block.IHealth;
import dev.anye.mc.basecore.block.IOwner;
import dev.anye.mc.basecore.block.InventoryBlockEntity;
import dev.anye.mc.basecore.block.entity.DamageBlockEntity;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.item.ItemRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ElectromagneticPulseBomb extends ThrowableItemProjectile {
    public ElectromagneticPulseBomb(EntityType<? extends ElectromagneticPulseBomb> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ElectromagneticPulseBomb(Level pLevel, Player pPlayer) {
        super(EntityTypeRegister.ElectromagneticPulseBomb.get(),pPlayer,pLevel);
    }

    public ElectromagneticPulseBomb(Level level) {
        super(EntityTypeRegister.ElectromagneticPulseBomb.get(),level);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return ItemRegister.ElectromagneticPulseBombItem.get();
    }

    private ParticleOptions getParticle() {
        ItemStack itemstack = this.getItemRaw();
        return (ParticleOptions)(itemstack.isEmpty() ? ParticleTypes.ITEM_SNOWBALL : new ItemParticleOption(ParticleTypes.ITEM, itemstack));
    }

    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 3) {
            ParticleOptions particleoptions = this.getParticle();

            for(int i = 0; i < 8; ++i) {
                this.level().addParticle(particleoptions, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    MobEffectInstance mobEffectInstance = new MobEffectInstance(MobEffects.DARKNESS,200,1);
    @Override
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        if (this.level() instanceof ServerLevel serverLevel){
            serverLevel.getPlayers(serverPlayer -> serverPlayer.distanceToSqr(pResult.getLocation()) < 25).forEach(serverPlayer -> {
                serverPlayer.addEffect(mobEffectInstance);
                serverPlayer.hurt(this.damageSources().fellOutOfWorld(),4);
            });

            BlockPos centerPos = BlockPos.containing(pResult.getLocation()); // 以碰撞点为中心
            double searchRange = 5.0;
            getNearbyBlockEntities(
                    serverLevel,
                    centerPos,
                    searchRange,
                    blockState -> blockState.getBlock() == BlockRegister.BASE_CORE.get() || blockState.getBlock() == BlockRegister.DEFEND.get()
            ).forEach((blockPos, blockEntity) -> {
                if (blockEntity instanceof IHealth IHealth) {
                    if (this.getOwner() instanceof ServerPlayer serverPlayer && blockEntity instanceof IOwner iOwner) {
                        if (serverPlayer.getUUID().equals(iOwner.getOwner()) || iOwner.canUse(serverPlayer.getUUID()))
                            return;
                    }
                    IHealth.damage(50);
                    if (IHealth.getHealth() <= 0) {
                        if (blockEntity instanceof InventoryBlockEntity inventoryBlockEntity) {
                            DamageBlockEntity damageBlockEntity = new DamageBlockEntity(blockEntity.getBlockPos(), BlockRegister.Damage.get().defaultBlockState());
                            damageBlockEntity.setItems(inventoryBlockEntity.getItems());
                            serverLevel.setBlock(inventoryBlockEntity.getBlockPos(), BlockRegister.Damage.get().defaultBlockState(), Block.UPDATE_ALL);
                            if (blockEntity instanceof BaseCoreBlockEntity baseCoreBlockEntity) {
                                BasecoreServerHelper.delEntityAndSend(baseCoreBlockEntity);
                                //BasecoreServerHelper.delAndSend(inventoryBlockEntity.getBlockPos());
                                //BasecoreServerHelper.upToClient();
                            }
                            serverLevel.setBlockEntity(damageBlockEntity);
                        }
                    }else serverLevel.sendBlockUpdated(blockPos, blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
                }
            });
            serverLevel.broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }


    public static Map<BlockPos, BlockEntity> getNearbyBlockEntities(ServerLevel level, BlockPos center, double range, Predicate<BlockState> blockPredicate) {
        Map<BlockPos, BlockEntity> foundEntities = new HashMap<>();
        int minX = (int) Math.floor(center.getX() - range);
        int minY = (int) Math.floor(center.getY() - range);
        int minZ = (int) Math.floor(center.getZ() - range);
        int maxX = (int) Math.ceil(center.getX() + range);
        int maxY = (int) Math.ceil(center.getY() + range);
        int maxZ = (int) Math.ceil(center.getZ() + range);

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutablePos.set(x, y, z);
                    BlockState blockState = level.getBlockState(mutablePos);
                    if (blockPredicate.test(blockState)) {
                        BlockEntity blockEntity = level.getBlockEntity(mutablePos);
                        if (blockEntity != null) {
                            foundEntities.put(mutablePos.immutable(), blockEntity);
                        }
                    }
                }
            }
        }
        return foundEntities;
    }
}
