package dev.anye.mc.basecore.screen;

import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.item.component.ItemComponentInit;
import dev.anye.mc.basecore.menu.upgrade.BasecoreUpgradeMenu;
import dev.anye.mc.basecore.menu.upgrade.UpgradeEntry;
import dev.anye.mc.basecore.net.MemberActionPayload;
import dev.anye.mc.basecore.net.UpgradeActionPayload;
import dev.anye.mc.basecore.net.UpgradeRefreshPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 零件升级界面（组件玩法）。
 * 普通基地核心使用与模块式玩法一致的 TimeX 灰蓝面板；苏式基地计算机使用自身的黑底黄框主题。
 * 底部为 Rs 能源条与能源输入槽（红石 / 红石块），能源系统关闭时隐藏。
 */
public class BasecoreUpgradeScreen extends AbstractContainerScreen<BasecoreUpgradeMenu> {
    private static final int W = 178;
    private static final int H = 216;
    private static final int ENTRY_HEIGHT = 24;
    private static final int ENTRIES_PER_PAGE = 3;

    private static final int TAB_Y = 4;
    private static final int TAB_W = 46;
    private static final int TAB_H = 14;
    private static final int TAB_UPGRADES_X = W - 96;
    private static final int TAB_MEMBERS_X = W - 50;

    /** 列表区起始 Y（顶栏下方、零件数量下方）。 */
    private static final int ENTRIES_Y = 34;

    /** 能源条：与能源槽同一行；横向跟随内容区。 */
    private static final int BAR_OFFSET_X = 22;
    private static final int BAR_Y = 119;
    private static final int BAR_W = 120;
    private static final int BAR_H = 8;
    /** 能源状态文本位于进度条上方。 */
    private static final int STATUS_Y = 109;

    /** 背包标题。 */
    private static final int INV_LABEL_Y = 129;

    /** 成员页布局。 */
    private static final int MEMBER_LIST_Y = 44;
    private static final int MEMBER_ROW_H = 12;
    private static final int MEMBER_MAX_ROWS = 4;
    private static final int INPUT_FRAME_TOP = 94;
    private static final int INPUT_FRAME_BOTTOM = 106;
    private static final int INPUT_FRAME_W = 120;
    private static final int ADD_BTN_W = 38;
    private static final int ADD_BTN_Y = 92;
    private static final int ADD_BTN_H = 14;

    private int currentTab = 0;

    // Upgrade tab
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private List<UpgradeEntry> entries = new ArrayList<>();
    private int playerParts = 0;

    // Member tab
    private EditBox memberInput;
    private int memberMaxLength = 16;

    public BasecoreUpgradeScreen(BasecoreUpgradeMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = W;
        this.imageHeight = H;
    }

    private boolean soviet() {
        return menu.isSoviet();
    }

    private boolean hasMemberTab() {
        return !menu.isDefend();
    }

    /** 内容区左端（苏式居中、基地核心靠左）。 */
    private int contentX() {
        return menu.getContentX();
    }

    /** 内容区右端，与 9 格背包右边界对齐。 */
    private int contentRight() {
        return menu.getContentRight();
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        if (hasMemberTab()) {
            // 无边框 EditBox 的文字从 getY() 开始绘制，故整体上移 2px 使其在 12px 边框内垂直居中
            memberInput = new EditBox(this.font, leftPos + contentX() + 4, topPos + INPUT_FRAME_TOP + 2,
                    INPUT_FRAME_W - 8, 10, Component.literal(""));
            memberInput.setBordered(false);
            memberInput.setMaxLength(memberMaxLength);
            memberInput.setTextColor(soviet() ? SovietComputerScreen.YELLOW : BasecoreGuiTheme.TEXT);
            addRenderableWidget(memberInput);
        }

        entries = menu.getEntries();
        maxScroll = Math.max(0, entries.size() - ENTRIES_PER_PAGE);

        PacketDistributor.sendToServer(new UpgradeRefreshPayload());
        selectTab(0);
    }

