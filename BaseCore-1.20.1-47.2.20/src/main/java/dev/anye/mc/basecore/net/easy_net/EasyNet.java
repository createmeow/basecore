package dev.anye.mc.basecore.net.easy_net;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EasyNet extends EasyNetCore{
    @Override
    public void client(Supplier<NetworkEvent.Context> contextSupplier , CompoundTag dat) {
        contextSupplier.get().setPacketHandled(true);
    }
    @Override
    public void server(Supplier<NetworkEvent.Context> contextSupplier, CompoundTag dat) {
        contextSupplier.get().setPacketHandled(true);
    }
}
