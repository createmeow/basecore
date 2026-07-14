package dev.anye.mc.basecore.basecore;

import com.mojang.logging.LogUtils;
import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.block.entity.basecore.BasecoreBlockEntityData;
import dev.anye.mc.basecore.net.Net;
import dev.anye.mc.basecore.net.NetReg;
import dev.anye.mc.basecore.net.easy_net.EasyNetSTC;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


//@OnlyIn(Dist.DEDICATED_SERVER)
public class BasecoreServerHelper {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<BaseCoreBlockEntity> BASE_CORE_BLOCK_ENTITIES = new ArrayList<>();
    private static final List<BlockPos> BaseCores = new ArrayList<>();
    public static void debug(){
        BaseCores.forEach(blockPos -> LOGGER.debug("pos:{}",blockPos));
    }
    public static boolean hasName(String name){
        boolean[] r ={false};
        BASE_CORE_BLOCK_ENTITIES.forEach(baseCoreBlockEntity -> {
            if (baseCoreBlockEntity.getEntityData().getName().equals(name)){
                r[0] = true;
            }
        });
        return r[0];
    }
    public static List<BaseCoreBlockEntity> getBaseCoreBlockEntities() {
        return BASE_CORE_BLOCK_ENTITIES;
    }

    public static void clear(){
        BASE_CORE_BLOCK_ENTITIES.clear();
        BaseCores.clear();
    }
    public static void addEntity(BaseCoreBlockEntity entity){
        BASE_CORE_BLOCK_ENTITIES.add(entity);
        add(entity.getBlockPos());
    }
    public static void addEntityAndSend(BaseCoreBlockEntity entity){
        BASE_CORE_BLOCK_ENTITIES.add(entity);
        addAndSend(entity.getBlockPos());
    }
    public static void addAndSend(BlockPos blockPos){
        add(blockPos);
        upToClient();
    }
    public static void add(BlockPos blockPos){
        if (!BaseCores.contains(blockPos)) {
            BaseCores.add(blockPos);
            //upToClient();
        }
    }
    public static void delEntityAndSend(BaseCoreBlockEntity entity){
        delEntity(entity);
        upToClient();
    }
    public static void delEntity(BaseCoreBlockEntity entity){
        del(entity.getBlockPos());
        BASE_CORE_BLOCK_ENTITIES.remove(entity);
    }

    public static void delAndSend(BlockPos blockPos){
        del(blockPos);
        upToClient();
    }
    public static void del(BlockPos blockPos){
        BaseCores.remove(blockPos);
    }

    public static boolean check(BaseCoreBlockEntity blockEntity,int r){
        if (blockEntity.getLevel() instanceof ServerLevel serverLevel) {
            for (BlockPos bp : BaseCores) {
                if (!bp.equals(blockEntity.getBlockPos())) {
                    BlockEntity be = serverLevel.getBlockEntity(bp);
                    if (be instanceof BaseCoreBlockEntity baseCoreBlockEntity) {
                        AABB q1 = new AABB(baseCoreBlockEntity.getBlockPos()).inflate(baseCoreBlockEntity.getRange());
                        if (q1.intersects(new AABB(blockEntity.getBlockPos()).inflate(r))) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean check(Level level, BlockPos blockPos, ItemStack itemStack){

        for (BlockPos bp : BaseCores){
            if (bp.equals(blockPos)) return true;
            BlockEntity be = level.getBlockEntity(bp);
            if (be instanceof BaseCoreBlockEntity baseCoreBlockEntity) {
                AABB q1 = new AABB(baseCoreBlockEntity.getBlockPos()).inflate(baseCoreBlockEntity.getRange() + 1);
                CompoundTag compoundTag = itemStack.getTagElement("BlockEntityTag");
                int range = 1;
                if (compoundTag != null){
                    if (compoundTag.contains("range")) range += compoundTag.getInt("range");
                    else range += BasecoreBlockEntityData.DefaultRange;
                } else range += BasecoreBlockEntityData.DefaultRange;

                if (q1.intersects(new AABB(blockPos).inflate(range))) {
                    return false;
                }
                /*
                if (bp.distToCenterSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ()) <= (range * range)) {
                    return false;
                }

                 */
            }
        }
        return true;
    }

    public static Rectangle createSquareFromCenterAndRadius(BlockPos centerPoint, int radius) {
        int topLeftX = centerPoint.getX() - radius;
        int topLeftY = centerPoint.getZ() - radius;
        int sideLength = 2 * radius;
        return new Rectangle(topLeftX, topLeftY, sideLength, sideLength);
    }





    public static @Nullable BaseCoreBlockEntity getBasecore(ServerPlayer player){
        return getBasecore(player.level(),player.position());
    }

    public static @Nullable BaseCoreBlockEntity getBasecore(Level level,Vec3 player){
        for (BlockPos bp : BaseCores){
            BaseCoreBlockEntity baseCoreBlockEntity = level.getBlockEntity(bp,BlockEntityRegister.BASECORE.get()).orElse(null);
            if (baseCoreBlockEntity != null && isInRange(player,baseCoreBlockEntity)){
                return baseCoreBlockEntity;
            }
        }
        return null;
    }

    public static boolean isInRange(Player player,BaseCoreBlockEntity baseCoreBlockEntity){
        return isInRange(player.position(),baseCoreBlockEntity);
    }
    public static boolean isInRange(Vec3 player, BaseCoreBlockEntity baseCoreBlockEntity){
        return new AABB(baseCoreBlockEntity.getBlockPos()).inflate(baseCoreBlockEntity.getRange()).contains(player);//  baseCoreBlockEntity.getBlockPos().distToCenterSqr(player) <= (baseCoreBlockEntity.getRange() * baseCoreBlockEntity.getRange());
    }

    public static boolean hasPermission(Level level, Vec3 player, UUID user){
        BaseCoreBlockEntity baseCoreBlockEntity = getBasecore(level,player);
        return baseCoreBlockEntity == null || baseCoreBlockEntity.canUse(user);
    }

    public static void upToClient(){
        CompoundTag poss = new CompoundTag();
        int i = 0;
        for (BlockPos blockPos : BaseCores){
            CompoundTag pos = new CompoundTag();
            pos.putInt("x",blockPos.getX());
            pos.putInt("y",blockPos.getY());
            pos.putInt("z",blockPos.getZ());
            poss.put(String.valueOf(i),pos);
            i++;
        }
        CompoundTag data = new CompoundTag();
        data.put("block_pos",poss);
        data.putString(Net.EASY_NET_KEY, NetReg.ActivityBasecoreNet.getId().toString());
        Net.EasyNetSTC(PacketDistributor.ALL.noArg(),new EasyNetSTC(data));
    }

    public static void upToPlayer(ServerPlayer serverPlayer){
        CompoundTag poss = new CompoundTag();
        int i = 0;
        for (BlockPos blockPos : BaseCores){
            CompoundTag pos = new CompoundTag();
            pos.putInt("x",blockPos.getX());
            pos.putInt("y",blockPos.getY());
            pos.putInt("z",blockPos.getZ());
            poss.put(String.valueOf(i),pos);
            i++;
        }
        CompoundTag data = new CompoundTag();
        data.put("block_pos",poss);
        data.putString(Net.EASY_NET_KEY, NetReg.ActivityBasecoreNet.getId().toString());
        Net.EasyNetSTP(new EasyNetSTC(data),serverPlayer);
    }
    public static List<BlockPos> getBaseCores(){
        return BaseCores;
    }
}
