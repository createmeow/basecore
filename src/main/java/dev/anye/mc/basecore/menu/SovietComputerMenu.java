package dev.anye.mc.basecore.menu;

import dev.anye.mc.basecore.block.BlockRegister;
import dev.anye.mc.basecore.block.soviet_computer.SovietComputerBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * 苏式基地计算机 GUI 菜单：上方 9x3 模块槽位 + 下方玩家背包。
 */
public class SovietComputerMenu extends AbstractContainerMenu {
    public static final int MACHINE_COLUMNS = 9;
    public static final int MACHINE_ROWS = 3;
    public static final int MACHINE_SLOT_COUNT = MACHINE_COLUMNS * MACHINE_ROWS;

    /** 机器槽位网格在 GUI 内的左上角坐标。 */
    public static final int MACHINE_X = 17;
    public static final int MACHINE_Y = 25;
    /** 玩家背包（9x3）与快捷栏在 GUI 内的坐标。 */
    public static final int PLAYER_X = 17;
    public static final int PLAYER_Y = 106;
    public static final int HOTBAR_Y = 160;

    private final SovietComputerBlockEntity computer;
    private final ContainerData data;
    /** 服务器端玩家引用，用于成员管理操作。 */
    private final net.minecraft.server.level.ServerPlayer serverPlayer;

    public SovietComputerMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        this(containerId, inventory, inventory.player.level().getBlockEntity(buf.readBlockPos()), new SimpleContainerData(6));
    }

    public SovietComputerMenu(int containerId, Inventory inventory, BlockEntity entity, ContainerData data) {
        super(MenuTypeRegister.SOVIET_COMPUTER_MENU.get(), containerId);
        this.computer = entity instanceof SovietComputerBlockEntity be ? be : null;
        this.data = data;
        this.serverPlayer = inventory.player instanceof net.minecraft.server.level.ServerPlayer sp ? sp : null;

        if (computer != null) {
            for (int row = 0; row < MACHINE_ROWS; row++) {
                for (int col = 0; col < MACHINE_COLUMNS; col++) {
                    int index = row * MACHINE_COLUMNS + col;
                    this.addSlot(new SlotItemHandler(computer.getItems(), index,
                            MACHINE_X + col * 18, MACHINE_Y + row * 18) {
                        @Override
                        public boolean mayPlace(@NotNull ItemStack stack) {
                            return true;
                        }
                    });
                }
            }
        }
        addPlayerInventory(inventory, PLAYER_X, PLAYER_Y, 18);
        addPlayerHotBar(inventory, PLAYER_X, HOTBAR_Y, 18);
        addDataSlots(data);
    }

    public int getHealth() {
        return data.get(0);
    }

    public int getMaxHealth() {
        return Math.max(1, data.get(1));
    }

    public int getProgress() {
        return data.get(2);
    }

    /** 是否处于故障状态（需要用电子管修复）。 */
    public boolean isBroken() {
        return data.get(3) != 0;
    }

    public int getEnergy() {
        return data.get(4);
    }

    public int getMaxEnergy() {
        return Math.max(1, data.get(5));
    }

    private void addPlayerInventory(Inventory playerInv, int x, int y, int space) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, x + col * space, y + row * space));
            }
        }
    }

    private void addPlayerHotBar(Inventory inventory, int x, int y, int space) {
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(inventory, i, x + i * space, y));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, MACHINE_SLOT_COUNT, slots.size(), false)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, 0, MACHINE_SLOT_COUNT, false)) return ItemStack.EMPTY;
        }

        if (stack.getCount() == 0) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (computer == null) return false;
        return stillValid(ContainerLevelAccess.create(player.level(), computer.getBlockPos()),
                player, BlockRegister.SOVIET_COMPUTER.get());
    }

    public SovietComputerBlockEntity getComputer() {
        return computer;
    }

    // ----- 成员管理（与基地核心一致） -----

    public void renameBase(String newName) {
        if (serverPlayer == null || computer == null) return;
        if (!serverPlayer.getUUID().equals(computer.getOwner())) {
            serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.translatable("error.basecore.permission.right_click_block"));
            return;
        }
        String name = newName == null ? "" : newName.trim();
        if (name.isEmpty()) return;
        computer.setName(name);
        computer.setChanged();
        serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.translatable("gui.basecore.basecore.renamed", name));
    }

    public void addMember(String playerName) {
        if (serverPlayer == null || computer == null) return;
        if (!serverPlayer.getUUID().equals(computer.getOwner())) {
            serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.translatable("error.basecore.permission.right_click_block"));
            return;
        }
        net.minecraft.server.players.PlayerList playerList = serverPlayer.server.getPlayerList();
        net.minecraft.server.level.ServerPlayer target = playerList.getPlayerByName(playerName);
        if (target == null) {
            serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("§4未找到在线玩家: " + playerName));
            return;
        }
        if (target.getUUID().equals(computer.getOwner())) {
            serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("§4不能将自己添加为成员"));
            return;
        }
        computer.getEntityData().addPermissionWithName(target.getUUID(), target.getName().getString());
        computer.setChanged();
        serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a已将 " + playerName + " 添加为基地成员"));
    }

    public void removeMember(String playerName) {
        if (serverPlayer == null || computer == null) return;
        if (!serverPlayer.getUUID().equals(computer.getOwner())) {
            serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.translatable("error.basecore.permission.right_click_block"));
            return;
        }
        net.minecraft.server.players.PlayerList playerList = serverPlayer.server.getPlayerList();
        net.minecraft.server.level.ServerPlayer target = playerList.getPlayerByName(playerName);
        java.util.UUID targetUid = null;
        if (target != null) {
            targetUid = target.getUUID();
        } else {
            for (java.util.UUID uid : computer.getEntityData().getPermittedPlayers()) {
                if (computer.getEntityData().getMemberName(uid).equals(playerName)) {
                    targetUid = uid;
                    break;
                }
            }
        }
        if (targetUid == null) {
            serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("§4未找到玩家: " + playerName));
            return;
        }
        if (!computer.getEntityData().hasPermission(targetUid)) {
            serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("§4" + playerName + " 不是基地成员"));
            return;
        }
        computer.getEntityData().rmUser(targetUid);
        computer.setChanged();
        serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a已将 " + playerName + " 移出基地"));
    }
}
