package dev.anye.mc.basecore.net.easy_net;

import net.minecraft.nbt.CompoundTag;

public abstract class EasyNetCore implements EasyNetInterface {
    @Override
    public abstract void client(CompoundTag dat);
    @Override
    public abstract void server(CompoundTag dat);
}