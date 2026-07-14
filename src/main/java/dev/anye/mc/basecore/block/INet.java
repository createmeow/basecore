package dev.anye.mc.basecore.block;

import net.minecraft.nbt.CompoundTag;

public interface INet {
    void updateToClient();
    void handlePacket(CompoundTag packet);
}