package dev.anye.mc.basecore.block.soviet_computer;

import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.config.BasecoreConfig;
import dev.anye.mc.basecore.item.ItemRegister;
import dev.anye.mc.basecore.menu.SovietComputerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 苏式基地计算机方块实体。3x3x3 多方块控制器，继承基地核心的全部功能
 * （成员管理、范围保护、模块效果、血量与受击逻辑），并额外拥有：
 * <ul>
 *     <li>随机损坏：隔一段随机时间损失血量；</li>
 *     <li>电子管修复：向 27 个槽位中放入电子管即可恢复血量，同时掉落损坏的电子管。</li>
 * </ul>
 */
public class SovietComputerBlockEntity extends BaseCoreBlockEntity {
    private static final int INVENTORY_SIZE = 27;
    /** 电子管修复间隔（tick）。 */
    private static final int REPAIR_INTERVAL_TICKS = 20;

    private int progress = 0;
    private int fillCooldown = 0;
    private int repairCooldown = 0;
    /** 距离下一次随机损坏的 tick 数，-1 表示尚未初始化。 */
    private int damageCountdown = -1;
    /** 是否处于故障状态：模块效果停止，需要用电子管修复。 */
    private boolean broken = false;
    private int syncTick = 0;

    private final ContainerData sovietData;

