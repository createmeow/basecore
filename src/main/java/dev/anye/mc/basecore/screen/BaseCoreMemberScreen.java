package dev.anye.mc.basecore.screen;

import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.menu.BaseCoreMenu;
import dev.anye.mc.basecore.net.MemberActionPayload;
import dev.anye.mc.basecore.net.RenameBasePayload;
import net.minecraft.client.gui.GuiGraphics;
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
 * 普通基地核心成员管理页（TimeXrebirth 风格：机器材质平均色渐变面板）。
 * 与槽位渲染/交互完全独立，只做成员与基地名称管理。
 */
public class BaseCoreMemberScreen extends AbstractContainerScreen<BaseCoreMenu> {
    private static final int W = 176;
    private static final int H = 220;

    private static final int TAB_Y = 4;
    private static final int TAB_W = 46;
    private static final int TAB_H = 14;
    private static final int TAB_MODULES_X = W - 96;
    private static final int TAB_MEMBERS_X = W - 50;

    private static final int LIST_X = 8;
    private static final int LIST_Y = 44;
    private static final int LIST_W = 160;
    private static final int ROW_H = 14;

    /** 输入框背景（16 高），EditBox 实际文字绘制在背景内 +4 处。 */
    private static final int INPUT_Y = 162;
    private static final int NAME_Y = 192;
    private static final int INPUT_W = 118;
    private static final int INPUT_H = 16;

    /** 自绘按钮（与面板同一主题色，避免原版灰色按钮），与输入框同行。 */
    private static final int BTN_X = 126;
    private static final int BTN_W = 44;
    private static final int BTN_H = 16;
    private static final int ADD_BTN_Y = INPUT_Y;
    private static final int RENAME_BTN_Y = NAME_Y;

    private EditBox memberInput;
    private EditBox nameInput;
    private final Inventory playerInv;
    private final List<UUID> shownMembers = new ArrayList<>();

    public BaseCoreMemberScreen(BaseCoreMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.playerInv = pPlayerInventory;
        this.imageWidth = W;
        this.imageHeight = H;
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;

        // 无边框 EditBox 的文字绘制在 (getX, getY+(h-8)/2)，
        // 因此把 EditBox 缩小放进背景内 (bgX+4, bgY+4, bgW-8, 8)，保证文字与背景对齐
        memberInput = new EditBox(font, leftPos + 8, topPos + INPUT_Y + 4, INPUT_W - 8, 8, Component.literal(""));
        memberInput.setMaxLength(16);
        memberInput.setBordered(false);
        memberInput.setTextColor(BasecoreGuiTheme.TEXT);
        addRenderableWidget(memberInput);

        BaseCoreBlockEntity be = menu.getBaseCoreBE();
        nameInput = new EditBox(font, leftPos + 8, topPos + NAME_Y + 4, INPUT_W - 8, 8, Component.literal(""));
        nameInput.setMaxLength(RenameBasePayload.MAX_NAME_LENGTH);
        nameInput.setBordered(false);
        nameInput.setTextColor(BasecoreGuiTheme.TEXT);
        if (be != null) nameInput.setValue(be.getName());
        addRenderableWidget(nameInput);
    }

    private void submitAdd() {
        String name = memberInput.getValue().trim();
        if (!name.isEmpty()) {
            PacketDistributor.sendToServer(new MemberActionPayload(name, true));
            memberInput.setValue("");
        }
    }

    private void submitRename() {
        String name = nameInput.getValue().trim();
        if (!name.isEmpty()) {
            PacketDistributor.sendToServer(new RenameBasePayload(name));
        }
    }

    private void switchToModuleTab() {
        BasecoreScreenSwitcher.switchTo(minecraft, new BaseCoreScreen(menu, playerInv, title));
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mx, int my) {
        int x = leftPos;
        int y = topPos;
        BasecoreGuiTheme.panel(g, x, y, W, H);
        BasecoreGuiTheme.tab(g, x + TAB_MODULES_X, y + TAB_Y, TAB_W, TAB_H, false);
        BasecoreGuiTheme.tab(g, x + TAB_MEMBERS_X, y + TAB_Y, TAB_W, TAB_H, true);

        BaseCoreBlockEntity be = menu.getBaseCoreBE();
        if (be != null) {
            g.drawString(font, "领主: " + ownerName(be), x + LIST_X + 2, y + 26,
                    BasecoreGuiTheme.TEXT, false);
            g.drawString(font, "成员: " + be.getEntityData().getPermittedPlayers().size(),
                    x + LIST_X + 2, y + 36, BasecoreGuiTheme.TEXT_DIM, false);

            shownMembers.clear();
            shownMembers.addAll(be.getEntityData().getPermittedPlayers());
            for (int i = 0; i < shownMembers.size(); i++) {
                UUID uid = shownMembers.get(i);
                int rowY = y + LIST_Y + i * ROW_H;
                g.fill(x + LIST_X, rowY, x + LIST_X + LIST_W, rowY + ROW_H - 2, 0x33FFFFFF);
                g.drawString(font, be.getEntityData().getMemberName(uid),
                        x + LIST_X + 4, rowY + 2, BasecoreGuiTheme.TEXT, false);
                g.drawString(font, "[x]", x + LIST_X + LIST_W - 18, rowY + 2, 0xFFFFCC00, false);
            }
        }

        // 输入框与按钮：与面板同一主题色，坐标与渲染位置一致
        BasecoreGuiTheme.input(g, x + 4, y + INPUT_Y, INPUT_W, INPUT_H);
        BasecoreGuiTheme.input(g, x + 4, y + NAME_Y, INPUT_W, INPUT_H);
        g.drawString(font, "添加成员:", x + LIST_X, y + INPUT_Y - 10, BasecoreGuiTheme.TEXT_DIM, false);
        g.drawString(font, "基地名称:", x + LIST_X, y + NAME_Y - 10, BasecoreGuiTheme.TEXT_DIM, false);

        BasecoreGuiTheme.button(g, font, x + BTN_X, y + ADD_BTN_Y, BTN_W, BTN_H, "添加",
                BasecoreGuiTheme.hover(x + BTN_X, y + ADD_BTN_Y, BTN_W, BTN_H, mx, my));
        BasecoreGuiTheme.button(g, font, x + BTN_X, y + RENAME_BTN_Y, BTN_W, BTN_H, "改名",
                BasecoreGuiTheme.hover(x + BTN_X, y + RENAME_BTN_Y, BTN_W, BTN_H, mx, my));
    }

