package dev.anye.mc.basecore.net.easy_net;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface EasyNetInterface {
    void server(Supplier<NetworkEvent.Context> contextSupplier , CompoundTag dat);
    void client(Supplier<NetworkEvent.Context> contextSupplier , CompoundTag dat);
}
