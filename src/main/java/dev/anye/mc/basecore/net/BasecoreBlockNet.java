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
                java.util.Optional<dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity> core =
                        clientLevel.getChunkAt(blockPos).getBlockEntity(blockPos, BlockEntityRegister.BASECORE.get());
                if (core.isPresent()) {
                    core.get().handlePacket(dat);
                } else {
                    // 苏式基地计算机继承基地核心，同样需要接收同步数据（血量 / 成员等）
                    clientLevel.getChunkAt(blockPos).getBlockEntity(blockPos, BlockEntityRegister.SOVIET_COMPUTER.get())
                            .ifPresent(blockEntity -> blockEntity.handlePacket(dat));
                }
            }
        }
    }

    @Override
    public void server(CompoundTag dat) {
        // handled via ServerLevel on the server side
    }
}