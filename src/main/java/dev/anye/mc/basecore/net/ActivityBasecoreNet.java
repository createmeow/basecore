package dev.anye.mc.basecore.net;

import com.mojang.logging.LogUtils;
import dev.anye.mc.basecore.basecore.BasecoreClientHelper;
import dev.anye.mc.basecore.net.easy_net.EasyNet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;

public class ActivityBasecoreNet extends EasyNet {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Override
    public void client(CompoundTag dat) {
        CompoundTag data = dat.getCompound("block_pos");
        BasecoreClientHelper.clearPos();
        data.getAllKeys().forEach(s -> {
            CompoundTag compoundTag = data.getCompound(s);
            BasecoreClientHelper.addPos(new BlockPos(compoundTag.getInt("x"), compoundTag.getInt("y"), compoundTag.getInt("z")));
        });
    }

    @Override
    public void server(CompoundTag dat) {
    }
}