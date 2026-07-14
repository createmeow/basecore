package dev.anye.mc.basecore.net.easy_net;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class EasyNetCore implements EasyNetInterface{
    @Override
    public abstract void client(Supplier<NetworkEvent.Context> contextSupplier , CompoundTag dat);
    @Override
    public abstract void server(Supplier<NetworkEvent.Context> contextSupplier , CompoundTag dat);
}
