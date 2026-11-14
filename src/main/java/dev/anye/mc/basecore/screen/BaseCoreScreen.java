package dev.anye.mc.basecore.screen;

import dev.anye.mc.basecore.menu.BaseCoreMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * 普通基地核心主界面（模块页）。
 * 视觉风格与 TimeXrebirth 一致：按机器材质平均色绘制渐变面板、槽位只用高亮填充，
 * 顶部为标题与「模块管理 / 成员管理」页签，模块区与玩家背包之间是能源条（Rs）。
 */
public class BaseCoreScreen extends AbstractContainerScreen<BaseCoreMenu> {
    private static final int W = 176;
    private static final int H = 166;

    private static final int TAB_Y = 4;
    private static final int TAB_W = 46;
    private static final int TAB_H = 14;
    private static final int TAB_MODULES_X = W - 96;
    private static final int TAB_MEMBERS_X = W - 50;

    /** 能源条位置（模块区 20..72，玩家背包 84 起，条放在两者之间）。 */
    private static final int BAR_X = 8;
    private static final int BAR_Y = 76;
    private static final int BAR_W = 118;
    private static final int BAR_H = 6;

    /** 模块区与玩家背包的槽位起点（与 BaseCoreMenu 保持一致）。 */
    private static final int MACHINE_X = 7;
    private static final int MACHINE_Y = 20;
    private static final int PLAYER_X = 8;
    private static final int PLAYER_Y = 84;
    private static final int HOTBAR_Y = 142;

    private final Inventory playerInv;

    public BaseCoreScreen(BaseCoreMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
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
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mx, int my) {
        int x = leftPos;
        int y = topPos;

        BasecoreGuiTheme.panel(g, x, y, W, H);
        BasecoreGuiTheme.tab(g, x + TAB_MODULES_X, y + TAB_Y, TAB_W, TAB_H, true);
        BasecoreGuiTheme.tab(g, x + TAB_MEMBERS_X, y + TAB_Y, TAB_W, TAB_H, false);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                BasecoreGuiTheme.slot(g, x + MACHINE_X + col * 18, y + MACHINE_Y + row * 18);
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                BasecoreGuiTheme.slot(g, x + PLAYER_X + col * 18, y + PLAYER_Y + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            BasecoreGuiTheme.slot(g, x + PLAYER_X + col * 18, y + HOTBAR_Y);
        }

        if (dev.anye.mc.basecore.config.BasecoreConfig.isEnergyEnabled()) {
            BasecoreGuiTheme.energyBar(g, x + BAR_X, y + BAR_Y, BAR_W, BAR_H,
                    menu.getEnergy(), menu.getMaxEnergy());
        }
    }

    @Override
    protected void renderSlotHighlight(GuiGraphics g, Slot slot, int mouseX, int mouseY, float partialTick) {
        g.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, BasecoreGuiTheme.SLOT_HOVER);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, this.title, 8, 5, BasecoreGuiTheme.TITLE, false);

        int labelY = TAB_Y + (TAB_H - 8) / 2;
        g.drawString(this.font, "模块管理",
                TAB_MODULES_X + (TAB_W - this.font.width("模块管理")) / 2, labelY,
                BasecoreGuiTheme.TAB_ACTIVE, false);
        g.drawString(this.font, "成员管理",
                TAB_MEMBERS_X + (TAB_W - this.font.width("成员管理")) / 2, labelY,
                BasecoreGuiTheme.TAB_IDLE, false);

        if (dev.anye.mc.basecore.config.BasecoreConfig.isEnergyEnabled()) {
            if (menu.getEnergy() <= 0) {
                g.drawString(this.font, "能源不足", BAR_X + BAR_W + 4, BAR_Y - 1, 0xFFFF5555, false);
            } else {
                g.drawString(this.font, menu.getEnergy() + "/" + menu.getMaxEnergy(),
                        BAR_X + BAR_W + 4, BAR_Y - 1, BasecoreGuiTheme.TEXT_DIM, false);
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        int barLeft = leftPos + BAR_X;
        int barTop = topPos + BAR_Y;
        if (dev.anye.mc.basecore.config.BasecoreConfig.isEnergyEnabled()
                && BasecoreGuiTheme.hover(barLeft - 1, barTop - 1, BAR_W + 2, BAR_H + 2, mouseX, mouseY)) {
            g.renderComponentTooltip(this.font, java.util.List.of(
                            Component.translatable("gui.basecore.energy", menu.getEnergy(), menu.getMaxEnergy()),
                            Component.translatable("gui.basecore.energy_hint")),
                    mouseX, mouseY);
        }
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int lx = (int) (mouseX - leftPos);
            int ly = (int) (mouseY - topPos);
            if (inTab(lx, ly, TAB_MODULES_X)) return true;
            if (inTab(lx, ly, TAB_MEMBERS_X)) {
                switchToMemberTab();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static boolean inTab(int lx, int ly, int tabX) {
        return lx >= tabX && lx < tabX + TAB_W && ly >= TAB_Y && ly < TAB_Y + TAB_H;
    }

    private void switchToMemberTab() {
        BasecoreScreenSwitcher.switchTo(minecraft, new BaseCoreMemberScreen(menu, playerInv, title));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
