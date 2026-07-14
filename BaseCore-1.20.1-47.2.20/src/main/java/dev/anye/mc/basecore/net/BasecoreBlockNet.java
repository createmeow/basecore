package dev.anye.mc.basecore.net;

import com.mojang.logging.LogUtils;
import dev.anye.mc.basecore.basecore.BasecoreClientHelper;
import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.BlockRegister;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.net.easy_net.EasyNet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;

import java.util.function.Supplier;

public class BasecoreBlockNet extends EasyNet {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Override
    public void client(Supplier<NetworkEvent.Context> contextSupplier, CompoundTag dat) {
        contextSupplier.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    ClientLevel clientLevel = Minecraft.getInstance().level;
                    if (clientLevel != null){
                        BlockPos blockPos = new BlockPos(dat.getInt("block.x"),dat.getInt("block.y"),dat.getInt("block.z"));
                        BasecoreClientHelper.addData(blockPos,dat);
                        if (clientLevel.hasChunkAt(blockPos)) {
                            BaseCoreBlockEntity blockEntity = clientLevel.getChunkAt(blockPos).getBlockEntity(blockPos,BlockEntityRegister.BASECORE.get()).orElse(null);
                            if (blockEntity == null) {
                                blockEntity = new BaseCoreBlockEntity(blockPos, BlockRegister.BASE_CORE.get().defaultBlockState());
                                clientLevel.setBlockEntity(blockEntity);
                            }
                            blockEntity.handlePacket(dat);
                        }
                    }
                })
        );
        super.client(contextSupplier,dat);
    }

    @Override
    public void server(Supplier<NetworkEvent.Context> contextSupplier, CompoundTag dat) {
        contextSupplier.get().enqueueWork(() -> {
            if (contextSupplier.get().getSender().level() instanceof ServerLevel serverLevel){
                BlockPos blockPos = new BlockPos(dat.getInt("x"),dat.getInt("y"),dat.getInt("z"));
                if (serverLevel.hasChunkAt(blockPos)){
                    serverLevel.getBlockEntity(blockPos, BlockEntityRegister.BASECORE.get()).ifPresent(BaseCoreBlockEntity::updateToClient);
                }
            }
        });
        super.server(contextSupplier, dat);
    }
}
