package dev.anye.mc.basecore.block.entity;

import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.IHealth;
import dev.anye.mc.basecore.block.InventoryBlockEntity;
import dev.anye.mc.basecore.block.IOwner;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.net.Net;
import dev.anye.mc.basecore.net.NetReg;
import dev.anye.mc.basecore.net.easy_net.EasyNetPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class HashChestBlockEntity extends InventoryBlockEntity implements MenuProvider, IOwner, IHealth {
    public static final int InventorySize = 54;
    private UUID owner;
    private String hash = "";
    public int type = 0;
    private int health = 50;
    private int maxHealth = 50;
    private int showHealthTick = 0;

    public HashChestBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegister.HashChest.get(), pPos, pBlockState, InventorySize);
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Override
    public @Nullable UUID getOwner() {
        return owner;
    }

    @Override
    public boolean canUse(UUID uuid) {
        // type 0: 仅自己
        if (type == 0) {
            return owner != null && owner.equals(uuid);
        }
        // type 1: 基地成员
        if (type == 1) {
            BaseCoreBlockEntity baseCore = getBaseCore();
            if (baseCore != null) {
                return baseCore.canUse(uuid);
            }
            return owner != null && owner.equals(uuid);
        }
        // type 2: 所有人
        return true;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setType() {
        type++;
        if (type > 2) type = 0;
    }

    public int getPlayerType() {
        return type;
    }

    public void showHealth() {
        showHealthTick = 100;
    }

    public int getShowHealthTick() {
        return showHealthTick;
    }

    // ===== IHealth =====
    @Override
    public void addHealth(int health) {
        this.health = Math.min(this.health + health, maxHealth);
        showHealth();
        updateToTracking();
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    public void damage(int health) {
        this.health -= health;
        if (this.health < 0) this.health = 0;
        showHealth();
        updateToTracking();
    }

    public void updateToPlayer(ServerPlayer player) {
        if (level instanceof ServerLevel serverLevel) {
            CompoundTag data = new CompoundTag();
            data.putInt("block.x", getBlockPos().getX());
            data.putInt("block.y", getBlockPos().getY());
            data.putInt("block.z", getBlockPos().getZ());
            data.putInt("health", health);
            data.putInt("maxHealth", maxHealth);
            data.putInt("showHealthTick", showHealthTick);
            data.putString(Net.EASY_NET_KEY, NetReg.HASH_CHEST_HEALTH_KEY);
            Net.sendToPlayer(new EasyNetPayload(data), player);
        }
    }

    public void updateToTracking() {
        if (level instanceof ServerLevel serverLevel) {
            CompoundTag data = new CompoundTag();
            data.putInt("block.x", getBlockPos().getX());
            data.putInt("block.y", getBlockPos().getY());
            data.putInt("block.z", getBlockPos().getZ());
            data.putInt("health", health);
            data.putInt("maxHealth", maxHealth);
            data.putInt("showHealthTick", showHealthTick);
            data.putString(Net.EASY_NET_KEY, NetReg.HASH_CHEST_HEALTH_KEY);
            serverLevel.players().forEach(serverPlayer -> {
                Net.sendToPlayer(new EasyNetPayload(data), serverPlayer);
            });
        }
    }

    @Nullable
    public BaseCoreBlockEntity getBaseCore() {
        if (getLevel() != null){
            return dev.anye.mc.basecore.basecore.BasecoreServerHelper.getBasecore(getLevel(),getBlockPos().getCenter());
        }
        return null;
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("block_entity.basecore.hash_chest");
    }

    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        if (pTag.contains("owner")) owner = pTag.getUUID("owner");
        if (pTag.contains("hash")) hash = pTag.getString("hash");
        if (pTag.contains("player_type")) type = pTag.getInt("player_type");
        if (pTag.contains("health")) health = pTag.getInt("health");
        if (pTag.contains("max_health")) maxHealth = pTag.getInt("max_health");
        if (pTag.contains("showHealthTick")) showHealthTick = pTag.getInt("showHealthTick");
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        if (owner != null) pTag.putUUID("owner", owner);
        pTag.putString("hash", hash);
        pTag.putInt("player_type", type);
        pTag.putInt("health", health);
        pTag.putInt("max_health", maxHealth);
        pTag.putInt("showHealthTick", showHealthTick);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new dev.anye.mc.basecore.menu.HashChestMenu(i, inventory, this);
    }
}