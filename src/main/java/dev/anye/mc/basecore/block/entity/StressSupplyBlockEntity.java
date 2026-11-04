package dev.anye.mc.basecore.block.entity;

import dev.anye.mc.basecore.basecore.BasecoreServerHelper;
import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.config.BasecoreConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 不间断应力供应器方块实体。
 * <p>
 * 存储 air（默认上限 1800），每 {@code supply_air_interval_ticks}（默认 2 秒）消耗 1 air，
 * air > 0 时向相邻的基地核心 / 防御炮提供 {@code supply_rpm} 转速与 {@code supply_stress} 应力。
 * <p>
 * 当自身位于某基地核心范围外扩 {@code supply_link_range}（默认 6）格以内时，
 * 与基地核心血量双向同步：核心掉血自身同步掉血，自身掉血核心也同步掉血。
 */
public class StressSupplyBlockEntity extends BlockEntity {
    private static final String NBT_AIR = "air";
    private static final String NBT_HEALTH = "health";
    private static final String NBT_MAX_HEALTH = "max_health";
    private static final String NBT_LAST_BASE_HEALTH = "last_base_health";

    private int air = 0;
    private int health = 500;
    private int maxHealth = 500;
    /** 上一次同步时的基地核心血量，用于判断血量变化来源。 */
    private int lastBaseHealth = -1;
    private int airTick = 0;
    private int linkTick = 0;

    public StressSupplyBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegister.STRESS_SUPPLY.get(), pos, state);
    }

    public int getAir() {
        return air;
    }

    public void setAir(int air) {
        this.air = Math.max(0, Math.min(air, BasecoreConfig.getSupplyAirCapacity()));
        setChanged();
    }

    /** air > 0 时才会输出旋转动力。 */
    public boolean isActive() {
        return air > 0;
    }

    public float getStressOutput() {
        return BasecoreConfig.getSupplyStress();
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        // air 消耗：每 2 秒（默认 40 tick）消耗 1 点
        if (++airTick >= BasecoreConfig.getSupplyAirIntervalTicks()) {
            airTick = 0;
            if (air > 0) {
                air--;
                setChanged();
            }
        }

        // 血量同步：每 10 tick 检查一次
        if (--linkTick <= 0) {
            linkTick = 10;
            syncHealth(level, pos);
        }

        if (health <= 0) {
            level.destroyBlock(pos, false);
        }
    }

    private void syncHealth(Level level, BlockPos pos) {
        BaseCoreBlockEntity baseCore = BasecoreServerHelper.getBasecore(
                level, Vec3.atCenterOf(pos), BasecoreConfig.getSupplyLinkRange());
        if (baseCore == null) return;

        maxHealth = baseCore.getMaxHealth();
        if (health > maxHealth) health = maxHealth;

        int baseHealth = baseCore.getHealth();
        if (baseHealth != lastBaseHealth) {
            // 基地核心血量变化 → 同步到自身
            health = baseHealth;
            lastBaseHealth = baseHealth;
        } else if (health != lastBaseHealth) {
            // 自身血量变化 → 同步到基地核心
            baseCore.setHealth(health);
            lastBaseHealth = health;
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(NBT_AIR, air);
        tag.putInt(NBT_HEALTH, health);
        tag.putInt(NBT_MAX_HEALTH, maxHealth);
        tag.putInt(NBT_LAST_BASE_HEALTH, lastBaseHealth);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        air = tag.getInt(NBT_AIR);
        if (tag.contains(NBT_HEALTH)) health = tag.getInt(NBT_HEALTH);
        if (tag.contains(NBT_MAX_HEALTH)) maxHealth = tag.getInt(NBT_MAX_HEALTH);
        if (tag.contains(NBT_LAST_BASE_HEALTH)) lastBaseHealth = tag.getInt(NBT_LAST_BASE_HEALTH);
    }
}
