package dev.anye.mc.basecore.basecore;

import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.block.entity.basecore.BasecoreBlockEntityData;
import dev.anye.mc.basecore.net.Net;
import dev.anye.mc.basecore.net.NetReg;
import dev.anye.mc.basecore.net.easy_net.EasyNetPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BasecoreServerHelper {
    private static final List<BaseCoreBlockEntity> BASECORE_BLOCK_ENTITIES = new ArrayList<>();

    public static void addEntity(BaseCoreBlockEntity baseCoreBlockEntity) {
        BASECORE_BLOCK_ENTITIES.add(baseCoreBlockEntity);
    }

    public static void delEntity(BaseCoreBlockEntity baseCoreBlockEntity) {
        BASECORE_BLOCK_ENTITIES.remove(baseCoreBlockEntity);
    }

    public static void addEntityAndSend(BaseCoreBlockEntity baseCoreBlockEntity) {
        if (baseCoreBlockEntity != null && !BASECORE_BLOCK_ENTITIES.contains(baseCoreBlockEntity)) {
            BASECORE_BLOCK_ENTITIES.add(baseCoreBlockEntity);
            syncToAll();
        }
    }

    public static void delEntityAndSend(BaseCoreBlockEntity baseCoreBlockEntity) {
        if (baseCoreBlockEntity != null) {
            BASECORE_BLOCK_ENTITIES.remove(baseCoreBlockEntity);
            syncToAll();
        }
    }

    private static void syncToAll() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString(Net.EASY_NET_KEY, NetReg.ACTIVITY_BASECORE_KEY);
        CompoundTag posTag = new CompoundTag();
        for (BaseCoreBlockEntity be : BASECORE_BLOCK_ENTITIES) {
            BlockPos bp = be.getBlockPos();
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", bp.getX());
            tag.putInt("y", bp.getY());
            tag.putInt("z", bp.getZ());
            posTag.put(bp.toShortString(), tag);
        }
        compoundTag.put("block_pos", posTag);
        Net.sendToAllPlayers(new EasyNetPayload(compoundTag));
        for (BaseCoreBlockEntity be : BASECORE_BLOCK_ENTITIES) {
            be.updateToTrackingOnly();
        }
    }

    public static List<BaseCoreBlockEntity> getBaseCoreBlockEntities() {
        return BASECORE_BLOCK_ENTITIES;
    }

    public static boolean check(Level level, BlockPos blockPos, net.minecraft.world.item.ItemStack itemStack) {
        for (BaseCoreBlockEntity baseCoreBlockEntity : BASECORE_BLOCK_ENTITIES) {
            AABB existingBox = getRangeBox(baseCoreBlockEntity.getBlockPos(), baseCoreBlockEntity.getRange());
            if (existingBox.intersects(getRangeBox(blockPos, BasecoreBlockEntityData.DefaultRange))) {
                return false;
            }
        }
        return true;
    }

    public static boolean check(BaseCoreBlockEntity baseCoreBlockEntity, int range) {
        for (BaseCoreBlockEntity baseCoreBlockEntity1 : BASECORE_BLOCK_ENTITIES) {
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
        for (BaseCoreBlockEntity baseCoreBlockEntity : BASECORE_BLOCK_ENTITIES) {
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
        for (BaseCoreBlockEntity baseCoreBlockEntity : BASECORE_BLOCK_ENTITIES) {
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
        for (BaseCoreBlockEntity baseCoreBlockEntity : BASECORE_BLOCK_ENTITIES) {
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
        for (BaseCoreBlockEntity baseCoreBlockEntity : BASECORE_BLOCK_ENTITIES) {
            baseCoreBlockEntity.updateToTrackingOnly();
        }
    }

    public static boolean hasName(String name) {
        for (BaseCoreBlockEntity be : BASECORE_BLOCK_ENTITIES) {
            if (be.getName().equals(name)) return true;
        }
        return false;
    }
}