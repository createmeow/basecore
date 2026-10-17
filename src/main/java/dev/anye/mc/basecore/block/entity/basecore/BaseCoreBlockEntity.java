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

    public BaseCoreBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegister.BASECORE.get(), pPos, pBlockState, inventorySize);
        entityData = new BasecoreBlockEntityData();
        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> BaseCoreBlockEntity.this.entityData.getHealth();
                    case 1 -> BaseCoreBlockEntity.this.entityData.getMaxHealth();
                    case 2 -> BaseCoreBlockEntity.this.entityData.getRange();
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
                return 3;
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

    public int getMaxHealth() {
        return entityData.getMaxHealth();
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
        loading = false;
        if (pTag.hasUUID("owner")) owner = pTag.getUUID("owner");
        entityData.handle(pTag);
        itemChange();
        updateToClient();
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        if (owner != null) pTag.putUUID("owner", owner);
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