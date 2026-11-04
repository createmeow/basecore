package dev.anye.mc.basecore.integration.coldsweat;

import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.config.BasecoreConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;

/**
 * 冷汗（Cold Sweat）软联动：不引入编译期依赖，仅通过反射读取方块位置的环境温度。
 * <p>
 * Cold Sweat 内部温度单位为 MC，换算关系为 {@code 1 MC = 25 ℃}，
 * 因此环境温度（摄氏度）= {@code WorldHelper.getRoughTemperatureAt(...) * 25}。
 * 未安装冷汗时本类所有判定均返回 false，基地核心行为保持不变。
 */
public final class ColdSweatCompat {
    private static final boolean LOADED = ModList.get().isLoaded("cold_sweat");
    private static final double MC_TO_CELSIUS = 25.0d;

    private static Method temperatureMethod;
    private static boolean resolved = false;

    private ColdSweatCompat() {
    }

    public static boolean isLoaded() {
        return LOADED;
    }

    /** 读取指定方块位置的环境温度（摄氏度）。不可用时返回 {@link Double#NaN}。 */
    public static double getTemperatureCelsius(Level level, BlockPos pos) {
        if (!LOADED) return Double.NaN;
        try {
            if (!resolved) {
                Class<?> helper = Class.forName("com.momosoftworks.coldsweat.util.world.WorldHelper");
                temperatureMethod = helper.getMethod("getRoughTemperatureAt", Level.class, BlockPos.class);
                resolved = true;
            }
            if (temperatureMethod == null) return Double.NaN;
            Object result = temperatureMethod.invoke(null, level, pos);
            if (result instanceof Number number) {
                return number.doubleValue() * MC_TO_CELSIUS;
            }
        } catch (Throwable ignored) {
            // 冷汗版本变化或未就绪时静默降级
        }
        return Double.NaN;
    }

    /** 低温是否导致液晶屏无法显示（低于 GUI 禁用阈值）。 */
    public static boolean isGuiDisabled(Level level, BlockPos pos) {
        if (!BasecoreConfig.isColdSweatIntegrationEnabled()) return false;
        double temperature = getTemperatureCelsius(level, pos);
        if (Double.isNaN(temperature)) return false;
        return temperature < BasecoreConfig.getColdSweatGuiDisableTemperature();
    }

    /** 低温是否导致基地核心彻底停机（低于停机阈值）。 */
    public static boolean isShutdown(Level level, BlockPos pos) {
        if (!BasecoreConfig.isColdSweatIntegrationEnabled()) return false;
        double temperature = getTemperatureCelsius(level, pos);
        if (Double.isNaN(temperature)) return false;
        return temperature < BasecoreConfig.getColdSweatShutdownTemperature();
    }

    /** 考虑核心豁免标记后的 GUI 禁用判定。 */
    public static boolean isGuiDisabled(BaseCoreBlockEntity baseCore) {
        if (baseCore == null || baseCore.getEntityData().isColdImmune()) return false;
        if (baseCore.getLevel() == null) return false;
        return isGuiDisabled(baseCore.getLevel(), baseCore.getBlockPos());
    }

    /** 考虑核心豁免标记后的停机判定。 */
    public static boolean isShutdown(BaseCoreBlockEntity baseCore) {
        if (baseCore == null || baseCore.getEntityData().isColdImmune()) return false;
        if (baseCore.getLevel() == null) return false;
        return isShutdown(baseCore.getLevel(), baseCore.getBlockPos());
    }
}
