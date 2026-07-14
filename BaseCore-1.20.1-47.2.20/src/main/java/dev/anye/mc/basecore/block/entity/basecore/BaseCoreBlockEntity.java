package dev.anye.mc.basecore.block.entity.basecore;

import com.mojang.logging.LogUtils;
import dev.anye.mc.basecore.basecore.BasecoreServerHelper;
import dev.anye.mc.basecore.basecore.UserNameHelper;
import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.INet;
import dev.anye.mc.basecore.block.basecore.BaseCoreBlock;
import dev.anye.mc.basecore.block.entity.ToDamageBlockEntity;
import dev.anye.mc.basecore.effect.EffectRegister;
import dev.anye.mc.basecore.item.ItemRegister;
import dev.anye.mc.basecore.item.module.BasecoreModuleItem;
import dev.anye.mc.basecore.menu.BaseCoreMenu;
import dev.anye.mc.basecore.net.Net;
import dev.anye.mc.basecore.net.NetReg;
import dev.anye.mc.basecore.net.easy_net.EasyNetSTC;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BaseCoreBlockEntity  extends ToDamageBlockEntity<BasecoreModuleItem> implements MenuProvider, INet {
    private static final Logger LOGGER = LogUtils.getLogger();
    public final Map<BasecoreModuleItem,Integer> modules = new HashMap<>();
    private @Nullable UUID owner = null;
    private final BasecoreBlockEntityData entityData;
    protected final ContainerData data;

    public BaseCoreBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegister.BASECORE.get(), pPos, pBlockState,27);
        entityData = new BasecoreBlockEntityData();
        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i){
                    case 0 -> BaseCoreBlockEntity.this.entityData.getHealth();
                    case 1 -> BaseCoreBlockEntity.this.entityData.getMaxHealth();
                    case 2 -> BaseCoreBlockEntity.this.entityData.getRange();
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int i1) {
                switch (i){
                    case 0 -> BaseCoreBlockEntity.this.entityData.setHealth(i1);
                }

            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    @Override
    public void setChanged() {
        super.setChanged();
        itemChange();
        updateToClient();
    }

    public BasecoreBlockEntityData getEntityData(){
        return entityData;
    }

    public boolean canUse(UUID user){
        return check(user,owner);
    }
    public boolean check(UUID user,UUID owner){
        return owner == null || user.equals(owner) || entityData.hasPermission(user);
    }

    private void itemChange(){
        entityData.setDefaultMaxHealth();
        entityData.setDefaultRange();
        Map<BasecoreModuleItem,Integer> tmap = new HashMap<>();
        for (int i = 0 ; i < items.getSlots(); i ++){
            if (items.getStackInSlot(i).getItem() instanceof BasecoreModuleItem basecoreModuleItem) {
                tmap.put(basecoreModuleItem,tmap.getOrDefault(basecoreModuleItem,0) + items.getStackInSlot(i).getCount());
            }
        }
        modules.clear();
        modules.putAll(tmap);
        modules.forEach((basecoreModuleItem, integer) -> {
            entityData.addMaxHealth(basecoreModuleItem.getHealth(integer));
            entityData.addRange(basecoreModuleItem.getRange(integer));
        });
    }

    public @Nullable UUID getOwner() {
        return owner;
    }

    public int getRange(){
        return entityData.getRange();
    }
    public int getMaxHealth(){
        return entityData.getMaxHealth();
    }


    public void damage(int health){
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

    public int getModuleLvl(BasecoreModuleItem module){
        return modules.getOrDefault(module,0);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.basecore.basecore");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new BaseCoreMenu(i,inventory,this,this.data);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        entityData.tick();

        Map<BasecoreModuleItem,Integer> tmap = new HashMap<>(modules);
        tmap.forEach((basecoreModuleItem, integer) -> {
            if (basecoreModuleItem.isHasTick()) basecoreModuleItem.runTick(this,level,blockPos,blockState,integer);
        });
    }

    public void setOwner(UUID owner) {
        if (this.owner == null) this.owner = owner;
        String baseName = new UserNameHelper().getName(owner) + "的基地";
        String name = baseName;
        int i = 0;
        while (BasecoreServerHelper.hasName(name)){
            i++;
            name = baseName + i;
        }
        entityData.setName(name);
        updateToClient();
    }


    @Override
    public void onLoad() {
        super.onLoad();
        itemChange();
    }

    //--------------------------------------------------------------------------


    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);
        if (owner != null) pTag.putUUID(BaseCoreBlock.OWNER_KEY,owner);
        entityData.saveToNbt(pTag);
    }
    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        if (pTag.hasUUID(BaseCoreBlock.OWNER_KEY)) owner = pTag.getUUID(BaseCoreBlock.OWNER_KEY);
        itemChange();
        entityData.handle(pTag);
    }


    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag compoundTag = new CompoundTag();
        saveAdditional(compoundTag);
        return compoundTag;
    }

    @Override
    public void updateToClient(){
        if (level instanceof ServerLevel serverLevel){
            //setChanged();
            serverLevel.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(), Block.UPDATE_ALL);
            /*
            if (owner != null) {
                entityData.setName(BasecoreNames.I.getName(owner));
            }

             */
            CompoundTag data = entityData.saveToNbt();
            data.putInt("block.x",this.getBlockPos().getX());
            data.putInt("block.y",this.getBlockPos().getY());
            data.putInt("block.z",this.getBlockPos().getZ());
            data.putString(Net.EASY_NET_KEY, NetReg.BasecoreNet.getId().toString());
            Net.EasyNetSTC(PacketDistributor.ALL.noArg(), new EasyNetSTC(data));
        }
    }

    public void handlePacket(CompoundTag dat) {
        entityData.handle(dat);
    }

    public void addHealth(int repair) {
        entityData.addHealth(repair);
        updateToClient();
    }
}
