package dev.anye.mc.basecore.basecore;

import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.block.entity.basecore.BasecoreBlockEntityData;
import dev.anye.mc.basecore.net.Net;
import dev.anye.mc.basecore.net.NetReg;
import dev.anye.mc.basecore.net.easy_net.EasyNetPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BasecoreServerHelper {
    // 按 "维度:坐标" 去重存储，避免区块重载/反复进存档时累积幽灵核心实例
    private static final Map<String, BaseCoreBlockEntity> BASECORE_BLOCK_ENTITIES = new HashMap<>();
    // 性能优化：区块加载/卸载只做脏标记，由服务器 tick 去重后每秒至多同步一次，
    // 避免进入存档时每个区块事件都触发全核心×全玩家的同步风暴
    private static volatile boolean pendingSync = false;

    public static void markDirty() {
        pendingSync = true;
    }

    /** 由 ServerTickEvent 调用：有脏标记时刷新一次客户端活动核心列表 */
    public static void flushSync() {
        if (pendingSync) {
            pendingSync = false;
            upToClient();
        }
    }

    /** 服务器停止时清空注册表（单人环境重进存档时静态字段不会自动清理，否则会累积旧存档的幽灵核心） */
    public static void clear() {
        BASECORE_BLOCK_ENTITIES.clear();
        pendingSync = false;
    }

    private static String keyOf(BaseCoreBlockEntity be) {
        ResourceLocation dim = be.getLevel() != null ? be.getLevel().dimension().location() : null;
        return (dim != null ? dim.toString() : "null") + ":" + be.getBlockPos().toShortString();
    }

    public static void addEntity(BaseCoreBlockEntity baseCoreBlockEntity) {
        if (baseCoreBlockEntity != null) {
            BASECORE_BLOCK_ENTITIES.put(keyOf(baseCoreBlockEntity), baseCoreBlockEntity);
        }
    }

    public static void delEntity(BaseCoreBlockEntity baseCoreBlockEntity) {
        if (baseCoreBlockEntity != null) {
            BASECORE_BLOCK_ENTITIES.remove(keyOf(baseCoreBlockEntity));
        }
    }

    public static void addEntityAndSend(BaseCoreBlockEntity baseCoreBlockEntity) {
        if (baseCoreBlockEntity != null) {
            String key = keyOf(baseCoreBlockEntity);
            boolean added = !BASECORE_BLOCK_ENTITIES.containsKey(key);
            BASECORE_BLOCK_ENTITIES.put(key, baseCoreBlockEntity);
            if (added) syncToAll();
        }
    }

    public static void delEntityAndSend(BaseCoreBlockEntity baseCoreBlockEntity) {
        if (baseCoreBlockEntity != null) {
            BASECORE_BLOCK_ENTITIES.remove(keyOf(baseCoreBlockEntity));
            syncToAll();
        }
    }

    private static void syncToAll() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString(Net.EASY_NET_KEY, NetReg.ACTIVITY_BASECORE_KEY);
        CompoundTag posTag = new CompoundTag();
        for (BaseCoreBlockEntity be : BASECORE_BLOCK_ENTITIES.values()) {
            BlockPos bp = be.getBlockPos();
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", bp.getX());
            tag.putInt("y", bp.getY());
            tag.putInt("z", bp.getZ());
            posTag.put(bp.toShortString(), tag);
        }
        compoundTag.put("block_pos", posTag);
        Net.sendToAllPlayers(new EasyNetPayload(compoundTag));
        for (BaseCoreBlockEntity be : BASECORE_BLOCK_ENTITIES.values()) {
            be.updateToTrackingOnly();
        }
    }

    public static Collection<BaseCoreBlockEntity> getBaseCoreBlockEntities() {
        return BASECORE_BLOCK_ENTITIES.values();
    }

    public static boolean check(Level level, BlockPos blockPos, net.minecraft.world.item.ItemStack itemStack) {
        for (BaseCoreBlockEntity baseCoreBlockEntity : BASECORE_BLOCK_ENTITIES.values()) {
            // 只与同一维度的核心比较，避免跨维度/旧存档残留实体误判
            if (baseCoreBlockEntity.getLevel() != level) continue;
            AABB existingBox = getRangeBox(baseCoreBlockEntity.getBlockPos(), baseCoreBlockEntity.getRange());
            if (existingBox.intersects(getRangeBox(blockPos, dev.anye.mc.basecore.config.BasecoreConfig.getBasecoreRange()))) {
                return false;
            }
        }
        return true;
    }

    public static boolean check(BaseCoreBlockEntity baseCoreBlockEntity, int range) {
        for (BaseCoreBlockEntity baseCoreBlockEntity1 : BASECORE_BLOCK_ENTITIES.values()) {
            if (baseCoreBlockEntity1 != baseCoreBlockEntity) {
                AABB existingBox = getRangeBox(baseCoreBlockEntity1.getBlockPos(), baseCoreBlockEntity1.getRange());
                AABB newBox = getRangeBox(baseCoreBlockEntity.getBlockPos(), range);
                if (existingBox.intersects(newBox)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static BaseCoreBlockEntity getBasecore(Level level, net.minecraft.world.phys.Vec3 vec3) {
        return getBasecore(level, vec3, 0);
    }

    public static BaseCoreBlockEntity getBasecore(Level level, net.minecraft.world.phys.Vec3 vec3, int extraRange) {
        for (BaseCoreBlockEntity baseCoreBlockEntity : BASECORE_BLOCK_ENTITIES.values()) {
            if (baseCoreBlockEntity.getLevel() != level) continue;
            AABB box = getRangeBox(baseCoreBlockEntity.getBlockPos(), baseCoreBlockEntity.getRange() + extraRange);
            if (box.contains(vec3)) {
                return baseCoreBlockEntity;
            }
        }
        return null;
    }

    public static BaseCoreBlockEntity getBasecore(ServerPlayer serverPlayer) {
        return getBasecore(serverPlayer.level(), serverPlayer.position());
    }

    public static boolean hasPermission(Level level, net.minecraft.world.phys.Vec3 vec3, UUID uuid) {
        for (BaseCoreBlockEntity baseCoreBlockEntity : BASECORE_BLOCK_ENTITIES.values()) {
            if (baseCoreBlockEntity.getLevel() != level) continue;
            AABB box = getRangeBox(baseCoreBlockEntity.getBlockPos(), baseCoreBlockEntity.getRange());
            if (box.contains(vec3)) {
                return baseCoreBlockEntity.canUse(uuid);
            }
        }
        return true;
    }

    private static AABB getRangeBox(BlockPos pos, int range) {
        return new AABB(
            pos.getX() + 0.5 - range,
            pos.getY() + 0.5 - range,
            pos.getZ() + 0.5 - range,
            pos.getX() + 0.5 + range,
            pos.getY() + 0.5 + range,
            pos.getZ() + 0.5 + range
        );
    }

    public static void upToPlayer(ServerPlayer serverPlayer) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString(Net.EASY_NET_KEY, NetReg.ACTIVITY_BASECORE_KEY);
        CompoundTag compoundTag1 = new CompoundTag();
        for (BaseCoreBlockEntity baseCoreBlockEntity : BASECORE_BLOCK_ENTITIES.values()) {
            BlockPos blockPos = baseCoreBlockEntity.getBlockPos();
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.putInt("x", blockPos.getX());
            compoundTag2.putInt("y", blockPos.getY());
            compoundTag2.putInt("z", blockPos.getZ());
            compoundTag1.put(baseCoreBlockEntity.getBlockPos().toShortString(), compoundTag2);
        }
        compoundTag.put("block_pos", compoundTag1);
        Net.sendToPlayer(new EasyNetPayload(compoundTag), serverPlayer);
    }

    public static void upToClient() {
        for (BaseCoreBlockEntity baseCoreBlockEntity : BASECORE_BLOCK_ENTITIES.values()) {
            baseCoreBlockEntity.updateToTrackingOnly();
        }
    }

    public static boolean hasName(String name) {
        for (BaseCoreBlockEntity be : BASECORE_BLOCK_ENTITIES.values()) {
            if (be.getName().equals(name)) return true;
        }
        return false;
    }

    /**
     * 生成一个不与任何现有基地重名的默认基地名称。
     * <p>
     * 基本名称格式为 "{playerName}的基地"；若已存在同名基地，
     * 则依次追加数字后缀（"{playerName}的基地1"、"{playerName}的基地2" ...）。
     * </p>
     *
     * @param playerName 领主玩家名
     * @return 唯一不冲突的基地名称
     */
    public static String getUniqueBaseName(String playerName) {
        String baseName = playerName + "的基地";
        if (!hasName(baseName)) return baseName;
        int i = 1;
        while (hasName(baseName + i)) {
            i++;
        }
        return baseName + i;
    }
}
