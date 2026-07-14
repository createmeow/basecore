package dev.anye.mc.basecore.net;

import dev.anye.mc.basecore.basecore.BasecoreClientHelper;
import dev.anye.mc.basecore.net.easy_net.EasyNet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public class DefendNet extends EasyNet {
    @Override
    public void client(CompoundTag dat) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) return;
        ClientLevel clientLevel = minecraft.level;
        if (clientLevel != null){
            BlockPos blockPos = new BlockPos(dat.getInt("block.x"),dat.getInt("block.y"),dat.getInt("block.z"));
            BasecoreClientHelper.addDefendData(blockPos, dat);
        }
    }

    @Override
    public void server(CompoundTag dat) {
    }
}