    private void selectTab(int tab) {
        currentTab = tab;
        if (memberInput != null) {
            memberInput.visible = tab == 1;
            memberInput.setFocused(tab == 1);
        }
    }

    public void updateEntries(List<UpgradeEntry> newEntries, int syncedParts) {
        this.entries = newEntries;
        this.playerParts = syncedParts;
        maxScroll = Math.max(0, entries.size() - ENTRIES_PER_PAGE);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
    }

    // ----- 绘制 -----

    @Override
    protected void renderBg(GuiGraphics g, float v, int mx, int my) {
        int x = leftPos;
        int y = topPos;

        if (soviet()) {
            g.fill(x, y, x + W, y + H, SovietComputerScreen.BLACK);
            g.renderOutline(x, y, W, H, SovietComputerScreen.YELLOW_DIM);
        } else {
            BasecoreGuiTheme.panel(g, x, y, W, H);
        }

        drawTab(g, x + TAB_UPGRADES_X, y + TAB_Y, currentTab == 0);
        if (hasMemberTab()) {
            drawTab(g, x + TAB_MEMBERS_X, y + TAB_Y, currentTab == 1);
        }

        if (currentTab == 0) {
            renderUpgradeTab(g, mx, my);
        } else if (hasMemberTab()) {
            renderMemberTab(g, mx, my);
        }
        renderEnergyRow(g);
        renderInventorySlots(g);
    }

    /** 背包与快捷栏槽位底衬：苏式主题不画框，只保留物品显示。 */
    private void renderInventorySlots(GuiGraphics g) {
        if (soviet()) return;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                BasecoreGuiTheme.slot(g, leftPos + contentX() + col * 18,
                        topPos + BasecoreUpgradeMenu.MAIN_INV_Y + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            BasecoreGuiTheme.slot(g, leftPos + contentX() + col * 18,
                    topPos + BasecoreUpgradeMenu.HOTBAR_Y);
        }
    }

    /** 能源槽底衬：两种主题都绘制，便于识别放入红石的位置。 */
    private void drawEnergySlotBg(GuiGraphics g, int x, int y) {
        if (soviet()) {
            g.fill(x, y, x + 16, y + 16, SovietComputerScreen.PANEL);
            g.renderOutline(x - 1, y - 1, 18, 18, SovietComputerScreen.YELLOW_DIM);
        } else {
            BasecoreGuiTheme.slot(g, x, y);
            g.renderOutline(x - 1, y - 1, 18, 18, 0xFF6C7A82);
        }
    }

    private void drawTab(GuiGraphics g, int x, int y, boolean active) {
        if (soviet()) {
            SovietComputerScreen.drawTab(g, x, y, active);
        } else {
            BasecoreGuiTheme.tab(g, x, y, TAB_W, TAB_H, active);
        }
    }

