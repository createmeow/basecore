package dev.anye.mc.basecore.item.module.basecore;

import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.effect.EffectRegister;
import dev.anye.mc.basecore.item.ItemRegister;
import dev.anye.mc.basecore.item.module.BasecoreModuleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BasecoreCounterReconnaissanceMdoule extends BasecoreModuleItem {
    public BasecoreCounterReconnaissanceMdoule(){
        super(true);
    }

    @Override
    protected void tick(BaseCoreBlockEntity baseCoreBlockEntity, Level level, BlockPos blockPos, BlockState blockState, int count) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getPlayers(serverPlayer -> {
                if (baseCoreBlockEntity.canUse(serverPlayer.getUUID())) return false;
                return (serverPlayer.distanceToSqr(blockPos.getCenter()) < Math.pow(baseCoreBlockEntity.getRange(), 2));
            }).forEach(serverPlayer -> {
                if (serverPlayer.hasEffect(EffectRegister.Disguise.get()) && !serverPlayer.getPersistentData().getBoolean("bcrm.basecore.isCheck")) {

                    MobEffectInstance effectInstance = serverPlayer.getEffect(EffectRegister.Disguise.get());
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
