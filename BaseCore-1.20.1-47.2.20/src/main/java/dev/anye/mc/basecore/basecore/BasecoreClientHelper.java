package dev.anye.mc.basecore.basecore;

import com.mojang.logging.LogUtils;
import dev.anye.mc.basecore.block.entity.basecore.BasecoreBlockEntityData;
import dev.anye.mc.basecore.net.Net;
import dev.anye.mc.basecore.net.NetReg;
import dev.anye.mc.basecore.net.easy_net.EasyNetCTS;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class BasecoreClientHelper {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<BlockPos> BaseCores = new ArrayList<>();
        private static final Map<BlockPos, BasecoreBlockEntityData> Data = new HashMap<>();

    public static void addPos(BlockPos blockPos){
        if (!BaseCores.contains(blockPos)) BaseCores.add(blockPos);
    }
    public static void addData(BlockPos blockPos,CompoundTag data){
        BasecoreBlockEntityData entityData = Data.getOrDefault(blockPos,new BasecoreBlockEntityData());
        entityData.handle(data);
        Data.put(blockPos,entityData);
    }
    public static @Nullable BasecoreBlockEntityData getData(BlockPos blockPos){
        if (Data.containsKey(blockPos)) return Data.get(blockPos);
        if (BaseCores.contains(blockPos)) {
            CompoundTag data = new CompoundTag();
            data.putInt("x",blockPos.getX());
            data.putInt("y",blockPos.getY());
            data.putInt("z",blockPos.getZ());
            data.putString(Net.EASY_NET_KEY, NetReg.BasecoreNet.getId().toString());
            Net.EasyNetCTS(new EasyNetCTS(data));
        }
        return Data.getOrDefault(blockPos,null);
    }

    public static int getRange(BlockPos blockPos){
        BasecoreBlockEntityData entityData = getData(blockPos);
        if (entityData == null) return 0;
        return entityData.getRange();
    }
    public static int getRange(CompoundTag compoundTag){
        return compoundTag.getInt("range");
    }

    public static int getHealth(BlockPos blockPos){
        BasecoreBlockEntityData entityData = getData(blockPos);
        if (entityData == null) return 500;
        return entityData.getHealth();
    }
    public static int getHealth(CompoundTag compoundTag){
        return compoundTag.getInt("health");
    }

    public static int getMaxHealth(BlockPos blockPos){
        BasecoreBlockEntityData entityData = getData(blockPos);
        if (entityData == null) return 500;
        return entityData.getMaxHealth();
    }
    public static int getMaxHealth(CompoundTag compoundTag){
        return compoundTag.getInt("max_health");
    }

    public static boolean isInRange(Vec3 o, BlockPos blockPos,int offset){
        BasecoreBlockEntityData entityData = getData(blockPos);
        if (entityData != null) {
            return new AABB(blockPos).inflate(entityData.getRange() + offset).contains(o);// blockPos.distToCenterSqr(o) <= (Math.pow(entityData.getRange() + offset, 2));
        }
        return false;
    }
    public static @Nullable BlockPos getNear(Vec3 o,int offset){
        for (BlockPos bp : BaseCores){
            if (isInRange(o,bp,offset)) return bp;
        }
        return null;
    }

    public static void clearPos() {
        BaseCores.clear();
    }

    public static void tick() {
        Data.forEach((blockPos, basecoreBlockEntityData) -> basecoreBlockEntityData.tick());
    }
}
