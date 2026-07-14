package dev.anye.mc.basecore.item.module.basecore;

import com.mojang.logging.LogUtils;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.item.module.BasecoreModuleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class EffectModuleItem extends BasecoreModuleItem {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final MobEffect mobEffect;
    private final int time;
    private final boolean reverse;
    private int tickTime = 0;
    public EffectModuleItem(MobEffect mobEffect, int time) {
        this(mobEffect,time,false);
    }
    public EffectModuleItem(MobEffect mobEffect, int time,boolean reverse) {
        super(true);
        this.mobEffect = mobEffect;
        this.time = time;
        this.reverse = reverse;
    }
    @Override
    public void tick(BaseCoreBlockEntity baseCoreBlockEntity, Level level, BlockPos blockPos, BlockState blockState, int count) {
        if (tickTime <= 0) {
            tickTime = 4;
            MobEffectInstance mobEffectInstance  = new MobEffectInstance(mobEffect,time,Math.min(count,3));
            level.getEntities(null,new AABB(blockPos).inflate(baseCoreBlockEntity.getRange())).forEach(entity -> {
                if (entity instanceof ServerPlayer serverPlayer) {
                    if (reverse){
                        if (!baseCoreBlockEntity.canUse(serverPlayer.getUUID())){
                            serverPlayer.addEffect(mobEffectInstance);
                        }
                    }else if (baseCoreBlockEntity.canUse(serverPlayer.getUUID()))
                        serverPlayer.addEffect(mobEffectInstance);
                }
            });
        }else tickTime--;
    }
}