    private void renderUpgradeTab(GuiGraphics g, int mx, int my) {
        int startY = topPos + ENTRIES_Y;
        for (int idx = 0; idx < ENTRIES_PER_PAGE; idx++) {
            int ei = scrollOffset + idx;
            if (ei >= entries.size()) break;
            UpgradeEntry entry = entries.get(ei);
            int y = startY + idx * ENTRY_HEIGHT;
            boolean canAfford = playerParts >= entry.getPartCost();
            // 列表宽度与 9 格背包一致，右端对齐
            int rowLeft = leftPos + contentX();
            int rowRight = leftPos + contentRight();

            if (soviet()) {
                g.fill(rowLeft, y, rowRight, y + ENTRY_HEIGHT - 1, SovietComputerScreen.PANEL);
                g.renderOutline(rowLeft, y, rowRight - rowLeft, ENTRY_HEIGHT - 1, SovietComputerScreen.YELLOW_DIM);
            } else {
                g.fill(rowLeft, y, rowRight, y + ENTRY_HEIGHT - 1, BasecoreGuiTheme.PANEL_BOTTOM);
                g.fill(rowLeft, y, rowRight, y + 1, 0xFF6C7A82);
            }

            ItemStack displayStack = entry.getDisplayStack();
            int iconX = rowLeft + 4;
            int iconY = y + 3;
            if (soviet() && displayStack.has(ItemComponentInit.SOVIET_YELLOW_COMPONENT)) {
                // 苏式主题：绘制该物品的黄色着色版材质（原材质名_yellow.png）
                ResourceLocation tex = ResourceLocation.fromNamespaceAndPath(
                        net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(displayStack.getItem()).getNamespace(),
                        "textures/item/"
                                + net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(displayStack.getItem()).getPath()
                                + "_yellow.png");
                g.blit(tex, iconX, iconY, 0, 0, 16, 16, 16, 16);
            } else {
                g.renderItem(displayStack, iconX, iconY);
            }
            g.renderItemDecorations(this.font, displayStack, iconX, iconY);

            int nameColor = soviet() ? SovietComputerScreen.YELLOW : BasecoreGuiTheme.TEXT;
            int subColor = soviet() ? SovietComputerScreen.YELLOW_TEXT_DIM : BasecoreGuiTheme.TEXT_DIM;
            String name = entry.getDisplayName().getString();
            int nameMaxWidth = 70;
            if (font.width(name) > nameMaxWidth) name = font.plainSubstrByWidth(name, nameMaxWidth - 3) + "..";
            g.drawString(font, name, rowLeft + 24, y + 3, nameColor, false);

            String countStr = entry.getCurrentCount() + "/" + (entry.getMaxCount() > 0 ? entry.getMaxCount() : "∞");
            g.drawString(font, countStr, rowLeft + 24, y + 13, subColor, false);

            int btnSize = 14;
            int btnY = y + 5;
            int buyX = rowRight - btnSize - 4;
            int sellX = buyX - btnSize - 4;

            boolean hoverSell = mx >= sellX && mx <= sellX + btnSize && my >= btnY && my <= btnY + btnSize;
            drawSmallButton(g, sellX, btnY, btnSize, "-1", entry.canSell(), hoverSell);

            boolean hoverBuy = mx >= buyX && mx <= buyX + btnSize && my >= btnY && my <= btnY + btnSize;
            drawSmallButton(g, buyX, btnY, btnSize, "+1", entry.canBuy() && canAfford, hoverBuy);

            String costStr = String.valueOf(entry.getPartCost());
            g.drawString(font, costStr, sellX - font.width(costStr) - 4, btnY + 3, subColor, false);
        }

        if (maxScroll > 0) {
            int barTop = startY;
            int barH = ENTRIES_PER_PAGE * ENTRY_HEIGHT;
            int bx = leftPos + contentRight() + 3;
            g.fill(bx, barTop, bx + 2, barTop + barH, soviet() ? SovietComputerScreen.YELLOW_DIM : 0xFF777777);
            int thumbH = Math.max(10, barH * ENTRIES_PER_PAGE / Math.max(1, entries.size()));
            int thumbY = barTop + (barH - thumbH) * scrollOffset / Math.max(1, maxScroll);
            g.fill(bx, thumbY, bx + 2, thumbY + thumbH, soviet() ? SovietComputerScreen.YELLOW : 0xFFCCCCCC);
        }
    }

    /** 小号操作按钮（-1 / +1），按主题着色。 */
    private void drawSmallButton(GuiGraphics g, int x, int y, int size, String label, boolean enabled, boolean hovered) {
        int bg;
        int border;
        int text;
        if (soviet()) {
            bg = enabled ? (hovered ? 0xFF4A3E00 : 0xFF141400) : 0xFF1E1C00;
            border = enabled ? (hovered ? SovietComputerScreen.YELLOW : SovietComputerScreen.YELLOW_DIM)
                    : SovietComputerScreen.YELLOW_DIM;
            text = enabled ? (hovered ? SovietComputerScreen.YELLOW : SovietComputerScreen.YELLOW_TEXT_DIM)
                    : SovietComputerScreen.YELLOW_DIM;
        } else {
            bg = enabled ? (hovered ? BasecoreGuiTheme.BUTTON_HOVER : BasecoreGuiTheme.BUTTON) : 0xFF2A3135;
            border = enabled ? BasecoreGuiTheme.TAB_IDLE : 0xFF556068;
            text = enabled ? BasecoreGuiTheme.TITLE : BasecoreGuiTheme.TEXT_DIM;
        }
        g.fill(x, y, x + size, y + size, bg);
        g.renderOutline(x, y, size, size, border);
        g.drawString(font, label, x + (size - font.width(label)) / 2, y + (size - 8) / 2, text, false);
    }

