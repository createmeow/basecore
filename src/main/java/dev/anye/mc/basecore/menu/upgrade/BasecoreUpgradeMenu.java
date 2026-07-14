package dev.anye.mc.basecore.menu.upgrade;

import com.mojang.logging.LogUtils;
import dev.anye.mc.basecore.block.BlockRegister;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.cap.PartHolder;
import dev.anye.mc.basecore.config.BasecoreConfig;
import dev.anye.mc.basecore.item.ItemRegister;
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
    private final BaseCoreBlockEntity baseCore;
    public BaseCoreBlockEntity getBaseCore() { return baseCore; }
    /** Turret position for defense mode switching; null for base core */
    private net.minecraft.core.BlockPos defendPos;
    public net.minecraft.core.BlockPos getDefendPos() { return defendPos; }
    public boolean isDefend() { return isDefend; }
    private final boolean isDefend;
    private List<UpgradeEntry> entries = new ArrayList<>();
    private ServerPlayer serverPlayer;

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
        if (inv.player instanceof ServerPlayer sp) {
            this.serverPlayer = sp;
        }
        rebuildEntries();
        // Send initial data to client (parts count + entry list)
        syncToClient();
    }

    public void rebuildEntries() {
        entries = new ArrayList<>();
        if (baseCore == null) return;

        if (isDefend) {
            // Defense turret upgrades (each module type maps to DefendBlockEntity.itemChange())
            addEntry(ItemRegister.RangeModule.get(), BasecoreConfig.getUpgradeDefendRangeCost());
            addEntry(ItemRegister.DefModule.get(), BasecoreConfig.getUpgradeDefendDefenseCost());
            addEntry(ItemRegister.ThornsModule.get(), BasecoreConfig.getUpgradeDefendThornsCost());
            addEntry(ItemRegister.AutoRepairModule.get(), BasecoreConfig.getUpgradeDefendAutoRepairCost());
            addEntry(ItemRegister.StrengthModule.get(), BasecoreConfig.getUpgradeDefendStrengthCost());
        } else {
            addEntry(ItemRegister.RangeModule.get(), BasecoreConfig.getUpgradeRangeCost());
            addEntry(ItemRegister.DefModule.get(), BasecoreConfig.getUpgradeDefenseCost());
            addEntry(ItemRegister.SecureModule.get(), BasecoreConfig.getUpgradeSecureCost());
            addEntry(ItemRegister.AutoRepairModule.get(), BasecoreConfig.getUpgradeAutoRepairCost());
            addEntry(ItemRegister.ThornsModule.get(), BasecoreConfig.getUpgradeThornsCost());
            addEntry(ItemRegister.ExpDefModule.get(), BasecoreConfig.getUpgradeExpDefCost());
            addEntry(ItemRegister.BasecoreCounterReconnaissanceMdoule.get(), BasecoreConfig.getUpgradeCounterReconCost());
            addEntry(ItemRegister.StrengthModule.get(), BasecoreConfig.getUpgradeStrengthCost());
            addEntry(ItemRegister.JumpBoostModule.get(), BasecoreConfig.getUpgradeJumpBoostCost());
            addEntry(ItemRegister.RegenerationModule.get(), BasecoreConfig.getUpgradeRegenerationCost());
            addEntry(ItemRegister.ResistanceModule.get(), BasecoreConfig.getUpgradeResistanceCost());
            addEntry(ItemRegister.DigSpeedModule.get(), BasecoreConfig.getUpgradeDigSpeedCost());
            addEntry(ItemRegister.MovementSpeedModule.get(), BasecoreConfig.getUpgradeMovementSpeedCost());
            // Negative effect modules (for invaders)
            addEntry(ItemRegister.DigSlowdownModule.get(), BasecoreConfig.getUpgradeDigSlowdownCost());
            addEntry(ItemRegister.WeaknessModule.get(), BasecoreConfig.getUpgradeWeaknessCost());
            addEntry(ItemRegister.MovementSlowdownModule.get(), BasecoreConfig.getUpgradeMovementSlowdownCost());
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
        return ItemStack.EMPTY;
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
                player, BlockRegister.BASE_CORE.get());
    }
}
