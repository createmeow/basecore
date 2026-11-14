package dev.anye.mc.basecore.screen;

import dev.anye.mc.basecore.block.soviet_computer.SovietComputerBlockEntity;
import dev.anye.mc.basecore.menu.SovietComputerMenu;
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
 * 苏式基地计算机成员页：与主界面同一套黑底黄框风格，
 * 中部成员列表，下方成员输入框与基地改名输入框，底部血量与状态。
 */
public class SovietComputerMemberScreen extends AbstractContainerScreen<SovietComputerMenu> {
    private static final int WINDOW_HEIGHT = 192;

    private static final int MEMBER_PANEL_TOP = 20;
    private static final int MEMBER_PANEL_BOTTOM = 134;
    private static final int MEMBER_LIST_Y = 28;
    private static final int MAX_VISIBLE_MEMBERS = 7;
    private static final int REMOVE_X = 174;

    /** 添加成员输入行。 */
    private static final int INPUT_TOP = 138;
    private static final int INPUT_BOTTOM = 154;
    /** 基地改名输入行。 */
    private static final int NAME_TOP = 158;
    private static final int NAME_BOTTOM = 174;

    private static final int BUTTON_X = 150;
    private static final int STATUS_Y = 180;

    private final Inventory playerInv;
    private EditBox memberInput;
    private EditBox nameInput;
    private int memberMaxLength = 16;
    private int nameMaxLength = RenameBasePayload.MAX_NAME_LENGTH;

    public SovietComputerMemberScreen(SovietComputerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.playerInv = inventory;
        this.imageWidth = SovietComputerScreen.WINDOW_WIDTH;
        this.imageHeight = WINDOW_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        memberInput = createInput(INPUT_TOP, memberMaxLength);
        addRenderableWidget(memberInput);

        nameInput = createInput(NAME_TOP, nameMaxLength);
        SovietComputerBlockEntity be = this.menu.getComputer();
        if (be != null) nameInput.setValue(be.getName());
        addRenderableWidget(nameInput);
    }

    /** 无边框输入框：原版带边框模式会绘制浅灰贴图，与黑黄主题不符。 */
    private EditBox createInput(int top, int maxLength) {
        EditBox box = new EditBox(this.font, leftPos + 6, topPos + top + 4, 138, 14, Component.literal(""));
        box.setBordered(false);
        box.setMaxLength(maxLength);
        box.setTextColor(0xFFFFCC00);
        return box;
    }

    private void submitMember() {
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

    private List<UUID> visibleMembers() {
        List<UUID> list = new ArrayList<>();
        SovietComputerBlockEntity be = this.menu.getComputer();
        if (be == null) return list;
        for (UUID uid : be.getEntityData().getPermittedPlayers()) {
            if (list.size() >= MAX_VISIBLE_MEMBERS) break;
            list.add(uid);
        }
        return list;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        graphics.fill(x, y, x + imageWidth, y + imageHeight, SovietComputerScreen.BLACK);
        SovietComputerScreen.drawTab(graphics, x + SovietComputerScreen.TAB_MODULES_X, y + SovietComputerScreen.TAB_Y, false);
        SovietComputerScreen.drawTab(graphics, x + SovietComputerScreen.TAB_MEMBERS_X, y + SovietComputerScreen.TAB_Y, true);

        SovietComputerScreen.drawPanel(graphics, x + SovietComputerScreen.PANEL_LEFT, y + MEMBER_PANEL_TOP,
                x + SovietComputerScreen.PANEL_RIGHT, y + MEMBER_PANEL_BOTTOM);

        // 输入框位置指示：亮黄细框（输入框本身无边框，只有黄色文字）
        drawInputFrame(graphics, x + SovietComputerScreen.PANEL_LEFT, y + INPUT_TOP,
                x + BUTTON_X - 2, y + INPUT_BOTTOM);
        drawInputFrame(graphics, x + SovietComputerScreen.PANEL_LEFT, y + NAME_TOP,
                x + BUTTON_X - 2, y + NAME_BOTTOM);
    }

    private void drawInputFrame(GuiGraphics graphics, int x1, int y1, int x2, int y2) {
        int color = SovietComputerScreen.YELLOW;
        graphics.fill(x1, y1, x2, y1 + 1, color);
        graphics.fill(x1, y2 - 1, x2, y2, color);
        graphics.fill(x1, y1, x1 + 1, y2, color);
        graphics.fill(x2 - 1, y1, x2, y2, color);
    }

    /**
     * 原版 EditBox 的插入光标颜色硬编码为灰白，无法通过 API 修改，
     * 因此在控件渲染完成后按主题色覆盖绘制同一位置的 1px 竖线。
     */
    private void drawThemedCursor(GuiGraphics graphics, EditBox box, int maxLength) {
        if (box == null || !box.isFocused()) return;
        String text = box.getValue();
        int cursor = Math.min(box.getCursorPosition(), text.length());
        // 与原版一致：仅当光标右侧有字符或文本已达最大长度时绘制竖线光标；
        // 否则原版绘制下划线 "_"，此时无需覆盖。
        boolean flag2 = cursor < text.length() || text.length() >= maxLength;
        if (!flag2) return;
        int innerWidth = Math.max(1, box.getWidth());
        int displayPos = 0;
        while (displayPos < cursor && this.font.width(text.substring(displayPos, cursor)) > innerWidth) {
            displayPos++;
        }
        // 与原版插入光标完全重合：drawString(带阴影) 返回 x+宽+1，光标再左移 1 → getX()+宽；
        // 竖线从 y-1 起、高 11。
        int cursorX = box.getX() + this.font.width(text.substring(displayPos, cursor));
        int top = box.getY() - 1;
        graphics.fill(cursorX, top, cursorX + 1, top + 11, SovietComputerScreen.YELLOW);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, "苏式基地计算机", 6, 6, SovietComputerScreen.YELLOW, false);
        drawTabLabel(graphics, SovietComputerScreen.TAB_MODULES_X, "模块管理", false);
        drawTabLabel(graphics, SovietComputerScreen.TAB_MEMBERS_X, "成员管理", true);

        SovietComputerBlockEntity be = this.menu.getComputer();
        if (be != null) {
            int idx = 0;
            for (UUID uid : visibleMembers()) {
                int rowY = MEMBER_LIST_Y + idx * 14;
                String name = be.getEntityData().getMemberName(uid);
                graphics.fill(8, rowY, 190, rowY + 12, SovietComputerScreen.PANEL);
                graphics.drawString(this.font, "● " + name, 10, rowY + 2, SovietComputerScreen.YELLOW, false);
                graphics.drawString(this.font, "[x]", REMOVE_X - 2, rowY + 2, SovietComputerScreen.YELLOW, false);
                idx++;
            }
        }

        graphics.drawString(this.font, "添加", BUTTON_X + (SovietComputerScreen.PANEL_RIGHT - BUTTON_X - this.font.width("添加")) / 2,
                INPUT_TOP + 4, SovietComputerScreen.YELLOW, false);
        graphics.drawString(this.font, "改名", BUTTON_X + (SovietComputerScreen.PANEL_RIGHT - BUTTON_X - this.font.width("改名")) / 2,
                NAME_TOP + 4, SovietComputerScreen.YELLOW, false);

        boolean broken = this.menu.isBroken();
        boolean noEnergy = dev.anye.mc.basecore.config.BasecoreConfig.isEnergyEnabled()
                && this.menu.getEnergy() <= 0;
        String line = this.menu.getHealth() + "/" + this.menu.getMaxHealth() + "  "
                + (broken ? "[WARN] 故障 需电子管修复" : (noEnergy ? "[WARN] 能源不足" : "正常工作"));
        graphics.drawString(this.font, line, 6, STATUS_Y, SovietComputerScreen.YELLOW, false);
    }