    private void renderMemberTab(GuiGraphics g, int mx, int my) {
        BaseCoreBlockEntity be = menu.getBaseCore();
        if (be == null) return;

        int idx = 0;
        for (UUID uid : be.getEntityData().getPermittedPlayers()) {
            if (idx >= MEMBER_MAX_ROWS) break;
            int y = topPos + MEMBER_LIST_Y + idx * MEMBER_ROW_H;
            int rowLeft = leftPos + contentX();
            int rowRight = leftPos + contentRight();
            if (soviet()) {
                g.fill(rowLeft, y, rowRight, y + MEMBER_ROW_H - 1, SovietComputerScreen.PANEL);
            } else {
                g.fill(rowLeft, y, rowRight, y + MEMBER_ROW_H - 1, BasecoreGuiTheme.PANEL_BOTTOM);
            }
            idx++;
        }

        // 添加成员输入框框架（与内容区同宽）
        int frameColor = soviet() ? SovietComputerScreen.YELLOW : 0xFF6C7A82;
        g.renderOutline(leftPos + contentX(), topPos + INPUT_FRAME_TOP, INPUT_FRAME_W,
                INPUT_FRAME_BOTTOM - INPUT_FRAME_TOP, frameColor);

        int addX = leftPos + contentRight() - ADD_BTN_W;
        boolean hoverAdd = mx >= addX && mx < addX + ADD_BTN_W
                && my >= topPos + ADD_BTN_Y && my < topPos + ADD_BTN_Y + ADD_BTN_H;
        drawWideButton(g, addX, topPos + ADD_BTN_Y, ADD_BTN_W, ADD_BTN_H, "添加", true, hoverAdd);
    }

    private void drawWideButton(GuiGraphics g, int x, int y, int w, int h, String label, boolean enabled, boolean hovered) {
        if (soviet()) {
            int border = enabled ? (hovered ? SovietComputerScreen.YELLOW : SovietComputerScreen.YELLOW_DIM)
                    : SovietComputerScreen.YELLOW_DIM;
            g.fill(x, y, x + w, y + h, enabled ? (hovered ? 0xFF4A3E00 : SovietComputerScreen.PANEL) : 0xFF1E1C00);
            g.renderOutline(x, y, w, h, border);
            g.drawString(font, label, x + (w - font.width(label)) / 2, y + (h - 8) / 2,
                    enabled ? (hovered ? SovietComputerScreen.YELLOW : SovietComputerScreen.YELLOW_TEXT_DIM)
                            : SovietComputerScreen.YELLOW_DIM, false);
        } else {
            int bg = enabled ? (hovered ? BasecoreGuiTheme.BUTTON_HOVER : BasecoreGuiTheme.BUTTON) : 0xFF2A3135;
            g.fill(x, y, x + w, y + h, bg);
            g.fill(x, y, x + w, y + 1, hovered ? BasecoreGuiTheme.TITLE : BasecoreGuiTheme.TAB_IDLE);
            g.fill(x, y + h - 1, x + w, y + h, 0xFF1E2528);
            g.fill(x, y, x + 1, y + h, 0xFF1E2528);
            g.fill(x + w - 1, y, x + w, y + h, 0xFF1E2528);
            g.drawString(font, label, x + (w - font.width(label)) / 2, y + (h - 8) / 2,
                    enabled ? BasecoreGuiTheme.TEXT : BasecoreGuiTheme.TEXT_DIM, false);
        }
    }

