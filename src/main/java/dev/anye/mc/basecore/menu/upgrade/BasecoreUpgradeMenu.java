package dev.anye.mc.basecore.menu.upgrade;

import com.mojang.logging.LogUtils;
import dev.anye.mc.basecore.block.BlockRegister;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.cap.PartHolder;
import dev.anye.mc.basecore.item.module.BasecoreModuleItem;
import dev.anye.mc.basecore.menu.MenuTypeRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class BasecoreUpgradeMenu extends AbstractContainerMenu {
    private static final Logger LOGGER = LogUtils.getLogger();
    /** 内容区宽度：与 9 格背包等宽。 */
    public static final int CONTENT_WIDTH = 9 * 18;
    /** 能源槽 / 背包 / 快捷栏的纵向坐标（横向由 contentX 动态决定）。 */
    public static final int ENERGY_SLOT_Y = 110;
    public static final int MAIN_INV_Y = 138;
    public static final int HOTBAR_Y = 194;
    private boolean hasEnergySlot;

    private final BaseCoreBlockEntity baseCore;
    public BaseCoreBlockEntity getBaseCore() { return baseCore; }
    /** Turret position for defense mode switching; null for base core */
    private net.minecraft.core.BlockPos defendPos;
    public net.minecraft.core.BlockPos getDefendPos() { return defendPos; }
    public boolean isDefend() { return isDefend; }
    private final boolean isDefend;
    /** 目标是否为苏式基地计算机（决定主题、方块校验与升级项范围）。 */
    private final boolean isSoviet;
    /** 内容区左端：苏式计算机居中（34），基地核心靠左（8）。 */
    private final int contentX;
    private final net.minecraft.world.inventory.ContainerData energyData;
    private List<UpgradeEntry> entries = new ArrayList<>();
    private ServerPlayer serverPlayer;

    public int getContentX() {
        return contentX;
    }

    public int getContentRight() {
        return contentX + CONTENT_WIDTH;
    }

    public BasecoreUpgradeMenu(int containerId, Inventory inv, FriendlyByteBuf buf) {
        this(containerId, inv, inv.player.level().getBlockEntity(buf.readBlockPos()), buf.readBoolean());
        // If is defend turret, read the turret position from the buffer
        if (this.isDefend && buf.readableBytes() >= 12) {
            this.defendPos = buf.readBlockPos();
        }
    }

    public BasecoreUpgradeMenu(int containerId, Inventory inv, BlockEntity entity, boolean isDefend) {
        this(containerId, inv, entity, isDefend, null);
    }

    public BasecoreUpgradeMenu(int containerId, Inventory inv, BlockEntity entity, boolean isDefend, BlockPos defendPos) {
        super(MenuTypeRegister.BASECORE_UPGRADE_MENU.get(), containerId);
        this.isDefend = isDefend;
        this.defendPos = defendPos;
        if (entity instanceof BaseCoreBlockEntity be) {
            this.baseCore = be;
        } else {
            this.baseCore = null;
        }
        this.isSoviet = this.baseCore instanceof dev.anye.mc.basecore.block.soviet_computer.SovietComputerBlockEntity;
        // 面板宽度按内容区收缩，两种机器内容区都居中（左右各 8px）
        this.contentX = 8;
        if (inv.player instanceof ServerPlayer sp) {
            this.serverPlayer = sp;
        }

        // 能源数据：服务器端实时读取方块实体，客户端由数据槽同步
        if (!inv.player.level().isClientSide() && baseCore != null) {
            this.energyData = new net.minecraft.world.inventory.ContainerData() {
                @Override
                public int get(int i) {
                    return i == 0 ? BasecoreUpgradeMenu.this.baseCore.getEnergy()
                            : BasecoreUpgradeMenu.this.baseCore.getMaxEnergy();
                }

                @Override
                public void set(int i, int value) {
                }

                @Override
                public int getCount() {
                    return 2;
                }
            };
        } else {
            this.energyData = new net.minecraft.world.inventory.SimpleContainerData(2);
        }
        this.addDataSlots(this.energyData);

        // 组件模式的能源输入槽：仅基地核心/苏式计算机，防御炮或能源系统关闭时不需要
        this.hasEnergySlot = !isDefend && baseCore != null
                && dev.anye.mc.basecore.config.BasecoreConfig.isEnergyEnabled();
        if (hasEnergySlot) {
            this.addSlot(new net.neoforged.neoforge.items.SlotItemHandler(baseCore.getEnergySlotHandler(), 0,
                    contentX, ENERGY_SLOT_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return dev.anye.mc.basecore.energy.EnergySources.isEnergySource(stack);
                }
            });
        }

        // 玩家背包与快捷栏：组件模式也需要从背包放入红石补充能源
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new net.minecraft.world.inventory.Slot(inv, col + row * 9 + 9,
                        contentX + col * 18, MAIN_INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new net.minecraft.world.inventory.Slot(inv, col,
                    contentX + col * 18, HOTBAR_Y));
        }

        rebuildEntries();
        // Send initial data to client (parts count + entry list)
        syncToClient();
    }

    public void rebuildEntries() {
        entries = new ArrayList<>();
        if (baseCore == null) return;

        // 遍历 API 注册的全部升级项，按当前目标（基地核心/防御炮）过滤
        for (dev.anye.mc.basecore.api.UpgradeDefinition def : dev.anye.mc.basecore.api.BasecoreUpgradeApi.getUpgrades()) {
            if (isDefend && !def.appliesToDefend()) continue;
            if (!isDefend && !def.appliesToBaseCore()) continue;
            dev.anye.mc.basecore.item.module.BasecoreModuleItem module = def.getModule();
            if (module == null) continue;
            addEntry(module, def.getPartCost());
        }
    }

    private void addEntry(BasecoreModuleItem module, int cost) {
        if (isDefend) {
            dev.anye.mc.basecore.block.entity.DefendBlockEntity turret = getTurret();
            if (turret != null) {
                int current = turret.getModuleCount(module);
                int max = turret.getModuleMax(module);
                entries.add(new UpgradeEntry(module, cost, new ItemStack(module), max, current));
                return;
            }
        }
        int current = baseCore != null ? baseCore.getModuleLvl(module) : 0;
        int max = module.getMaxLevel();
        entries.add(new UpgradeEntry(module, cost, new ItemStack(module), max, current));
    }

    private dev.anye.mc.basecore.block.entity.DefendBlockEntity getTurret() {
        if (serverPlayer == null || defendPos == null) return null;
        if (serverPlayer.level().getBlockEntity(defendPos) instanceof dev.anye.mc.basecore.block.entity.DefendBlockEntity turret) {
            return turret;
        }
        return null;
    }

    public List<UpgradeEntry> getEntries() {
        return entries;
    }

    /** 目标是否为苏式基地计算机。 */
    public boolean isSoviet() {
        return isSoviet;
    }

    public int getEnergy() {
        return energyData.get(0);
    }

    public int getMaxEnergy() {
        return Math.max(1, energyData.get(1));
    }

    /**
     * Process a buy ([+1]) or sell-back ([-1]) action.
     */
    public void processAction(int entryIndex, boolean isBuy) {
        if (entryIndex < 0 || entryIndex >= entries.size()) return;
        if (serverPlayer == null) return;
        UpgradeEntry entry = entries.get(entryIndex);

        if (isBuy) {
            handleBuy(entry);
        } else {
            handleSell(entry);
        }
    }

    private void handleBuy(UpgradeEntry entry) {
        if (!entry.canBuy()) {
            serverPlayer.sendSystemMessage(Component.translatable("error.basecore.upgrade.max_level"));
            return;
        }
        int storedParts = PartHolder.getValue(serverPlayer);
        int cost = entry.getPartCost();
        if (storedParts < cost) {
            serverPlayer.sendSystemMessage(Component.translatable("error.basecore.parts.insufficient"));
            return;
        }

        PartHolder.modify(serverPlayer, -cost);
        ItemStack moduleStack = new ItemStack(entry.getModule(), 1);
        if (!placeInInventory(moduleStack)) {
            PartHolder.modify(serverPlayer, cost);
            serverPlayer.sendSystemMessage(Component.translatable("error.basecore.upgrade.no_space"));
            return;
        }
        setChanged();
        rebuildEntries();
        syncToClient();
    }

    private void handleSell(UpgradeEntry entry) {
        if (!entry.canSell()) {
            serverPlayer.sendSystemMessage(Component.translatable("error.basecore.upgrade.no_module_to_sell"));
            return;
        }
        // Only owner can sell back modules
        if (baseCore != null && !serverPlayer.getUUID().equals(baseCore.getOwner())) {
            serverPlayer.sendSystemMessage(Component.translatable("error.basecore.permission.right_click_block"));
            return;
        }
        if (!removeFromInventory(entry.getModule())) {
            serverPlayer.sendSystemMessage(Component.translatable("error.basecore.upgrade.no_module_to_sell"));
            return;
        }
        PartHolder.modify(serverPlayer, entry.getPartCost());
        setChanged();
        rebuildEntries();
        syncToClient();
    }

    /** Add/remove module in the correct inventory (turret or base core). */

    private boolean placeInInventory(ItemStack moduleStack) {
        if (isDefend) {
            dev.anye.mc.basecore.block.entity.DefendBlockEntity turret = getTurret();
            if (turret == null) return false;
            for (int i = 0; i < turret.getItems().getSlots(); i++) {
                ItemStack slotStack = turret.getItems().getStackInSlot(i);
                ItemStack toPlace = moduleStack.copy();
                toPlace.setCount(1);
                if (slotStack.isEmpty()) {
                    turret.getItems().setStackInSlot(i, toPlace);
                    return true;
                } else if (ItemStack.isSameItemSameComponents(slotStack, toPlace)
                        && slotStack.getCount() < slotStack.getMaxStackSize()) {
                    slotStack.grow(1);
                    turret.getItems().setStackInSlot(i, slotStack);
                    return true;
                }
            }
            return false;
        }
        if (baseCore == null) return false;
        for (int i = 0; i < baseCore.getItems().getSlots(); i++) {
            ItemStack slotStack = baseCore.getItems().getStackInSlot(i);
            ItemStack toPlace = moduleStack.copy();
            toPlace.setCount(1);
            if (slotStack.isEmpty()) {
                baseCore.getItems().setStackInSlot(i, toPlace);
                return true;
            } else if (ItemStack.isSameItemSameComponents(slotStack, toPlace)
                    && slotStack.getCount() < slotStack.getMaxStackSize()) {
                slotStack.grow(1);
                baseCore.getItems().setStackInSlot(i, slotStack);
                return true;
            }
        }
        return false;
    }

    private boolean removeFromInventory(net.minecraft.world.item.Item moduleItem) {
        if (isDefend) {
            dev.anye.mc.basecore.block.entity.DefendBlockEntity turret = getTurret();
            if (turret == null) return false;
            for (int i = 0; i < turret.getItems().getSlots(); i++) {
                ItemStack slotStack = turret.getItems().getStackInSlot(i);
                if (!slotStack.isEmpty() && slotStack.getItem() == moduleItem) {
                    slotStack.shrink(1);
                    turret.getItems().setStackInSlot(i, slotStack.isEmpty() ? ItemStack.EMPTY : slotStack);
                    return true;
                }
            }
            return false;
        }
        if (baseCore == null) return false;
        for (int i = 0; i < baseCore.getItems().getSlots(); i++) {
            ItemStack slotStack = baseCore.getItems().getStackInSlot(i);
            if (!slotStack.isEmpty() && slotStack.getItem() == moduleItem) {
                slotStack.shrink(1);
                baseCore.getItems().setStackInSlot(i, slotStack.isEmpty() ? ItemStack.EMPTY : slotStack);
                return true;
            }
        }
        return false;
    }

    private void setChanged() {
        if (isDefend) {
            dev.anye.mc.basecore.block.entity.DefendBlockEntity turret = getTurret();
            if (turret != null) turret.setChanged();
        } else if (baseCore != null) {
            baseCore.setChanged();
        }
    }

    public void syncToClient() {
        if (serverPlayer == null) return;
        int parts = dev.anye.mc.basecore.cap.PartHolder.getValue(serverPlayer);
        var payload = new dev.anye.mc.basecore.net.UpgradeMenuS2CPayload(entries, parts);
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, payload);
    }

    /** Add a player by name as a base member. */
    public void addMember(String playerName) {
        if (serverPlayer == null || baseCore == null) return;
        if (!serverPlayer.getUUID().equals(baseCore.getOwner())) {
            serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.translatable("error.basecore.permission.right_click_block"));
            return;
        }
        net.minecraft.server.players.PlayerList playerList = serverPlayer.server.getPlayerList();
        net.minecraft.server.level.ServerPlayer target = playerList.getPlayerByName(playerName);
        if (target == null) {
            serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("§4未找到在线玩家: " + playerName));
            return;
        }
        if (target.getUUID().equals(baseCore.getOwner())) {
            serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("§4不能将自己添加为成员"));
            return;
        }
        baseCore.getEntityData().addPermissionWithName(target.getUUID(), target.getName().getString());
        baseCore.setChanged();
        baseCore.updateToClient();
        serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a已将 " + playerName + " 添加为基地成员"));
        syncToClient();
    }

    /** Remove a member by name from the base. */
    public void removeMember(String playerName) {
        if (serverPlayer == null || baseCore == null) return;
        if (!serverPlayer.getUUID().equals(baseCore.getOwner())) {
            serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.translatable("error.basecore.permission.right_click_block"));
            return;
        }
        // Try online player first
        net.minecraft.server.players.PlayerList playerList = serverPlayer.server.getPlayerList();
        net.minecraft.server.level.ServerPlayer target = playerList.getPlayerByName(playerName);
        java.util.UUID targetUid = null;
        if (target != null) {
            targetUid = target.getUUID();
        } else {
            // Fallback: search stored member names
            for (java.util.UUID uid : baseCore.getEntityData().getPermittedPlayers()) {
                if (baseCore.getEntityData().getMemberName(uid).equals(playerName)) {
                    targetUid = uid;
                    break;
                }
            }
        }
        if (targetUid == null) {
            serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("§4未找到玩家: " + playerName));
            return;
        }
        if (!baseCore.getEntityData().hasPermission(targetUid)) {
            serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("§4" + playerName + " 不是基地成员"));
            return;
        }
        baseCore.getEntityData().rmUser(targetUid);
        baseCore.setChanged();
        baseCore.updateToClient();
        serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a已将 " + playerName + " 移出基地"));
        syncToClient();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int i) {
        net.minecraft.world.inventory.Slot slot = this.slots.get(i);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (hasEnergySlot) {
            if (i == 0) {
                // 能源槽 -> 背包
                if (!moveItemStackTo(stack, 1, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                // 背包中的能源物品 -> 能源槽
                if (!dev.anye.mc.basecore.energy.EnergySources.isEnergySource(stack)) return ItemStack.EMPTY;
                if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        if (isDefend) {
            if (defendPos == null) return false;
            return stillValid(ContainerLevelAccess.create(player.level(), defendPos),
                    player, dev.anye.mc.basecore.block.BlockRegister.DEFEND.get());
        }
        if (baseCore == null) return false;
        return stillValid(ContainerLevelAccess.create(player.level(), baseCore.getBlockPos()),
                player, isSoviet ? BlockRegister.SOVIET_COMPUTER.get() : BlockRegister.BASE_CORE.get());
    }
}
