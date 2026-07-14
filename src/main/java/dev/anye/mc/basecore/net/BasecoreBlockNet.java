package dev.anye.mc.basecore.net;

import com.mojang.logging.LogUtils;
import dev.anye.mc.basecore.basecore.BasecoreClientHelper;
import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.net.easy_net.EasyNet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;

public class BasecoreBlockNet extends EasyNet {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Override
    public void client(CompoundTag dat) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) return;
        ClientLevel clientLevel = minecraft.level;
        if (clientLevel != null){
            BlockPos blockPos = new BlockPos(dat.getInt("block.x"),dat.getInt("block.y"),dat.getInt("block.z"));
            BasecoreClientHelper.addData(blockPos,dat);
            if (clientLevel.hasChunkAt(blockPos)) {
                clientLevel.getChunkAt(blockPos).getBlockEntity(blockPos, BlockEntityRegister.BASECORE.get()).ifPresent(blockEntity -> {
                    blockEntity.handlePacket(dat);
                });
            }
        }
    }

    @Override
    public void server(CompoundTag dat) {
        // handled via ServerLevel on the server side
    }
}