    public SovietComputerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegister.SOVIET_COMPUTER.get(), pos, state, INVENTORY_SIZE);
        // 苏式计算机复用基地核心功能，但不额外受冷汗 / 机械动力联动门槛限制
        getEntityData().setColdImmune(true);
        getEntityData().setRotationImmune(true);
        this.sovietData = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> SovietComputerBlockEntity.this.getHealth();
                    case 1 -> SovietComputerBlockEntity.this.getMaxHealth();
                    case 2 -> SovietComputerBlockEntity.this.progress;
                    case 3 -> SovietComputerBlockEntity.this.broken ? 1 : 0;
                    case 4 -> SovietComputerBlockEntity.this.getEnergy();
                    case 5 -> SovietComputerBlockEntity.this.getMaxEnergy();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) SovietComputerBlockEntity.this.getEntityData().setHealth(value);
                if (index == 3) SovietComputerBlockEntity.this.broken = value != 0;
            }

            @Override
            public int getCount() {
                return 6;
            }
        };
    }

    public boolean isBroken() {
        return broken;
    }

    public ContainerData getSovietData() {
        return sovietData;
    }

    public int getProgress() {
        return progress;
    }

    public boolean isAssembled() {
        return level != null && SovietComputerStructure.isComplete(level, getBlockPos());
    }

    /** 3x3x3 结构的几何中心：控制器位于最底层中心，因此向上偏移 1 格。 */
    @Override
    public BlockPos getBaseCenter() {
        return getBlockPos().above();
    }

    /** 苏式计算机的能源消耗速度是普通基地核心的两倍：1 Rs 只够运行 1 秒。 */
    @Override
    protected int getEnergyTicksPerUnit() {
        return BasecoreConfig.getSovietEnergyTicksPerRs();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.basecore.soviet_computer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SovietComputerMenu(containerId, inventory, this, this.sovietData);
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        if (getHealth() <= 0) {
            meltdown(level, pos);
            return;
        }

        boolean complete = SovietComputerStructure.isComplete(level, pos);

        // 未完整时不定期尝试填充外壳；若周围被占用导致失败，保留控制器并稍后重试，避免方块被丢弃
        if (!complete) {
            if (fillCooldown > 0) {
                fillCooldown--;
            } else {
                fillCooldown = 20;
                SovietComputerStructure.fill(level, pos);
                complete = SovietComputerStructure.isComplete(level, pos);
            }
        }

        if (!complete) {
            if (progress != 0) progress = 0;
            return;
        }

        progress = (broken ? 0 : (progress + 1) % 101);

        if (!broken) {
            // 随机损坏（间隔可配置，默认数分钟级）
            if (damageCountdown < 0) damageCountdown = randomDamageInterval(level);
            if (--damageCountdown <= 0) {
                damageCountdown = randomDamageInterval(level);
                malfunction(level, pos);
            }
        }

        // 电子管修复
        if (repairCooldown > 0) {
            repairCooldown--;
        } else {
            repairCooldown = REPAIR_INTERVAL_TICKS;
            tryRepair(level, pos);
        }

        if (broken) {
            // 故障期间模块效果停止，仅保留基础数据同步
            if (++syncTick >= 20) {
                syncTick = 0;
                updateToTrackingOnly();
            }
        } else {
            // 基地核心逻辑：模块 tick、怪物攻核、客户端同步
            super.tick(level, pos, state);
        }
    }

    private int randomDamageInterval(Level level) {
        int min = BasecoreConfig.getSovietDamageMinTicks();
        int max = BasecoreConfig.getSovietDamageMaxTicks();
        if (max <= min) return min;
        return min + level.getRandom().nextInt(max - min + 1);
    }

    /** 随机损坏：只进入故障状态（不扣血），需要用电子管修复。 */
    private void malfunction(Level level, BlockPos pos) {
        broken = true;
        level.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.8f, 0.7f);
        // 通知在线领主
        if (getOwner() != null && level.getServer() != null) {
            net.minecraft.server.level.ServerPlayer owner = level.getServer().getPlayerList().getPlayer(getOwner());
            if (owner != null) {
                String displayName = getName().isEmpty()
                        ? Component.translatable("block.basecore.soviet_computer").getString() : getName();
                owner.sendSystemMessage(Component.translatable("error.basecore.soviet_computer.broken", displayName));
            }
        }
    }

    /** 从 27 个槽位中找到电子管并消耗 1 个，清除故障状态（不恢复血量），同时掉落损坏的电子管。 */
    private void tryRepair(Level level, BlockPos pos) {
        if (!broken) return;
        for (int i = 0; i < getItems().getSlots(); i++) {
            ItemStack stack = getItems().getStackInSlot(i);
            if (!stack.is(ItemRegister.ELECTRONIC_TUBE.get())) continue;

            ItemStack remaining = stack.copy();
            remaining.shrink(1);
            getItems().setStackInSlot(i, remaining);

            broken = false;
            setChanged();

            // 损坏的电子管从结构顶部掉出
            ItemStack brokenTube = new ItemStack(ItemRegister.BROKEN_ELECTRONIC_TUBE.get());
            Block.popResource(level, pos.offset(0, 3, 0), brokenTube);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.6f, 1.2f);
            break;
        }
    }

    private void meltdown(Level level, BlockPos pos) {
        dev.anye.mc.basecore.basecore.BasecoreServerHelper.delEntityAndSend(this);
        level.destroyBlock(pos, false);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("progress", progress);
        tag.putInt("damage_countdown", damageCountdown);
        tag.putBoolean("broken", broken);
    }

    @Override
    public void loadAdditional(CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        resizeLegacyInventory(tag, inventoryItemSaveKey);
        super.loadAdditional(tag, registries);
        progress = tag.getInt("progress");
        damageCountdown = tag.contains("damage_countdown") ? tag.getInt("damage_countdown") : -1;
        broken = tag.getBoolean("broken");
    }

    /**
     * 旧存档中机器只有 9 个槽位（Size=9），直接读取会让 ItemStackHandler 缩容，
     * 而界面固定显示 27 格会越界，这里在读档前把 Size 提升到 27。
     */
    private static void resizeLegacyInventory(CompoundTag tag, String key) {
        if (!tag.contains(key, net.minecraft.nbt.Tag.TAG_COMPOUND)) return;
        CompoundTag inventory = tag.getCompound(key);
        if (!inventory.contains("Size", net.minecraft.nbt.Tag.TAG_INT)) return;
        int size = inventory.getInt("Size");
        if (size >= INVENTORY_SIZE) return;
        inventory.putInt("Size", INVENTORY_SIZE);
    }

    @Override
    public CompoundTag getUpdateTag(@NotNull HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("progress", progress);
        tag.putBoolean("broken", broken);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        resizeLegacyInventory(tag, inventoryItemSaveKey);
        resizeLegacyInventory(tag, "Inventory");
        super.handleUpdateTag(tag, registries);
        progress = tag.getInt("progress");
        broken = tag.getBoolean("broken");
    }
}
