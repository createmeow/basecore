package dev.anye.mc.basecore.block.entity.basecore;

import com.mojang.logging.LogUtils;
import dev.anye.mc.basecore.block.*;
import dev.anye.mc.basecore.block.entity.ToDamageBlockEntity;
import dev.anye.mc.basecore.item.ItemRegister;
import dev.anye.mc.basecore.item.module.BasecoreModuleItem;
import dev.anye.mc.basecore.item.module.basecore.*;
import dev.anye.mc.basecore.menu.BaseCoreMenu;
import dev.anye.mc.basecore.net.Net;
import dev.anye.mc.basecore.net.NetReg;
import dev.anye.mc.basecore.net.easy_net.EasyNetPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class BaseCoreBlockEntity extends ToDamageBlockEntity<BasecoreModuleItem> implements MenuProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int inventorySize = 27;
    private final Map<BasecoreModuleItem, Integer> modules = new HashMap<>();
    private @Nullable UUID owner = null;
    private final BasecoreBlockEntityData entityData;
    protected final ContainerData data;
    /** True while {@link #loadAdditional} is running; suppresses {@link #setChanged()} side effects. */
    private boolean loading = false;
    /** 当前能源（Rs）。 */
    private int energy = 0;
    /** 距下一次扣除 1 Rs 的 tick 数。 */
    private int energyTick = 0;
    /** 距下一次尝试补充能源的 tick 数。 */
    private int refuelTick = 0;
    /** 当前能源是否足以继续运行。 */
    private boolean energyPowered = false;
    /** 漏斗专用的只进不出物品处理器，延迟创建。 */
    private net.neoforged.neoforge.items.IItemHandler energyInputHandler;
    /** 零件升级界面中独立的能源输入槽（红石 / 红石块），与模块槽位分离。 */
    private final net.neoforged.neoforge.items.ItemStackHandler energySlot =
            new net.neoforged.neoforge.items.ItemStackHandler(1) {
                @Override
                protected void onContentsChanged(int slot) {
                    BaseCoreBlockEntity.this.setChanged();
                }
            };

    public BaseCoreBlockEntity(BlockPos pPos, BlockState pBlockState) {
        this(BlockEntityRegister.BASECORE.get(), pPos, pBlockState, inventorySize);
    }

    /** 供继承的多方块控制器（如苏式基地计算机）复用基地核心逻辑。 */
    protected BaseCoreBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type,
                                  BlockPos pPos, BlockState pBlockState, int inventorySize) {
        super(type, pPos, pBlockState, inventorySize);
        entityData = new BasecoreBlockEntityData();
        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> BaseCoreBlockEntity.this.entityData.getHealth();
                    case 1 -> BaseCoreBlockEntity.this.entityData.getMaxHealth();
                    case 2 -> BaseCoreBlockEntity.this.entityData.getRange();
                    case 3 -> BaseCoreBlockEntity.this.energy;
                    case 4 -> BaseCoreBlockEntity.this.getMaxEnergy();
                    default -> 0;
                };
            }
            @Override
            public void set(int i, int i1) {
                switch (i) {
                    case 0 -> BaseCoreBlockEntity.this.entityData.setHealth(i1);
                }
            }
            @Override
            public int getCount() {
                return 5;
            }
        };
    }

    public BasecoreBlockEntityData getEntityData() {
        return entityData;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (loading) return;
        itemChange();
        updateToClient();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        itemChange();
    }

    public void setOwner(UUID owner) {
        if (this.owner == null) this.owner = owner;
        entityData.setOwner(this.owner);
        updateToClient();
    }

    public void setOwnerWithName(UUID owner, String playerName) {
        if (this.owner == null) this.owner = owner;
        entityData.setOwner(this.owner);
        entityData.setOwnerName(playerName);
        // 仅当基地尚无名称时生成唯一名称；重新放置已有名称的基地时保留原名
        if (entityData.getName().isEmpty()) {
            entityData.setName(dev.anye.mc.basecore.basecore.BasecoreServerHelper.getUniqueBaseName(playerName));
        }
        updateToClient();
    }

    public @Nullable UUID getOwner() {
        return owner;
    }

    public boolean canUse(UUID user) {
        return check(user, owner);
    }

    public boolean check(UUID user, UUID owner) {
        return owner == null || user.equals(owner) || entityData.hasPermission(user);
    }

    private void itemChange() {
        entityData.setDefaultMaxHealth();
        entityData.setDefaultRange();
        entityData.resetEnemy();
        Map<BasecoreModuleItem, Integer> tmap = new HashMap<>();
        for (int i = 0; i < items.getSlots(); i++) {
            if (items.getStackInSlot(i).getItem() instanceof BasecoreModuleItem basecoreModuleItem) {
                int count = items.getStackInSlot(i).getCount();
                // Negative effect modules cap at 2
                if (basecoreModuleItem instanceof EffectModuleItem efi && efi.isReverse()) {
                    count = Math.min(count, 2);
                }
                tmap.put(basecoreModuleItem, tmap.getOrDefault(basecoreModuleItem, 0) + count);
            }
        }
        modules.clear();
        modules.putAll(tmap);
        modules.forEach((basecoreModuleItem, integer) -> {
            entityData.addMaxHealth(basecoreModuleItem.getHealth(integer));
            entityData.addRange(basecoreModuleItem.getRange(integer));
        });
    }

    public int getRange() {
        return entityData.getRange();
    }

    /** 基地逻辑中心（用于范围计算）；普通基地核心即方块自身位置，多方块控制器可覆写为结构中心。 */
    public net.minecraft.core.BlockPos getBaseCenter() {
        return getBlockPos();
    }

    public int getMaxHealth() {
        return entityData.getMaxHealth();
    }

    // ----- 能源（Rs） -----

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return dev.anye.mc.basecore.config.BasecoreConfig.getEnergyCapacity();
    }

    public void setEnergy(int value) {
        energy = net.minecraft.util.Mth.clamp(value, 0, getMaxEnergy());
        energyPowered = energy > 0;
        super.setChanged();
    }

    public void addEnergy(int value) {
        setEnergy(energy + value);
    }

    /** 当前能源是否足够运行；为 false 时模块效果与怪物攻核停止。能源系统关闭时恒为 true。 */
    public boolean isEnergyPowered() {
        return !dev.anye.mc.basecore.config.BasecoreConfig.isEnergyEnabled() || energyPowered;
    }

    /** 每消耗 1 Rs 所需的 tick 数；苏式基地计算机覆写为 1 秒。 */
    protected int getEnergyTicksPerUnit() {
        return dev.anye.mc.basecore.config.BasecoreConfig.getBasecoreEnergyTicksPerRs();
    }

    /**
     * 每 {@code balance.energy_refuel_seconds}（默认 10 秒）尝试一次：
     * 优先使用 GUI 中的专用能源槽，其次扫描模块容器；若剩余可填充量小于该物品提供的 Rs，则不消耗（避免浪费）。
     */
    private void tryRefuelEnergy() {
        int room = getMaxEnergy() - energy;
        if (room <= 0) return;
        if (consumeEnergyItem(energySlot, 0, room)) return;
        for (int i = 0; i < items.getSlots(); i++) {
            if (consumeEnergyItem(items, i, room)) return;
        }
    }

    private boolean consumeEnergyItem(net.neoforged.neoforge.items.ItemStackHandler handler, int slot, int room) {
        int rs = dev.anye.mc.basecore.energy.EnergySources.getRs(handler.getStackInSlot(slot));
        if (rs <= 0 || rs > room) return false;
        handler.extractItem(slot, 1, false);
        energy += rs;
        energyPowered = true;
        super.setChanged();
        return true;
    }

    /** 零件升级界面绑定的独立能源输入槽（容量 1 格，只接受能源物品）。 */
    public net.neoforged.neoforge.items.ItemStackHandler getEnergySlotHandler() {
        return energySlot;
    }

    /**
     * 供漏斗使用的物品处理器：只接受能源物品（默认红石 / 红石块），且不允许抽出。
     * 通过 {@code Capabilities.ItemHandler.BLOCK} 暴露给其他方块。
     * <p>
     * 直接对准 GUI 中的专用能源槽，而不是模块容器——否则红石会被塞进模块槽位，
     * 组件玩法下玩家看不到这些物品，会误以为漏斗没能输入。
     */
    public net.neoforged.neoforge.items.IItemHandler getEnergyInputHandler() {
        if (energyInputHandler == null) {
            energyInputHandler = new net.neoforged.neoforge.items.IItemHandler() {
                @Override
                public int getSlots() {
                    return energySlot.getSlots();
                }

                @Override
                public ItemStack getStackInSlot(int slot) {
                    return energySlot.getStackInSlot(slot);
                }

                @Override
                public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                    if (!dev.anye.mc.basecore.energy.EnergySources.isEnergySource(stack)) return stack;
                    return energySlot.insertItem(0, stack, simulate);
                }

                @Override
                public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    return ItemStack.EMPTY;
                }

                @Override
                public int getSlotLimit(int slot) {
                    return energySlot.getSlotLimit(0);
                }

                @Override
                public boolean isItemValid(int slot, ItemStack stack) {
                    return dev.anye.mc.basecore.energy.EnergySources.isEnergySource(stack);
                }
            };
        }
        return energyInputHandler;
    }

    public void damage(int health) {
        entityData.addHealth(-health);
        super.setChanged();
        updateToClient();
    }

    public void setHealth(int health) {
        entityData.setHealth(health);
        super.setChanged();
        updateToClient();
    }

    public int getHealth() {
        return entityData.getHealth();
    }

    public void addHealth(int health) {
        entityData.addHealth(health);
        super.setChanged();
        updateToClient();
    }

    public int getModuleLvl(BasecoreModuleItem module) {
        return modules.getOrDefault(module, 0);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.basecore.basecore");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new BaseCoreMenu(i, inventory, this, this.data);
    }

    private int syncTick = 0;
    /** 每 20 tick 重新判定一次旋转动力，避免频繁反射读取 Create 网络。 */
    private int rotationCheckTick = 0;
    /** 当前是否获得足够的旋转动力；未启用联动或带免疫标记时为 true。 */
    private boolean rotationPowered = true;
    // 裂纹破坏循环状态（怪物攻击核心可选玩法），按怪物实体 ID 分别追踪，支持多怪物同时攻核
    private final Map<Integer, Float> crackProgressMap = new HashMap<>();
    private final Map<Integer, Integer> crackStageMap = new HashMap<>();
    // 性能优化：实体扫描每 20 tick 才做一次并缓存结果，其余 tick 只做存活/距离的廉价校验
    private final List<net.minecraft.world.entity.monster.Monster> cachedMonsters = new java.util.ArrayList<>();
    private final Map<Integer, net.minecraft.world.entity.monster.Monster> attackerMap = new java.util.LinkedHashMap<>();
    private int monsterScanTick = 0;
    private int crackSoundCooldown = 0;

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        entityData.tick();

        if (!level.isClientSide()) {
            // 冷汗联动：低于停机阈值时基地核心彻底停止工作（仍保留客户端同步，便于显示状态）
            if (dev.anye.mc.basecore.integration.coldsweat.ColdSweatCompat.isShutdown(this)) {
                syncTick++;
                if (syncTick >= 20) {
                    syncTick = 0;
                    updateToTrackingOnly();
                }
                return;
            }
            // 能源：每 X tick 消耗 1 Rs，耗尽后基地停止工作
            // 可在配置 balance.energy_enabled 中整体关闭，关闭后视为永久供能
            if (dev.anye.mc.basecore.config.BasecoreConfig.isEnergyEnabled()) {
                if (--energyTick <= 0) {
                    energyTick = getEnergyTicksPerUnit();
                    if (energy > 0) {
                        energy--;
                        super.setChanged();
                    }
                }
                energyPowered = energy > 0;
                if (++refuelTick >= dev.anye.mc.basecore.config.BasecoreConfig.getEnergyRefuelTicks()) {
                    refuelTick = 0;
                    tryRefuelEnergy();
                }
            } else {
                energyPowered = true;
            }
            // 机械动力联动：需要足够转速与应力才能工作
            if (--rotationCheckTick <= 0) {
                rotationCheckTick = 20;
                rotationPowered = dev.anye.mc.basecore.integration.create.RotationHelper.evaluate(
                        level, blockPos,
                        dev.anye.mc.basecore.config.BasecoreConfig.getCreateBasecoreMinRpm(),
                        dev.anye.mc.basecore.config.BasecoreConfig.getCreateBasecoreStress(),
                        entityData.isRotationImmune()).powered();
            }
        }

        if (!rotationPowered || !energyPowered) {
            // 未获得足够应力或能源：模块效果与怪物攻击均停止，仅维持基础同步
            syncTick++;
            if (syncTick >= 20) {
                syncTick = 0;
                updateToTrackingOnly();
            }
            return;
        }

        // 可选玩法：怪物攻击基地核心（配置 balance.monster_attack_core，默认关闭）
        if (!level.isClientSide() && dev.anye.mc.basecore.config.BasecoreConfig.isMonsterAttackCore()) {
            tickMonsterAttack(level, blockPos);
        }

        Map<BasecoreModuleItem, Integer> tmap = new HashMap<>(modules);
        tmap.forEach((basecoreModuleItem, integer) -> {
            if (basecoreModuleItem.isHasTick())
                basecoreModuleItem.runTick(this, level, blockPos, blockState, integer);
        });

        syncTick++;
        if (syncTick >= 20) {
            syncTick = 0;
            updateToTrackingOnly();
        }
    }

    /** 当前是否已获得足够的旋转动力（供 GUI 或联动模组查询）。 */
    public boolean isRotationPowered() {
        return rotationPowered;
    }

    /**
     * 怪物攻击核心：仿照 mutanter 摧毁者的破坏流程，
     * 在核心方块上施加裂纹（destroyBlockProgress 0-9 级）→ 裂纹满时对核心扣血 → 清空裂纹，
     * 循环直到核心血量归零（由 ForgeEvent 的核心损坏逻辑处理）。
     * 支持最多 balance.monster_attack_max_count（默认 10）只怪物同时攻核，
     * 每只怪物独立追踪裂纹进度，各自完成一轮即独立扣血。
     * 性能：实体扫描与寻路指令每 20 tick 一次（缓存怪物列表），
     * 其余 tick 只做存活/距离校验与裂纹进度更新，避免每 tick 的 AABB 实体查询。
     * 仅在 balance.monster_attack_core = true 时由 tick() 调用。
     */
    private void tickMonsterAttack(Level level, BlockPos blockPos) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;

        net.minecraft.world.phys.Vec3 center = net.minecraft.world.phys.Vec3.atCenterOf(blockPos);

        // 每 20 tick 重新扫描范围内的怪物并刷新寻路；其余 tick 使用缓存
        if (++monsterScanTick >= 20) {
            monsterScanTick = 0;
            scanMonsters(serverLevel, blockPos, center);
        }

        if (attackerMap.isEmpty()) return;

        float cycleTicks = dev.anye.mc.basecore.config.BasecoreConfig.getMonsterCrackTicks();
        int dmg = Math.max(1, Math.round(dev.anye.mc.basecore.config.BasecoreConfig.getMonsterCoreDamage()));
        boolean anyProgress = false;
        boolean anyBreak = false;

        for (java.util.Iterator<Map.Entry<Integer, net.minecraft.world.entity.monster.Monster>> it = attackerMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, net.minecraft.world.entity.monster.Monster> entry = it.next();
            int id = entry.getKey();
            net.minecraft.world.entity.monster.Monster attacker = entry.getValue();
            // 廉价校验：死亡或离开 3 格 → 立即清裂并移出
            if (!attacker.isAlive() || attacker.distanceToSqr(center) > 3.0D * 3.0D) {
                serverLevel.destroyBlockProgress(id, blockPos, -1);
                crackProgressMap.remove(id);
                crackStageMap.remove(id);
                it.remove();
                continue;
            }
            anyProgress = true;

            // 每只怪物独立积累裂纹并独立扣血
            float progress = crackProgressMap.merge(id, 1.0f / cycleTicks, Float::sum);
            int stage = Math.min((int) (progress * 10), 9);
            Integer oldStage = crackStageMap.get(id);
            if (oldStage == null || stage != oldStage) {
                crackStageMap.put(id, stage);
                serverLevel.destroyBlockProgress(id, blockPos, stage);
            }
            if (progress >= 1.0f) {
                // 多怪物各自独立扣血，绕过单次伤害的无敌帧
                entityData.addHealth(-dmg);
                serverLevel.destroyBlockProgress(id, blockPos, -1);
                crackProgressMap.remove(id);
                crackStageMap.remove(id);
                it.remove();
                anyBreak = true;
            }
        }

        // 挖掘音效 + 挥手动画（参考摧毁者的表现，全体攻击者同步动作；音效每 8 tick 至多一次）
        if (anyProgress && --crackSoundCooldown <= 0) {
            net.minecraft.world.level.block.SoundType soundType = getBlockState().getSoundType();
            serverLevel.playSound(null, blockPos, soundType.getHitSound(), net.minecraft.sounds.SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 4.0F, soundType.getPitch() * 0.5F);
            crackSoundCooldown = 8;
            for (net.minecraft.world.entity.monster.Monster attacker : attackerMap.values()) {
                attacker.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
            }
        }
        // 破坏音效每 tick 至多一次，避免多怪物同 tick 完成时刷屏
        if (anyBreak) {
            serverLevel.playSound(null, blockPos, getBlockState().getSoundType().getBreakSound(), net.minecraft.sounds.SoundSource.BLOCKS,
                    1.0F, 1.0F);
        }
    }

    /** 每 20 tick 一次的实体扫描：过滤名单、刷新寻路、选出贴身攻击者（按距离排序、限制数量） */
    private void scanMonsters(net.minecraft.server.level.ServerLevel serverLevel, BlockPos blockPos, net.minecraft.world.phys.Vec3 center) {
        // 扫描半径 = 怪物感知范围（balance.monster_sense_range，默认 32 格），而非核心自身范围
        int range = dev.anye.mc.basecore.config.BasecoreConfig.getMonsterSenseRange();
        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(blockPos).inflate(range);
        List<net.minecraft.world.entity.monster.Monster> monsters = serverLevel.getEntitiesOfClass(
                net.minecraft.world.entity.monster.Monster.class, box);
        // 按配置名单过滤（all / whitelist / blacklist）
        monsters.removeIf(m -> !m.isAlive() || !dev.anye.mc.basecore.config.BasecoreConfig.shouldMonsterAttackCore(m));
        cachedMonsters.clear();
        cachedMonsters.addAll(monsters);

        // 面向核心并寻路（每 20 tick 一次，避免每 tick 重算路径的开销）
        for (net.minecraft.world.entity.monster.Monster monster : cachedMonsters) {
            monster.getLookControl().setLookAt(center.x, center.y, center.z);
            monster.getNavigation().moveTo(center.x, center.y, center.z, 1.0D);
        }

        // 选取 3 格内的怪物，按距离排序，限制最大同时攻击数量
        List<net.minecraft.world.entity.monster.Monster> attackers = new java.util.ArrayList<>();
        for (net.minecraft.world.entity.monster.Monster monster : monsters) {
            if (monster.distanceToSqr(center) <= 3.0D * 3.0D) attackers.add(monster);
        }
        attackers.sort((a, b) -> Double.compare(a.distanceToSqr(center), b.distanceToSqr(center)));
        int maxAttackers = dev.anye.mc.basecore.config.BasecoreConfig.getMonsterAttackMaxCount();
        if (attackers.size() > maxAttackers) attackers.subList(maxAttackers, attackers.size()).clear();

        // 更新攻击者集合，清掉不再攻核者的裂纹
        java.util.Set<Integer> activeIds = new java.util.HashSet<>();
        for (net.minecraft.world.entity.monster.Monster attacker : attackers) {
            activeIds.add(attacker.getId());
            attackerMap.put(attacker.getId(), attacker);
        }
        attackerMap.keySet().removeIf(id -> {
            if (activeIds.contains(id)) return false;
            Integer stage = crackStageMap.remove(id);
            if (stage != null) serverLevel.destroyBlockProgress(id, blockPos, -1);
            crackProgressMap.remove(id);
            return true;
        });
    }

    public void setName(String name) {
        entityData.setName(name);
    }

    public String getName() {
        return entityData.getName();
    }

    public void setInvincibleTime(int invincibleTime) {
        entityData.setInvincibleTime(invincibleTime);
    }

    public int getInvincibleTime() {
        return entityData.getInvincibleTime();
    }

    public void setInterferenceTime(int interferenceTime) {
        entityData.setInterferenceTime(interferenceTime);
    }

    public int getInterferenceTime() {
        return entityData.getInterferenceTime();
    }

    public void setDurabilityDamage(int durabilityDamage) {
        entityData.setDurabilityDamage(durabilityDamage);
    }

    public int getDurabilityDamage() {
        return entityData.getDurabilityDamage();
    }

    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        loading = true;
        super.loadAdditional(pTag, pRegistries);
        if (pTag.hasUUID("owner")) owner = pTag.getUUID("owner");
        entityData.handle(pTag);
        energy = pTag.getInt("energy");
        energyPowered = energy > 0;
        if (pTag.contains("energy_slot", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            energySlot.deserializeNBT(pRegistries, pTag.getCompound("energy_slot"));
        }
        loading = false;
        itemChange();
        updateToClient();
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        if (owner != null) pTag.putUUID("owner", owner);
        pTag.putInt("energy", energy);
        pTag.put("energy_slot", energySlot.serializeNBT(pRegistries));
        entityData.saveToNbt(pTag);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag compoundTag = new CompoundTag();
        if (owner != null) compoundTag.putUUID("owner", owner);
        entityData.saveToNbt(compoundTag);
        // Include inventory data for client container rendering
        compoundTag.put("Inventory", getItems().serializeNBT(pRegistries));
        return compoundTag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider pRegistries) {
        if (tag.hasUUID("owner")) owner = tag.getUUID("owner");
        entityData.handle(tag);
        // Restore inventory from update tag if present
        if (tag.contains("Inventory", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            getItems().deserializeNBT(pRegistries, tag.getCompound("Inventory"));
        }
    }

    public void updateToClient() {
        if (level instanceof ServerLevel serverLevel) {
            CompoundTag data = entityData.saveToNbt();
            data.putInt("block.x", this.getBlockPos().getX());
            data.putInt("block.y", this.getBlockPos().getY());
            data.putInt("block.z", this.getBlockPos().getZ());
            data.putString(Net.EASY_NET_KEY, NetReg.BASECORE_BLOCK_KEY);
            serverLevel.players().forEach(serverPlayer -> {
                Net.sendToPlayer(new EasyNetPayload(data), serverPlayer);
            });
        }
    }

    public void updateToTrackingOnly() {
        if (level instanceof ServerLevel serverLevel) {
            CompoundTag data = entityData.saveToNbt();
            data.putInt("block.x", this.getBlockPos().getX());
            data.putInt("block.y", this.getBlockPos().getY());
            data.putInt("block.z", this.getBlockPos().getZ());
            data.putString(Net.EASY_NET_KEY, NetReg.BASECORE_BLOCK_KEY);
            serverLevel.players().forEach(serverPlayer -> {
                Net.sendToPlayer(new EasyNetPayload(data), serverPlayer);
            });
        }
    }

    public void handlePacket(CompoundTag dat) {
        entityData.handle(dat);
    }

    protected Map<BasecoreModuleItem, Long> getModuleCooldowns() {
        return moduleCooldowns;
    }
}