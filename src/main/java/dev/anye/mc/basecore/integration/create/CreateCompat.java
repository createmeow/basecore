package dev.anye.mc.basecore.integration.create;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;

/**
 * 机械动力（Create）软联动：通过反射读取相邻动力方块的真实转速与应力余量，
 * 不引入编译期依赖，未安装 Create 时返回空值。
 */
public final class CreateCompat {
    private static final boolean LOADED = ModList.get().isLoaded("create");

    private static Method getSpeedMethod;
    private static Method getNetworkMethod;
    private static Method capacityMethod;
    private static Method stressMethod;
    private static Class<?> kineticBlockEntityClass;
    private static boolean resolved = false;

    private CreateCompat() {
    }

    public static boolean isLoaded() {
        return LOADED;
    }

    /** 相邻动力方块提供的旋转采样：转速（RPM）与网络剩余应力（SU）。 */
    public record Sample(float rpm, float spareStress) {
    }

    public static Sample sample(Level level, BlockPos pos) {
        if (!LOADED) return null;
        try {
            if (!resolve()) return null;
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null || !kineticBlockEntityClass.isInstance(blockEntity)) return null;

            float speed = ((Number) getSpeedMethod.invoke(blockEntity)).floatValue();
            if (Math.abs(speed) < 1.0f) return null;

            float spareStress = 0.0f;
            Object network = getNetworkMethod.invoke(blockEntity);
            if (network != null) {
                try {
                    float capacity = ((Number) capacityMethod.invoke(network)).floatValue();
                    float used = ((Number) stressMethod.invoke(network)).floatValue();
                    spareStress = Math.max(0.0f, capacity - used);
                } catch (Throwable ignored) {
                    // 某些网络状态不可读时按 0 余量处理
                }
            }
            return new Sample(Math.abs(speed), spareStress);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean resolve() {
        if (resolved) return kineticBlockEntityClass != null;
        resolved = true;
        try {
            kineticBlockEntityClass = Class.forName("com.simibubi.create.content.kinetics.base.KineticBlockEntity");
            getSpeedMethod = kineticBlockEntityClass.getMethod("getSpeed");
            getNetworkMethod = kineticBlockEntityClass.getMethod("getOrCreateNetwork");
            Class<?> networkClass = Class.forName("com.simibubi.create.content.kinetics.KineticNetwork");
            capacityMethod = networkClass.getMethod("calculateCapacity");
            stressMethod = networkClass.getMethod("calculateStress");
        } catch (Throwable t) {
            kineticBlockEntityClass = null;
        }
        return kineticBlockEntityClass != null;
    }
}
