package dev.anye.mc.basecore.net.easy_net;

import net.minecraft.nbt.CompoundTag;

public interface EasyNetInterface {
    void server(CompoundTag dat);
    void client(CompoundTag dat);
}