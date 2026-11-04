package dev.anye.mc.basecore.integration.create;

import dev.anye.mc.basecore.block.entity.StressSupplyBlockEntity;
import dev.anye.mc.basecore.config.BasecoreConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 应力需求判定：基地核心 / 防御炮从四个水平面接收旋转动力。
 * <p>
 * 可用的输入来源：
 * <ul>
 *     <li>本模组的「不间断应力供应器」，每个提供 {@code supply_rpm} 转速与 {@code supply_stress} 应力；</li>
 *     <li>安装了 Create 时，相邻的真实 Create 动力方块（读取其转速与所在网络剩余应力）。</li>
 * </ul>
 * 多个供应器的转速会累加（两个 32 RPM 的供应器即可满足 64 RPM 门槛），
 * 真实 Create 动力方块取最大转速，应力取网络余量之和。
 * 未启用机械动力联动或核心带豁免标记时，直接视为已供电。
 */
public final class RotationHelper {
    /** 判定结果。 */
    public record Status(boolean powered, float rpm, float stress) {
        public static final Status UNPOWERED = new Status(false, 0.0f, 0.0f);
    }

    private static final Direction[] HORIZONTAL = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    private RotationHelper() {
    }

    public static Status evaluate(Level level, BlockPos pos, int minRpm, int requiredStress, boolean immune) {
        // 未启用联动、带免疫标记、或未安装 Create 时：不施加应力门槛，保持原有玩法
        if (!BasecoreConfig.isCreateIntegrationEnabled() || immune || !CreateCompat.isLoaded()) {
            return new Status(true, minRpm, requiredStress);
        }

        float createRpm = 0.0f;
        float stress = 0.0f;
        int supplyCount = 0;

        for (Direction direction : HORIZONTAL) {
            BlockPos neighborPos = pos.relative(direction);
            BlockEntity blockEntity = level.getBlockEntity(neighborPos);
            if (blockEntity instanceof StressSupplyBlockEntity supply && supply.isActive()) {
                supplyCount++;
                stress += supply.getStressOutput();
            } else if (CreateCompat.isLoaded()) {
                CreateCompat.Sample sample = CreateCompat.sample(level, neighborPos);
                if (sample != null) {
                    createRpm = Math.max(createRpm, sample.rpm());
                    stress += sample.spareStress();
                }
            }
        }

        float rpm = Math.max(createRpm, supplyCount * (float) BasecoreConfig.getSupplyRpm());
        boolean powered = rpm >= minRpm && stress >= requiredStress;
        return new Status(powered, rpm, stress);
    }
}
