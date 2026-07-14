package dev.anye.mc.basecore.block.entity.basecore;

import com.mojang.logging.LogUtils;
import dev.anye.mc.basecore.block.*;
import dev.anye.mc.basecore.block.entity.ToDamageBlockEntity;
import dev.anye.mc.basecore.item.ItemRegister;
import dev.anye.mc.basecore.item.module.BasecoreModuleItem;
import dev.anye.mc.basecore.item.module.basecore.*;
import dev.anye.mc.basecore.menu.BaseCoreMenu;
import dev.anye.mc.basecore.net.Net;
import dev.anye.mc.basecore.net.NetReg;
import dev.anye.mc.basecore.net.easy_net.EasyNetPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class BaseCoreBlockEntity extends ToDamageBlockEntity<BasecoreModuleItem> implements MenuProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int inventorySize = 27;
    private final Map<BasecoreModuleItem, Integer> modules = new HashMap<>();
    private @Nullable UUID owner = null;
    private final BasecoreBlockEntityData entityData;
    protected final ContainerData data;

    public BaseCoreBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegister.BASECORE.get(), pPos, pBlockState, inventorySize);
        entityData = new BasecoreBlockEntityData();
        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> BaseCoreBlockEntity.this.entityData.getHealth();
                    case 1 -> BaseCoreBlockEntity.this.entityData.getMaxHealth();
                    case 2 -> BaseCoreBlockEntity.this.entityData.getRange();
                    default -> 0;
                };
            }
            @Override
            public void set(int i, int i1) {
                switch (i) {
                    case 0 -> BaseCoreBlockEntity.this.entityData.setHealth(i1);
                }
            }
            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    public BasecoreBlockEntityData getEntityData() {
        return entityData;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        itemChange();
        updateToClient();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        itemChange();
    }

    public void setOwner(UUID owner) {
        if (this.owner == null) this.owner = owner;
        entityData.setOwner(this.owner);
        updateToClient();
    }

    public void setOwnerWithName(UUID owner, String playerName) {
        if (this.owner == null) this.owner = owner;
        entityData.setOwner(this.owner);
        entityData.setOwnerName(playerName);
        String baseName = playerName + "的基地";
        String name = baseName;
        int i = 0;
        while (dev.anye.mc.basecore.basecore.BasecoreServerHelper.hasName(name)) {
            i++;
            name = baseName + i;
        }
        entityData.setName(name);
        updateToClient();
    }

    public @Nullable UUID getOwner() {
        return owner;
    }

    public boolean canUse(UUID user) {
        return check(user, owner);
    }

    public boolean check(UUID user, UUID owner) {
        return owner == null || user.equals(owner) || entityData.hasPermission(user);
    }

    private void itemChange() {
        entityData.setDefaultMaxHealth();
        entityData.setDefaultRange();
        entityData.resetEnemy();
        Map<BasecoreModuleItem, Integer> tmap = new HashMap<>();
        for (int i = 0; i < items.getSlots(); i++) {
            if (items.getStackInSlot(i).getItem() instanceof BasecoreModuleItem basecoreModuleItem) {
                int count = items.getStackInSlot(i).getCount();
                // Negative effect modules cap at 2
                if (basecoreModuleItem instanceof EffectModuleItem efi && efi.isReverse()) {
                    count = Math.min(count, 2);
                }
                tmap.put(basecoreModuleItem, tmap.getOrDefault(basecoreModuleItem, 0) + count);
            }
        }
        modules.clear();
        modules.putAll(tmap);
        modules.forEach((basecoreModuleItem, integer) -> {
            entityData.addMaxHealth(basecoreModuleItem.getHealth(integer));
            entityData.addRange(basecoreModuleItem.getRange(integer));
        });
    }

    public int getRange() {
        return entityData.getRange();
    }

    public int getMaxHealth() {
        return entityData.getMaxHealth();
    }

    public void damage(int health) {
        entityData.addHealth(-health);
        updateToClient();
    }

    public void setHealth(int health) {
        entityData.setHealth(health);
        updateToClient();
    }

    public int getHealth() {
        return entityData.getHealth();
    }

    public void addHealth(int health) {
        entityData.addHealth(health);
        updateToClient();
    }

    public int getModuleLvl(BasecoreModuleItem module) {
        return modules.getOrDefault(module, 0);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.basecore.basecore");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new BaseCoreMenu(i, inventory, this, this.data);
    }

    private int syncTick = 0;

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        entityData.tick();

        Map<BasecoreModuleItem, Integer> tmap = new HashMap<>(modules);
        tmap.forEach((basecoreModuleItem, integer) -> {
            if (basecoreModuleItem.isHasTick())
                basecoreModuleItem.runTick(this, level, blockPos, blockState, integer);
        });

        syncTick++;
        if (syncTick >= 20) {
            syncTick = 0;
            updateToTrackingOnly();
        }
    }

    public void setName(String name) {
        entityData.setName(name);
    }

    public String getName() {
        return entityData.getName();
    }

    public void setInvincibleTime(int invincibleTime) {
        entityData.setInvincibleTime(invincibleTime);
    }

    public int getInvincibleTime() {
        return entityData.getInvincibleTime();
    }

    public void setInterferenceTime(int interferenceTime) {
        entityData.setInterferenceTime(interferenceTime);
    }

    public int getInterferenceTime() {
        return entityData.getInterferenceTime();
    }

    public void setDurabilityDamage(int durabilityDamage) {
        entityData.setDurabilityDamage(durabilityDamage);
    }

    public int getDurabilityDamage() {
        return entityData.getDurabilityDamage();
    }

    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        if (pTag.hasUUID("owner")) owner = pTag.getUUID("owner");
        entityData.handle(pTag);
        itemChange();
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        if (owner != null) pTag.putUUID("owner", owner);
        entityData.saveToNbt(pTag);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag compoundTag = new CompoundTag();
        if (owner != null) compoundTag.putUUID("owner", owner);
        entityData.saveToNbt(compoundTag);
        // Include inventory data for client container rendering
        compoundTag.put("Inventory", getItems().serializeNBT(pRegistries));
        return compoundTag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider pRegistries) {
        if (tag.hasUUID("owner")) owner = tag.getUUID("owner");
        entityData.handle(tag);
        // Restore inventory from update tag if present
        if (tag.contains("Inventory", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            getItems().deserializeNBT(pRegistries, tag.getCompound("Inventory"));
        }
    }

    public void updateToClient() {
        if (level instanceof ServerLevel serverLevel) {
            CompoundTag data = entityData.saveToNbt();
            data.putInt("block.x", this.getBlockPos().getX());
            data.putInt("block.y", this.getBlockPos().getY());
            data.putInt("block.z", this.getBlockPos().getZ());
            data.putString(Net.EASY_NET_KEY, NetReg.BASECORE_BLOCK_KEY);
            serverLevel.players().forEach(serverPlayer -> {
                Net.sendToPlayer(new EasyNetPayload(data), serverPlayer);
            });
        }
    }

    public void updateToTrackingOnly() {
        if (level instanceof ServerLevel serverLevel) {
            CompoundTag data = entityData.saveToNbt();
            data.putInt("block.x", this.getBlockPos().getX());
            data.putInt("block.y", this.getBlockPos().getY());
            data.putInt("block.z", this.getBlockPos().getZ());
            data.putString(Net.EASY_NET_KEY, NetReg.BASECORE_BLOCK_KEY);
            serverLevel.players().forEach(serverPlayer -> {
                Net.sendToPlayer(new EasyNetPayload(data), serverPlayer);
            });
        }
    }

    public void handlePacket(CompoundTag dat) {
        entityData.handle(dat);
    }

    protected Map<BasecoreModuleItem, Long> getModuleCooldowns() {
        return moduleCooldowns;
    }
}