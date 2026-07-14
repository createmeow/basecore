package dev.anye.mc.basecore.item.module.basecore;

import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.item.module.BasecoreModuleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class EffectModuleItem extends BasecoreModuleItem {
    private final Holder<MobEffect> mobEffect;
    private final int time;
    private final boolean reverse;
    private int tickTime = 0;
    public EffectModuleItem(Holder<MobEffect> mobEffect, int time) {
        this(mobEffect, time, false);
    }
    public EffectModuleItem(Holder<MobEffect> mobEffect, int time, boolean reverse) {
        super(3, true); // max 3 levels, has tick
        this.mobEffect = mobEffect;
        this.time = time;
        this.reverse = reverse;
    }
    public boolean isReverse() {
        return reverse;
    }
    @Override
    public int getMaxLevel() {
        return reverse ? 2 : super.getMaxLevel();
    }
    @Override
    public void tick(BaseCoreBlockEntity baseCoreBlockEntity, Level level, BlockPos blockPos, BlockState blockState, int count) {
        if (tickTime <= 0) {
            tickTime = 20; // reapply every 1 second (20 ticks)
            MobEffectInstance mobEffectInstance = new MobEffectInstance(mobEffect, time, Math.min(count - 1, 2));
            int range = baseCoreBlockEntity.getRange();
            level.getEntities(null, new AABB(blockPos).inflate(range)).forEach(entity -> {
                if (entity instanceof ServerPlayer serverPlayer) {
                    if (reverse){
                        if (!baseCoreBlockEntity.canUse(serverPlayer.getUUID())){
                            serverPlayer.addEffect(mobEffectInstance);
                        }
                    } else if (baseCoreBlockEntity.canUse(serverPlayer.getUUID()))
                        serverPlayer.addEffect(mobEffectInstance);
                }
            });
        } else tickTime--;
    }
}