    private static String ownerName(BaseCoreBlockEntity be) {
        String ownerName = be.getEntityData().getOwnerName();
        return ownerName.isEmpty() ? "无" : ownerName;
    }

    /** 本页自行接管渲染（无 pose 平移），因此所有文字与面板同样使用绝对坐标。 */
    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        g.drawString(this.font, this.title, x + 8, y + 5, BasecoreGuiTheme.TITLE, false);
        int labelY = y + TAB_Y + (TAB_H - 8) / 2;
        g.drawString(this.font, "模块管理",
                x + TAB_MODULES_X + (TAB_W - this.font.width("模块管理")) / 2, labelY,
                BasecoreGuiTheme.TAB_IDLE, false);
        g.drawString(this.font, "成员管理",
                x + TAB_MEMBERS_X + (TAB_W - this.font.width("成员管理")) / 2, labelY,
                BasecoreGuiTheme.TAB_ACTIVE, false);
    }

    // ----- 完全接管渲染，排除槽位/悬停逻辑 -----

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        this.renderBackground(g, mx, my, partialTick);
        this.renderBg(g, partialTick, mx, my);
        for (Renderable renderable : this.renderables) {
            renderable.render(g, mx, my, partialTick);
        }
        this.renderLabels(g, mx, my);
        this.renderTooltip(g, mx, my);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            int lx = (int) (mx - leftPos);
            int ly = (int) (my - topPos);
            if (inTab(lx, ly, TAB_MODULES_X)) {
                switchToModuleTab();
                return true;
            }
            if (inTab(lx, ly, TAB_MEMBERS_X)) return true;

            if (inButton(lx, ly, ADD_BTN_Y)) {
                submitAdd();
                return true;
            }
            if (inButton(lx, ly, RENAME_BTN_Y)) {
                submitRename();
                return true;
            }

            // 成员行的 [x]
            int row = (ly - LIST_Y) / ROW_H;
            if (ly >= LIST_Y && row < shownMembers.size()) {
                int rx = lx - (LIST_X + LIST_W - 18);
                if (rx >= 0 && rx < 18) {
                    BaseCoreBlockEntity be = menu.getBaseCoreBE();
                    if (be != null) {
                        PacketDistributor.sendToServer(new MemberActionPayload(
                                be.getEntityData().getMemberName(shownMembers.get(row)), false));
                    }
                    return true;
                }
            }

            // 点击输入框背景整体均可聚焦（EditBox 本体比背景小一圈）
            if (inInputBox(lx, ly, INPUT_Y)) {
                memberInput.mouseClicked(mx, my, button);
                this.setFocused(memberInput);
                return true;
            }
            if (inInputBox(lx, ly, NAME_Y)) {
                nameInput.mouseClicked(mx, my, button);
                this.setFocused(nameInput);
                return true;
            }
        }

        for (var widget : this.children()) {
            if (widget.isMouseOver(mx, my) && widget.mouseClicked(mx, my, button)) {
                this.setFocused(widget);
                if (button == 0) this.setDragging(true);
                return true;
            }
        }
        return false;
    }

    private static boolean inTab(int lx, int ly, int tabX) {
        return lx >= tabX && lx < tabX + TAB_W && ly >= TAB_Y && ly < TAB_Y + TAB_H;
    }

    private static boolean inButton(int lx, int ly, int btnY) {
        return lx >= BTN_X && lx < BTN_X + BTN_W && ly >= btnY && ly < btnY + BTN_H;
    }

    private static boolean inInputBox(int lx, int ly, int boxY) {
        return lx >= 4 && lx < 4 + INPUT_W && ly >= boxY && ly < boxY + INPUT_H;
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            if (memberInput.isFocused() || nameInput.isFocused()) {
                return true;
            }
            onClose();
            return true;
        }
        if (keyCode == 257 || keyCode == 335) {
            if (memberInput.isFocused()) {
                String name = memberInput.getValue().trim();
                if (!name.isEmpty()) {
                    PacketDistributor.sendToServer(new MemberActionPayload(name, true));
                    memberInput.setValue("");
                }
                return true;
            }
            if (nameInput.isFocused()) {
                submitRename();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