    /** 能源输入槽底衬 + Rs 能源条；防御炮或能源系统关闭时不显示。 */
    private void renderEnergyRow(GuiGraphics g) {
        if (menu.isDefend() || !dev.anye.mc.basecore.config.BasecoreConfig.isEnergyEnabled()) return;

        int sx = leftPos + contentX();
        int sy = topPos + BasecoreUpgradeMenu.ENERGY_SLOT_Y;
        drawEnergySlotBg(g, sx, sy);

        int bx = leftPos + contentX() + BAR_OFFSET_X;
        int by = topPos + BAR_Y;
        int energy = menu.getEnergy();
        int maxEnergy = menu.getMaxEnergy();
        if (soviet()) {
            g.fill(bx, by, bx + BAR_W, by + BAR_H, 0xFF201C00);
            if (energy > 0 && maxEnergy > 0) {
                int filled = Math.max(1, Math.min(BAR_W, (int) ((long) energy * BAR_W / maxEnergy)));
                g.fill(bx, by, bx + filled, by + BAR_H, SovietComputerScreen.YELLOW);
            }
            g.renderOutline(bx, by, BAR_W, BAR_H, SovietComputerScreen.YELLOW_DIM);
        } else {
            BasecoreGuiTheme.energyBar(g, bx, by, BAR_W, BAR_H, energy, maxEnergy);
            g.renderOutline(bx, by, BAR_W, BAR_H, 0xFF6C7A82);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        g.drawString(font, titleText(), 8, 5, titleColor(), false);

        drawTabLabel(g, TAB_UPGRADES_X, "升级管理", currentTab == 0);
        if (hasMemberTab()) {
            drawTabLabel(g, TAB_MEMBERS_X, "成员管理", currentTab == 1);
        }

        if (currentTab == 0) {
            String parts = "零件: " + playerParts;
            g.drawString(font, parts, contentX(), 22,
                    soviet() ? SovietComputerScreen.YELLOW_TEXT_DIM : BasecoreGuiTheme.TEXT_DIM, false);
        } else if (hasMemberTab()) {
            renderMemberLabels(g);
        }

        if (!menu.isDefend() && dev.anye.mc.basecore.config.BasecoreConfig.isEnergyEnabled()) {
            int energy = menu.getEnergy();
            String text = energy <= 0 ? "能源不足"
                    : energy + "/" + menu.getMaxEnergy();
            int color;
            if (soviet()) {
                color = SovietComputerScreen.YELLOW_TEXT_DIM;
            } else {
                color = energy <= 0 ? 0xFFFF5555 : BasecoreGuiTheme.TEXT_DIM;
            }
            g.drawString(font, text, contentX() + BAR_OFFSET_X, STATUS_Y, color, false);
        }

        g.drawString(font, "背包", contentX(), INV_LABEL_Y,
                soviet() ? SovietComputerScreen.YELLOW_TEXT_DIM : BasecoreGuiTheme.TEXT_DIM, false);
    }

    private void renderMemberLabels(GuiGraphics g) {
        BaseCoreBlockEntity be = menu.getBaseCore();
        if (be == null) return;

        String ownerName = be.getEntityData().getOwnerName();
        if (ownerName.isEmpty()) ownerName = "无";
        g.drawString(font, "基地领主: " + ownerName, contentX(), 20,
                soviet() ? SovietComputerScreen.YELLOW : BasecoreGuiTheme.TEXT, false);
        g.drawString(font, "成员 (" + be.getEntityData().getPermittedPlayers().size() + ")",
                contentX(), 34, soviet() ? SovietComputerScreen.YELLOW_TEXT_DIM : BasecoreGuiTheme.TEXT_DIM, false);

        int idx = 0;
        for (UUID uid : be.getEntityData().getPermittedPlayers()) {
            if (idx >= MEMBER_MAX_ROWS) break;
            int y = MEMBER_LIST_Y + idx * MEMBER_ROW_H;
            String name = be.getEntityData().getMemberName(uid);
            g.drawString(font, "● " + name, contentX() + 4, y + 1,
                    soviet() ? SovietComputerScreen.YELLOW : BasecoreGuiTheme.TEXT, false);
            g.drawString(font, "[x]", contentRight() - 18, y + 1,
                    soviet() ? SovietComputerScreen.YELLOW : 0xFFCC6666, false);
            idx++;
        }
    }

    private String titleText() {
        if (menu.isDefend()) return "防御炮";
        return soviet() ? "苏式基地计算机" : "基地核心";
    }

    private int titleColor() {
        return soviet() ? SovietComputerScreen.YELLOW : BasecoreGuiTheme.TITLE;
    }

    private void drawTabLabel(GuiGraphics g, int tabX, String label, boolean active) {
        int color;
        if (soviet()) {
            color = active ? SovietComputerScreen.YELLOW : SovietComputerScreen.YELLOW_TEXT_DIM;
        } else {
            color = active ? BasecoreGuiTheme.TAB_ACTIVE : BasecoreGuiTheme.TAB_IDLE;
        }
        g.drawString(font, label, tabX + (TAB_W - font.width(label)) / 2, TAB_Y + (TAB_H - 8) / 2, color, false);
    }

    /**
     * 原版 EditBox 光标固定灰白，苏式主题下用黄色覆盖绘制同一位置的 1px 竖线。
     */
    private void drawThemedCursor(GuiGraphics g, EditBox box) {
        if (box == null || !box.isFocused() || !soviet()) return;
        String text = box.getValue();
        int cursor = Math.min(box.getCursorPosition(), text.length());
        // 与原版一致：仅当光标右侧有字符或文本已达最大长度时绘制竖线光标；
        // 否则原版绘制下划线 "_"，此时无需覆盖。
        boolean flag2 = cursor < text.length() || text.length() >= memberMaxLength;
        if (!flag2) return;
        int innerWidth = Math.max(1, box.getWidth());
        int displayPos = 0;
        while (displayPos < cursor && font.width(text.substring(displayPos, cursor)) > innerWidth) {
            displayPos++;
        }
        // 与原版插入光标完全重合：drawString(带阴影) 返回 x+宽+1，光标再左移 1 → getX()+宽；
        // 竖线从 y-1 起、高 11。
        int cursorX = box.getX() + font.width(text.substring(displayPos, cursor));
        int top = box.getY() - 1;
        g.fill(cursorX, top, cursorX + 1, top + 11, SovietComputerScreen.YELLOW);
    }

    // ----- 交互 -----

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);

