package dev.anye.mc.basecore.api;

import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.integration.coldsweat.ColdSweatCompat;
import dev.anye.mc.basecore.integration.create.RotationHelper;

/**
 * 环境联动公共 API。
 * <p>
 * 供依赖 basecore 的模组在特定玩法中豁免基地核心的低温 / 应力限制，
 * 避免对方模组因为环境联动而无法正常工作。
 */
public final class BasecoreEnvironmentApi {
    private BasecoreEnvironmentApi() {
    }

    /** 设置该核心是否免疫冷汗低温影响（低于阈值仍可打开 GUI 并继续工作）。 */
    public static void setColdImmune(BaseCoreBlockEntity baseCore, boolean immune) {
        if (baseCore == null) return;
        baseCore.getEntityData().setColdImmune(immune);
        baseCore.setChanged();
    }

    public static boolean isColdImmune(BaseCoreBlockEntity baseCore) {
        return baseCore != null && baseCore.getEntityData().isColdImmune();
    }

    /** 设置该核心是否免疫应力需求（无需旋转动力即可工作）。 */
    public static void setRotationImmune(BaseCoreBlockEntity baseCore, boolean immune) {
        if (baseCore == null) return;
        baseCore.getEntityData().setRotationImmune(immune);
        baseCore.setChanged();
    }

    public static boolean isRotationImmune(BaseCoreBlockEntity baseCore) {
        return baseCore != null && baseCore.getEntityData().isRotationImmune();
    }

    /** 当前环境温度是否已使该核心液晶屏失效。 */
    public static boolean isGuiDisabledByTemperature(BaseCoreBlockEntity baseCore) {
        return ColdSweatCompat.isGuiDisabled(baseCore);
    }

    /** 当前环境温度是否已使该核心彻底停机。 */
    public static boolean isShutdownByTemperature(BaseCoreBlockEntity baseCore) {
        return ColdSweatCompat.isShutdown(baseCore);
    }

    /** 该核心当前是否已获得足够的旋转动力（未启用联动或带豁免时恒为 true）。 */
    public static boolean isRotationPowered(BaseCoreBlockEntity baseCore) {
        if (baseCore == null || baseCore.getLevel() == null) return true;
        return RotationHelper.evaluate(baseCore.getLevel(), baseCore.getBlockPos(),
                dev.anye.mc.basecore.config.BasecoreConfig.getCreateBasecoreMinRpm(),
                dev.anye.mc.basecore.config.BasecoreConfig.getCreateBasecoreStress(),
                baseCore.getEntityData().isRotationImmune()).powered();
    }
}
