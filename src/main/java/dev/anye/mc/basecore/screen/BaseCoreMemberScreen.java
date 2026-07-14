package dev.anye.mc.basecore.screen;

import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.menu.BaseCoreMenu;
import dev.anye.mc.basecore.net.MemberActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Member management screen for modular base core.
 * Completely independent from slot rendering/interaction.
 * Only shows member info and management UI.
 */
public class BaseCoreMemberScreen extends AbstractContainerScreen<BaseCoreMenu> {
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 200;

    private Button tabModules, tabMembers;

    private EditBox memberInput;
    private Button addBtn;
    private final List<Button> removeBtns = new ArrayList<>();
    private final Inventory playerInv;
    private int lastMemberCount = -1;

    public BaseCoreMemberScreen(BaseCoreMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.playerInv = pPlayerInventory;
        imageWidth = IMAGE_WIDTH;
        imageHeight = IMAGE_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;

        // Tab buttons
        tabModules = addRenderableWidget(Button.builder(
                Component.literal("§6模块管理"),
                b -> switchToModuleTab()
        ).bounds(leftPos + 2, topPos - 10, imageWidth / 2 - 2, 12).build());

        tabMembers = addRenderableWidget(Button.builder(
                Component.literal("§6成员管理"),
                b -> { /* already here */ }
        ).bounds(leftPos + imageWidth / 2, topPos - 10, imageWidth / 2, 12).build());
        tabMembers.active = false;

        // Member input
        memberInput = new EditBox(font, leftPos + 4, topPos + 168, 120, 14, Component.literal(""));
        memberInput.setMaxLength(16);
        addRenderableWidget(memberInput);

        addBtn = addRenderableWidget(Button.builder(
                Component.literal("§a添加"),
                b -> {
                    String name = memberInput.getValue().trim();
                    if (!name.isEmpty()) {
                        PacketDistributor.sendToServer(new MemberActionPayload(name, true));
                        memberInput.setValue("");
                    }
                }
        ).bounds(leftPos + 128, topPos + 166, 42, 16).build());

        rebuildRemoveButtons();
    }

    private void switchToModuleTab() {
        BasecoreScreenSwitcher.switchTo(minecraft, new BaseCoreScreen(menu, playerInv, title));
    }

    private void rebuildRemoveButtons() {
        for (Button btn : removeBtns) removeWidget(btn);
        removeBtns.clear();

        BaseCoreBlockEntity be = menu.getBaseCoreBE();
        if (be == null) return;
        int startY = topPos + 44;
        int idx = 0;
        for (UUID uid : be.getEntityData().getPermittedPlayers()) {
            int y = startY + idx * 14;
            Button btn = Button.builder(
                    Component.literal("§c✕"),
                    b -> PacketDistributor.sendToServer(new MemberActionPayload(be.getEntityData().getMemberName(uid), false))
            ).bounds(leftPos + 150, y, 16, 12).build();
            addRenderableWidget(btn);
            removeBtns.add(btn);
            idx++;
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float v, int mx, int my) {
        // Solid background covering the entire screen area (no slots visible)
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
        g.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, 0xFF333333);

        renderMemberTab(g, mx, my);
    }

    private void renderMemberTab(GuiGraphics g, int mx, int my) {
        BaseCoreBlockEntity be = menu.getBaseCoreBE();
        if (be == null) return;

        // Owner info
        String ownerName = be.getEntityData().getOwnerName();
        if (ownerName.isEmpty()) ownerName = "无";
        g.drawString(font, "§6领主: §e" + ownerName, leftPos + 8, topPos + 18, 0xFFFFFFFF, false);

        // Member count
        g.drawString(font, "§6成员: §e" + be.getEntityData().getPermittedPlayers().size(), leftPos + 8, topPos + 30, 0xFFFFFFFF, false);

        // Input label
        g.drawString(font, "§7添加成员:", leftPos + 8, topPos + 160, 0xFFAAAAAA, false);

        // Member list
        int startY = topPos + 44;
        int idx = 0;
        for (UUID uid : be.getEntityData().getPermittedPlayers()) {
            int y = startY + idx * 14;
            String name = be.getEntityData().getMemberName(uid);
            g.fill(leftPos + 8, y, leftPos + 146, y + 12, 0xFF444444);
            g.drawString(font, "§7● §f" + name, leftPos + 10, y + 1, 0xFFFFFFFF, false);
            idx++;
        }
    }

    // ----- Completely override rendering to exclude all slot/hover logic -----

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        // Refresh member list and remove buttons if data changed
        BaseCoreBlockEntity be = menu.getBaseCoreBE();
        if (be != null) {
            int count = be.getEntityData().getPermittedPlayers().size();
            if (count != lastMemberCount) {
                lastMemberCount = count;
                rebuildRemoveButtons();
            }
        }
        this.renderBackground(g, mx, my, partialTick);
        // Render widgets (buttons, edit box) directly, same as Screen.render()
        for (Renderable renderable : this.renderables) {
            renderable.render(g, mx, my, partialTick);
        }
        this.renderBg(g, partialTick, mx, my);
        this.renderLabels(g, mx, my);
        this.renderTooltip(g, mx, my);
    }

    // ----- Prevent all slot mouse interactions -----

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // Only handle widget clicks (buttons, edit box), not slot clicks
        for (var widget : this.children()) {
            if (widget.isMouseOver(mx, my) && widget.mouseClicked(mx, my, button)) {
                this.setFocused(widget);
                if (button == 0) this.setDragging(true);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dragX, double dragY) {
        return false;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        return false;
    }

    // ----- E key handling -----

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            if (memberInput.isFocused()) {
                return true;
            }
            onClose();
            return true;
        }
        if (memberInput.isFocused() && (keyCode == 257 || keyCode == 335)) {
            String name = memberInput.getValue().trim();
            if (!name.isEmpty()) {
                PacketDistributor.sendToServer(new MemberActionPayload(name, true));
                memberInput.setValue("");
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
