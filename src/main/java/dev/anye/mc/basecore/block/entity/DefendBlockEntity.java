package dev.anye.mc.basecore.block.entity;

import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.IHealth;
import dev.anye.mc.basecore.block.entity.ToDamageBlockEntity;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.item.ItemRegister;
import dev.anye.mc.basecore.item.module.BaseModuleItem;
import dev.anye.mc.basecore.menu.DefendMenu;
import dev.anye.mc.basecore.net.Net;
import dev.anye.mc.basecore.net.NetReg;
import dev.anye.mc.basecore.net.easy_net.EasyNetPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DefendBlockEntity extends ToDamageBlockEntity<BaseModuleItem> implements MenuProvider, IHealth {
    private static final int inventorySize = 27;
    private UUID owner;
    private int range = 15;
    private int health = 50;
    private int maxHealth = 50;
    private int invincibleTime = 0;
    private int attackTick = 0;
    private int type = 1;
    private float amount = 4f;
    private int showHealthTick = 0;
    private boolean hasThorns = false;
    private int autoRepairCount = 0;
    private int autoRepairTick = 0;

    protected final ContainerData data;

    public DefendBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegister.Defend.get(), pPos, pBlockState, inventorySize);
        this.data = new SimpleContainerData(3){
            @Override
            public int get(int i) {
                return switch (i){
                    case 0 -> DefendBlockEntity.this.range;
                    case 1 -> DefendBlockEntity.this.health;
                    case 2 -> DefendBlockEntity.this.maxHealth;
                    default -> 0;
                };
            }
            @Override
            public void set(int i, int i1) {
                switch (i){
                    case 0 -> DefendBlockEntity.this.range = i1;
                    case 1 -> DefendBlockEntity.this.health = i1;
                    case 2 -> DefendBlockEntity.this.maxHealth = i1;
                }
            }
        };
    }

    public void showHealth() {
        showHealthTick = 100;
    }

    public int getShowHealthTick() {
        return showHealthTick;
    }

    public boolean hasThorns() {
        return hasThorns;
    }

    @Nullable
    public BaseCoreBlockEntity getBaseCore() {
        if (getLevel() != null){
            return dev.anye.mc.basecore.basecore.BasecoreServerHelper.getBasecore(getLevel(),getBlockPos().getCenter());
        }
        return null;
    }

    @Override
    public void setOwner(UUID owner) {
        if (this.owner == null) this.owner = owner;
    }

    @Override
    public @Nullable UUID getOwner() {
        return owner;
    }

    @Override
    public boolean canUse(UUID uuid) {
        return owner != null && owner.equals(uuid);
    }

    @Override
    public void addHealth(int health) {
        this.health = Math.min(this.health + health, maxHealth);
        showHealth();
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    public void damage(int health) {
        if (invincibleTime <= 0) {
            this.health -= health;
            if (this.health < 0) this.health = 0;
            invincibleTime = 0;
            showHealth();
        }
    }

    @Override
    public int getModuleLvl(BaseModuleItem item) {
        return 0;
    }

    public void setType() {
        this.type++;
        if (type > 3) type = 1;
        showHealth();
        updateToTracking();
    }

    public void setType(int mode) {
        if (mode >= 1 && mode <= 3) {
            this.type = mode;
            showHealth();
            updateToTracking();
        }
    }

    public int getPlayerType() {
        return type;
    }

    public float getAmount() {
        return amount;
    }

    /** Get the count of a specific module type in the turret inventory. */
    public int getModuleCount(net.minecraft.world.item.Item moduleItem) {
        int total = 0;
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == moduleItem) {
                total += stack.getCount();
            }
        }
        return total;
    }

    /** Get the max allowed count for a module type. */
    public int getModuleMax(net.minecraft.world.item.Item moduleItem) {
        if (moduleItem == ItemRegister.RangeModule.get()) return 4;
        if (moduleItem == ItemRegister.DefModule.get()) return 2;
        if (moduleItem == ItemRegister.AutoRepairModule.get()) return 2;
        if (moduleItem == ItemRegister.ThornsModule.get()) return 1;
        if (moduleItem == ItemRegister.StrengthModule.get()) return 2;
        return 4;
    }

    private static void shootBeam(ServerLevel level, Vec3 start, Vec3 end, @Nullable ServerPlayer target) {
        end = end.add(0, 0.4, 0);
        Vec3 direction = end.subtract(start);
        double distance = direction.length();
        if (distance <= 0) return;
        Vec3 normalized = direction.normalize();
        int steps = (int) (distance / 0.5d);
        for (int i = 0; i < steps; i++) {
            double progress = i * 0.5d + 0.25d;
            Vec3 pos = start.add(normalized.scale(progress));
            if (target != null) {
                level.sendParticles(target, ParticleTypes.FLAME, false, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
            } else {
                level.getPlayers(p -> true).forEach(p -> level.sendParticles(p, ParticleTypes.FLAME, false, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0));
            }
        }
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (health <= 0) {
            pLevel.destroyBlock(pPos, false);
            return;
        }
        if (invincibleTime > 0) invincibleTime--;
        if (showHealthTick > 0) showHealthTick--;

        // Auto repair tick
        if (autoRepairCount > 0) {
            if (autoRepairTick >= 300) {
                autoRepairTick = 0;
                addHealth(autoRepairCount);
            } else autoRepairTick++;
        }

        if (attackTick >= 80) {
            attackTick = 0;

            float dmg = amount;

            BaseCoreBlockEntity baseCoreBlockEntity = getBaseCore();

            boolean[] has = {false};

            if (type == 2 || type == 3) {
                if (pLevel instanceof ServerLevel serverLevel) {
                    AABB rangeBox = new AABB(pPos).inflate(range);
                    serverLevel.getPlayers(serverPlayer -> {
                        if (this.owner != null && this.owner.equals(serverPlayer.getUUID())) return false;
                        if (baseCoreBlockEntity != null && baseCoreBlockEntity.canUse(serverPlayer.getUUID())) return false;
                        if (!rangeBox.contains(serverPlayer.position())) return false;
                        // 伪装效果检查：伪装成功且未被识破的玩家不会被攻击
                        if (serverPlayer.hasEffect(dev.anye.mc.basecore.effect.EffectRegister.Disguise)) {
                            if (!serverPlayer.getPersistentData().getBoolean("bcrm.basecore.isDiscern"))
                                return false;
                        }
                        return true;
                    }).forEach(serverPlayer -> {
                        if (has[0]) return;
                        has[0] = true;
                        // 攻击音效
                        serverPlayer.level().playSound(
                                null,
                                pPos.getX() + 0.5,
                                pPos.getY() + 0.5,
                                pPos.getZ() + 0.5,
                                net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_LAUNCH,
                                net.minecraft.sounds.SoundSource.BLOCKS,
                                1.0F,
                                serverPlayer.level().getRandom().nextFloat() * 0.4F + 0.8F
                        );
                        shootBeam(serverLevel, Vec3.atCenterOf(pPos), serverPlayer.position(), serverPlayer);
                        serverPlayer.hurt(pLevel.damageSources().fellOutOfWorld(), dmg);
                    });
                }
            }
            if (type == 1 || type == 3) {
                if (pLevel instanceof ServerLevel serverLevel) {
                    List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, new AABB(pPos).inflate(range),
                        e -> e.getType().getCategory() == MobCategory.MONSTER);
                    entities.forEach(livingEntity -> {
                        if (has[0]) return;
                        has[0] = true;
                        // 攻击音效
                        livingEntity.level().playSound(
                                null,
                                pPos.getX() + 0.5,
                                pPos.getY() + 0.5,
                                pPos.getZ() + 0.5,
                                net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_LAUNCH,
                                net.minecraft.sounds.SoundSource.BLOCKS,
                                1.0F,
                                livingEntity.level().getRandom().nextFloat() * 0.4F + 0.8F
                        );
                        shootBeam(serverLevel, Vec3.atCenterOf(pPos), livingEntity.position(), null);
                        livingEntity.hurt(livingEntity.damageSources().fellOutOfWorld(), dmg);
                    });
                }
            }
        } else {
            attackTick++;
        }
    }

    private void itemChange() {
        range = 15;
        maxHealth = 50;
        amount = 4f;
        hasThorns = false;
        autoRepairCount = 0;

        int rangeCount = 0;
        int defCount = 0;
        int strengthCount = 0;

        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            ItemStack itemStack = stack;
            var item = itemStack.getItem();
            int count = itemStack.getCount();

            if (item == ItemRegister.RangeModule.get())
                rangeCount = Math.min(rangeCount + count, 4);
            else if (item == ItemRegister.DefModule.get())
                defCount = Math.min(defCount + count, 2);
            else if (item == ItemRegister.AutoRepairModule.get())
                autoRepairCount = Math.min(autoRepairCount + count, 2);
            else if (item == ItemRegister.ThornsModule.get())
                hasThorns = true;
            else if (item == ItemRegister.StrengthModule.get())
                strengthCount = Math.min(strengthCount + count, 2);
        }

        range += rangeCount * 3;
        maxHealth += defCount * 50;
        amount = 4 + strengthCount * 2;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        itemChange();
    }

    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        range = pTag.contains("range") ? pTag.getInt("range") : 15;
        health = pTag.contains("health") ? pTag.getInt("health") : 50;
        maxHealth = pTag.contains("max_health") ? pTag.getInt("max_health") : 50;
        attackTick = pTag.getInt("tick");
        type = pTag.contains("type") ? pTag.getInt("type") : 1;
        showHealthTick = pTag.getInt("showHealthTick");
        if (pTag.contains("owner")) owner = pTag.getUUID("owner");
        itemChange();
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        pTag.putInt("range", range);
        pTag.putInt("health", health);
        pTag.putInt("max_health", maxHealth);
        pTag.putInt("tick", attackTick);
        pTag.putInt("type", type);
        pTag.putInt("showHealthTick", showHealthTick);
        if (owner != null) pTag.putUUID("owner", owner);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block_entity.basecore.defend");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new DefendMenu(i, inventory, this, this.data);
    }

    @Override
    public void updateToClient() {
    }

    @Override
    public void handlePacket(CompoundTag packet) {
    }

    public void updateToPlayer(ServerPlayer player) {
        if (level instanceof ServerLevel serverLevel) {
            CompoundTag data = new CompoundTag();
            data.putInt("block.x", getBlockPos().getX());
            data.putInt("block.y", getBlockPos().getY());
            data.putInt("block.z", getBlockPos().getZ());
            data.putInt("health", health);
            data.putInt("maxHealth", maxHealth);
            data.putInt("showHealthTick", showHealthTick);
            data.putInt("type", type);
            data.putString(Net.EASY_NET_KEY, NetReg.DEFEND_BLOCK_KEY);
            Net.sendToPlayer(new EasyNetPayload(data), player);
        }
    }

    public void updateToTracking() {
        if (level instanceof ServerLevel serverLevel) {
            CompoundTag data = new CompoundTag();
            data.putInt("block.x", getBlockPos().getX());
            data.putInt("block.y", getBlockPos().getY());
            data.putInt("block.z", getBlockPos().getZ());
            data.putInt("health", health);
            data.putInt("maxHealth", maxHealth);
            data.putInt("showHealthTick", showHealthTick);
            data.putInt("type", type);
            data.putString(Net.EASY_NET_KEY, NetReg.DEFEND_BLOCK_KEY);
            serverLevel.players().forEach(serverPlayer -> {
                Net.sendToPlayer(new EasyNetPayload(data), serverPlayer);
            });
        }
    }
}