        int lx = (int) (mx - leftPos);
        int ly = (int) (my - topPos);

        if (inTab(lx, ly, TAB_UPGRADES_X)) {
            selectTab(0);
            return true;
        }
        if (hasMemberTab() && inTab(lx, ly, TAB_MEMBERS_X)) {
            selectTab(1);
            return true;
        }

        if (currentTab == 0) {
            int startY = topPos + ENTRIES_Y;
            int btnSize = 14;
            for (int idx = 0; idx < ENTRIES_PER_PAGE; idx++) {
                int ei = scrollOffset + idx;
                if (ei >= entries.size()) break;
                int y = startY + idx * ENTRY_HEIGHT;
                int btnY = y + 5;
                int buyX = leftPos + contentRight() - btnSize - 4;
                int sellX = buyX - btnSize - 4;

                if (mx >= sellX && mx <= sellX + btnSize && my >= btnY && my <= btnY + btnSize) {
                    UpgradeEntry entry = entries.get(ei);
                    if (entry.canSell()) {
                        PacketDistributor.sendToServer(new UpgradeActionPayload(ei, false));
                    }
                    return true;
                }
                if (mx >= buyX && mx <= buyX + btnSize && my >= btnY && my <= btnY + btnSize) {
                    UpgradeEntry entry = entries.get(ei);
                    if (entry.canBuy() && playerParts >= entry.getPartCost()) {
                        PacketDistributor.sendToServer(new UpgradeActionPayload(ei, true));
                        playerParts -= entry.getPartCost();
                    }
                    return true;
                }
            }
        } else if (hasMemberTab()) {
            int addX = contentRight() - ADD_BTN_W;
            if (lx >= addX && lx < addX + ADD_BTN_W
                    && ly >= ADD_BTN_Y && ly < ADD_BTN_Y + ADD_BTN_H) {
                submitMember();
                return true;
            }
            BaseCoreBlockEntity be = menu.getBaseCore();
            if (be != null) {
                int idx = 0;
                for (UUID uid : be.getEntityData().getPermittedPlayers()) {
                    if (idx >= MEMBER_MAX_ROWS) break;
                    int y = MEMBER_LIST_Y + idx * MEMBER_ROW_H;
                    if (lx >= contentRight() - 19 && lx < contentRight() - 1
                            && ly >= y && ly < y + MEMBER_ROW_H - 2) {
                        PacketDistributor.sendToServer(new MemberActionPayload(
                                be.getEntityData().getMemberName(uid), false));
                        return true;
                    }
                    idx++;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    private void submitMember() {
        if (memberInput == null) return;
        String name = memberInput.getValue().trim();
        if (!name.isEmpty()) {
            PacketDistributor.sendToServer(new MemberActionPayload(name, true));
            memberInput.setValue("");
        }
    }

    private static boolean inTab(int lx, int ly, int tabX) {
        return lx >= tabX && lx < tabX + TAB_W && ly >= TAB_Y && ly < TAB_Y + TAB_H;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (currentTab == 0 && maxScroll > 0) {
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) Math.signum(scrollY)));
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            if (hasMemberTab() && currentTab == 1 && memberInput != null && memberInput.isFocused()) {
                return true;
            }
            onClose();
            return true;
        }
        if (hasMemberTab() && currentTab == 1 && memberInput != null && memberInput.isFocused()
                && (keyCode == 257 || keyCode == 335)) {
            submitMember();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        super.render(g, mx, my, partialTick);

        if (currentTab == 1 && hasMemberTab()) {
            drawThemedCursor(g, memberInput);
        }

        if (currentTab == 0) {
            int startY = topPos + ENTRIES_Y;
            for (int idx = 0; idx < ENTRIES_PER_PAGE; idx++) {
                int ei = scrollOffset + idx;
                if (ei >= entries.size()) break;
                int y = startY + idx * ENTRY_HEIGHT;
                int iconX = leftPos + contentX() + 4;
                int iconY = y + 3;
                if (mx >= iconX && mx <= iconX + 18 && my >= iconY && my <= iconY + 18) {
                    UpgradeEntry entry = entries.get(ei);
                    g.renderTooltip(font, entry.getDisplayStack(), mx, my);
                    break;
                }
            }
        }

        if (!menu.isDefend() && dev.anye.mc.basecore.config.BasecoreConfig.isEnergyEnabled()) {
            int barLeft = leftPos + contentX() + BAR_OFFSET_X;
            int barTop = topPos + BAR_Y;
            if (BasecoreGuiTheme.hover(barLeft - 1, barTop - 1, BAR_W + 2, BAR_H + 2, mx, my)) {
                Component energyLine = Component.translatable("gui.basecore.energy", menu.getEnergy(), menu.getMaxEnergy());
                Component hint = Component.translatable("gui.basecore.energy_hint");
                if (soviet()) {
                    energyLine = energyLine.copy().withStyle(net.minecraft.ChatFormatting.GOLD);
                    hint = hint.copy().withStyle(net.minecraft.ChatFormatting.GOLD);
                }
                g.renderComponentTooltip(font, List.of(energyLine, hint), mx, my);
            }
        }
        this.renderTooltip(g, mx, my);
    }
}