    private void drawTabLabel(GuiGraphics graphics, int tabX, String label, boolean active) {
        int color = active ? SovietComputerScreen.YELLOW : SovietComputerScreen.YELLOW_TEXT_DIM;
        int textX = tabX + (SovietComputerScreen.TAB_W - this.font.width(label)) / 2;
        graphics.drawString(this.font, label, textX,
                SovietComputerScreen.TAB_Y + (SovietComputerScreen.TAB_H - 8) / 2, color, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        this.renderBg(graphics, partialTick, mouseX, mouseY);
        // 先画背景再画控件，避免面板遮挡输入框
        for (Renderable renderable : this.renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }
        // 原版 EditBox 光标固定灰白，这里覆盖为黄色主题光标
        drawThemedCursor(graphics, memberInput, memberMaxLength);
        drawThemedCursor(graphics, nameInput, nameMaxLength);
        // renderLabels 使用相对 GUI 左上角的坐标，需要手动平移（super.render 才会代劳）
        graphics.pose().pushPose();
        graphics.pose().translate((float) this.leftPos, (float) this.topPos, 0.0F);
        this.renderLabels(graphics, mouseX, mouseY);
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int lx = (int) (mouseX - leftPos);
        int ly = (int) (mouseY - topPos);

        if (button == 0) {
            if (SovietComputerScreen.isInTab(lx, ly, SovietComputerScreen.TAB_MODULES_X)) {
                BasecoreScreenSwitcher.switchTo(this.minecraft,
                        new SovietComputerScreen(this.menu, this.playerInv, this.title));
                return true;
            }
            if (SovietComputerScreen.isInTab(lx, ly, SovietComputerScreen.TAB_MEMBERS_X)) {
                return true;
            }
            if (isInButton(lx, ly, INPUT_TOP, INPUT_BOTTOM)) {
                submitMember();
                return true;
            }
            if (isInButton(lx, ly, NAME_TOP, NAME_BOTTOM)) {
                submitRename();
                return true;
            }
            SovietComputerBlockEntity be = this.menu.getComputer();
            if (be != null) {
                int idx = 0;
                for (UUID uid : visibleMembers()) {
                    int rowY = MEMBER_LIST_Y + idx * 14;
                    if (lx >= REMOVE_X && lx < REMOVE_X + 16 && ly >= rowY && ly < rowY + 12) {
                        PacketDistributor.sendToServer(
                                new MemberActionPayload(be.getEntityData().getMemberName(uid), false));
                        return true;
                    }
                    idx++;
                }
            }
        }

        // 输入框等控件的点击
        for (var widget : this.children()) {
            if (widget.isMouseOver(mouseX, mouseY) && widget.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(widget);
                if (button == 0) this.setDragging(true);
                return true;
            }
        }
        return false;
    }

    private boolean isInButton(int lx, int ly, int top, int bottom) {
        return lx >= BUTTON_X && lx < SovietComputerScreen.PANEL_RIGHT && ly >= top && ly < bottom;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            if (memberInput.isFocused() || nameInput.isFocused()) return true;
            onClose();
            return true;
        }
        if (keyCode == 257 || keyCode == 335) {
            if (memberInput.isFocused()) {
                submitMember();
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
