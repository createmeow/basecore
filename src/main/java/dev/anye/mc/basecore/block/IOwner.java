package dev.anye.mc.basecore.block;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface IOwner {
    void setOwner(UUID owner);
    @Nullable UUID getOwner();
    boolean canUse(UUID user);
}