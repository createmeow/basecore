package dev.anye.mc.basecore.item.module.basecore;

import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.effect.EffectRegister;
import dev.anye.mc.basecore.item.module.BasecoreModuleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class BasecoreCounterReconnaissanceMdoule extends BasecoreModuleItem {
    public BasecoreCounterReconnaissanceMdoule() {
        super(true);
    }

    @Override
    protected void tick(BaseCoreBlockEntity baseCoreBlockEntity, Level level, BlockPos blockPos, BlockState blockState, int count) {
        if (level instanceof ServerLevel serverLevel) {
            int range = baseCoreBlockEntity.getRange();
            AABB rangeBox = new AABB(
                blockPos.getX() + 0.5 - range,
                blockPos.getY() + 0.5 - range,
                blockPos.getZ() + 0.5 - range,
                blockPos.getX() + 0.5 + range,
                blockPos.getY() + 0.5 + range,
                blockPos.getZ() + 0.5 + range
            );
            serverLevel.getPlayers(serverPlayer -> {
                if (baseCoreBlockEntity.canUse(serverPlayer.getUUID())) return false;
                return rangeBox.contains(serverPlayer.position());
            }).forEach(serverPlayer -> {
                if (serverPlayer.hasEffect(EffectRegister.Disguise) && !serverPlayer.getPersistentData().getBoolean("bcrm.basecore.isCheck")) {
                    MobEffectInstance effectInstance = serverPlayer.getEffect(EffectRegister.Disguise);
                    if (effectInstance != null) {
                        serverPlayer.getPersistentData().putBoolean("bcrm.basecore.isCheck", true);
                        int effectLvl = count * 20 - (effectInstance.getAmplifier() + 1) * 15;
                        if (serverLevel.getRandom().nextInt(101) < effectLvl) {
                            serverPlayer.getPersistentData().putBoolean("bcrm.basecore.isDiscern", true);
                            serverPlayer.sendSystemMessage(Component.translatable("basecore.basecore.discern"));
                        }
                    }
                }
            });
        }
    }
}