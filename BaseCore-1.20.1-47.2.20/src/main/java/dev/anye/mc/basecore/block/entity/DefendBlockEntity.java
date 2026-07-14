package dev.anye.mc.basecore.block.entity;

import dev.anye.mc.basecore.basecore.BasecoreServerHelper;
import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.IHealth;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.effect.EffectRegister;
import dev.anye.mc.basecore.item.ItemRegister;
import dev.anye.mc.basecore.item.module.DefendBlockModule;
import dev.anye.mc.basecore.menu.DefendMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DefendBlockEntity extends ToDamageBlockEntity<DefendBlockModule> implements MenuProvider, IHealth {
    private int range = 15;
    private int health = 50;
    private int maxHealth = 50;
    private UUID owner;
    private int tick = 0;
    private int type = 1;
    private float amount = 4f;

    protected final ContainerData data;
    private final Map<DefendBlockModule,Integer> modules = new HashMap<>();
    public DefendBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegister.Defend.get(), pPos, pBlockState, 27);
        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i){
                    case 0 -> DefendBlockEntity.this.range;
                    case 1 -> DefendBlockEntity.this.getHealth();
                    case 2 -> DefendBlockEntity.this.getMaxHealth();
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int i1) {
                switch (i){
                    case 0 -> DefendBlockEntity.this.health = i1;
                }

            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (level instanceof ServerLevel serverLevel){
            if (tick >= 80) {
                tick = 0;
                boolean[] has = {false};
                if (type == 2 || type == 3) {
                    BaseCoreBlockEntity baseCoreBlockEntity = BasecoreServerHelper.getBasecore(this.level,blockPos.getCenter());
                    serverLevel.getPlayers(serverPlayer -> {
                        if (canUse(serverPlayer.getUUID())) return false;
                        return (serverPlayer.distanceToSqr(this.getBlockPos().getCenter()) < Math.pow(range, 2));

                    }).forEach(serverPlayer -> {
                        if (has[0]) return;
                        if (serverPlayer.hasEffect(EffectRegister.Disguise.get())) {
                            if (!serverPlayer.getPersistentData().getBoolean("bcrm.basecore.isDiscern"))
                                return;
                        }
                        serverPlayer.level().playSound(
                                null,
                                blockPos.getX() + 0.5,
                                blockPos.getY() + 0.5,
                                blockPos.getZ() + 0.5,
                                SoundEvents.FIREWORK_ROCKET_LAUNCH,
                                SoundSource.BLOCKS,
                                1.0F,
                                serverPlayer.level().getRandom().nextFloat() * 0.4F + 0.8F
                        );
                        has[0] = true;
                        serverPlayer.hurt(serverPlayer.damageSources().fellOutOfWorld(), getAmount());
                    });
                }
                if (type == 1 || type == 3){
                    List<LivingEntity> entities = new ArrayList<>();
                    serverLevel.getEntities().get(new AABB(this.getBlockPos()).inflate(range),entity -> {
                        if (entity instanceof LivingEntity livingEntity && livingEntity.getType().getCategory() == MobCategory.MONSTER){
                            entities.add(livingEntity);
                        }
                    });
                    entities.forEach(livingEntity -> {
                        if (has[0]) return;
                        has[0] = true;
                        livingEntity.hurt(livingEntity.damageSources().fellOutOfWorld(), getAmount());

                        livingEntity.level().playSound(
                                null,
                                blockPos.getX() + 0.5,
                                blockPos.getY() + 0.5,
                                blockPos.getZ() + 0.5,
                                SoundEvents.FIREWORK_ROCKET_LAUNCH,
                                SoundSource.BLOCKS,
                                1.0F,
                                livingEntity.level().getRandom().nextFloat() * 0.4F + 0.8F
                        );
                    });
                            /*
                            //.getEntities((Entity) null,new AABB(this.getBlockPos()), entity -> entity.getType().getCategory() == MobCategory.MONSTER).forEach(entity -> {
                        System.out.println("===========entity==========");
                        if (entity instanceof LivingEntity livingEntity){
                            livingEntity.hurt(livingEntity.damageSources().fellOutOfWorld(), amount);
                        }
                    });

                             */
                }
            }else tick ++;
        }
    }
    public float getFixAmount(){
        return getModuleLvl(ItemRegister.DefendDamageModule.get()) + getAmount();
    }

    public float getAmount() {
        return amount;
    }

    public void setType() {
        this.type ++;
        if (type > 3 ) type = 1;
    }

    public void setOwner(UUID owner) {
        if (this.owner == null) this.owner = owner;
    }
    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putInt("range",range);
        pTag.putInt("health",health);
        pTag.putInt("max_health",maxHealth);
        pTag.putInt("tick",tick);
        pTag.putInt("type",type);
        if (owner != null) pTag.putUUID("owner",owner);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        range = pTag.getInt("range");
        health = pTag.getInt("health");
        maxHealth = pTag.getInt("max_health");
        tick = pTag.getInt("tick");
        type = pTag.getInt("type");
        if (pTag.hasUUID("owner")) owner = pTag.getUUID("owner");
        itemChange();
    }

    @Override
    public void addHealth(int health) {
        this.health += health;
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
        addHealth(-health);
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public boolean canUse(UUID user) {
        BaseCoreBlockEntity baseCoreBlockEntity = BasecoreServerHelper.getBasecore(this.level, this.getBlockPos().getCenter());
        if (baseCoreBlockEntity != null){
            return baseCoreBlockEntity.canUse(user);
        }
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.basecore.defend");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new DefendMenu(pContainerId,pPlayerInventory,this,this.data);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        itemChange();
    }


    private void itemChange(){
        range = 15;
        maxHealth = 50;
        Map<DefendBlockModule,Integer> tmap = new HashMap<>();
        for (int i = 0 ; i < items.getSlots(); i ++){
            if (items.getStackInSlot(i).getItem() instanceof DefendBlockModule basecoreModuleItem) {
                tmap.put(basecoreModuleItem,tmap.getOrDefault(basecoreModuleItem,0) + items.getStackInSlot(i).getCount());
            }
        }
        tmap.forEach((basecoreModuleItem, integer) -> {
            range += basecoreModuleItem.getRange(integer);
            maxHealth += basecoreModuleItem.getHealth(integer);
        });
        amount = 4 + tmap.getOrDefault(ItemRegister.DefendDamageModule.get(),0);
        modules.clear();
        modules.putAll(tmap);
    }

    @Override
    public int getModuleLvl(DefendBlockModule item) {
        return modules.getOrDefault(item,0);
    }
    public int getPlayerType() {
        return type;
    }
}
