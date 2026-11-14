package dev.anye.mc.basecore.screen;

import dev.anye.mc.basecore.menu.SovietComputerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 苏式基地计算机主界面（模块页）：黑底黄框。
 * 顶部为标题与「模块管理 / 成员管理」页签，中部 9x3 槽位面板，下方血量与状态，底部玩家背包面板。
 */
public class SovietComputerScreen extends AbstractContainerScreen<SovietComputerMenu> {
    static final int YELLOW = 0xFFFFCC00;
    static final int YELLOW_DIM = 0xFF8A6E00;
    static final int YELLOW_TEXT_DIM = 0xFFB89B00;
    static final int BLACK = 0xFF000000;
    static final int PANEL = 0xFF141400;

    public static final int WINDOW_WIDTH = 196;
    public static final int WINDOW_HEIGHT = 186;

    static final int TAB_Y = 3;
    static final int TAB_W = 46;
    static final int TAB_H = 14;
    static final int TAB_MODULES_X = 98;
    static final int TAB_MEMBERS_X = 146;

    static final int PANEL_LEFT = 4;
    static final int PANEL_RIGHT = WINDOW_WIDTH - 4;
    static final int STATUS_Y = 86;
    /** 能源条位置：模块区（到 79）与玩家背包（106 起）之间。 */
    static final int BAR_X = 17;
    static final int BAR_Y = 96;
    static final int BAR_W = 116;
    static final int BAR_H = 6;
    /** 槽位悬停高亮色（黄色半透明）。 */
    private static final int SLOT_HIGHLIGHT = 0x66FFCC00;

    private final Inventory playerInv;

    public SovietComputerScreen(SovietComputerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.playerInv = inventory;
        this.imageWidth = WINDOW_WIDTH;
        this.imageHeight = WINDOW_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, BLACK);

        drawTab(graphics, x + TAB_MODULES_X, y + TAB_Y, true);
        drawTab(graphics, x + TAB_MEMBERS_X, y + TAB_Y, false);

        // 能源条（黑底黄条）；配置关闭能源系统时隐藏
        if (dev.anye.mc.basecore.config.BasecoreConfig.isEnergyEnabled()) {
            graphics.fill(x + BAR_X, y + BAR_Y, x + BAR_X + BAR_W, y + BAR_Y + BAR_H, 0xFF201C00);
            int energy = this.menu.getEnergy();
            int maxEnergy = this.menu.getMaxEnergy();
            if (energy > 0 && maxEnergy > 0) {
                int filled = Math.max(1, Math.min(BAR_W, (int) ((long) energy * BAR_W / maxEnergy)));
                graphics.fill(x + BAR_X, y + BAR_Y, x + BAR_X + filled, y + BAR_Y + BAR_H, YELLOW);
            }
            graphics.renderOutline(x + BAR_X, y + BAR_Y, BAR_W, BAR_H, YELLOW_DIM);
        }
    }

    /** 槽位不再画边框，只保留悬停时的黄色高亮。 */
    @Override
    protected void renderSlotHighlight(GuiGraphics graphics, net.minecraft.world.inventory.Slot slot,
                                       int mouseX, int mouseY, float partialTick) {
        graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, SLOT_HIGHLIGHT);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, "苏式基地计算机", 6, 6, YELLOW, false);
        drawTabLabel(graphics, TAB_MODULES_X, "模块管理", true);
        drawTabLabel(graphics, TAB_MEMBERS_X, "成员管理", false);

        boolean broken = this.menu.isBroken();
        boolean energyEnabled = dev.anye.mc.basecore.config.BasecoreConfig.isEnergyEnabled();
        boolean noEnergy = energyEnabled && this.menu.getEnergy() <= 0;
        String line = this.menu.getHealth() + "/" + this.menu.getMaxHealth() + "  "
                + (broken ? "[WARN] 故障 需电子管修复" : (noEnergy ? "[WARN] 能源不足" : "正常工作"));
        graphics.drawString(this.font, line, 6, STATUS_Y, YELLOW, false);

        if (energyEnabled) {
            graphics.drawString(this.font, this.menu.getEnergy() + "/" + this.menu.getMaxEnergy(),
                    BAR_X + BAR_W + 4, BAR_Y - 1, YELLOW_TEXT_DIM, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (dev.anye.mc.basecore.config.BasecoreConfig.isEnergyEnabled()
                && mouseX >= leftPos + BAR_X && mouseX < leftPos + BAR_X + BAR_W
                && mouseY >= topPos + BAR_Y && mouseY < topPos + BAR_Y + BAR_H) {
            graphics.renderComponentTooltip(this.font, java.util.List.of(
                            Component.translatable("gui.basecore.energy", this.menu.getEnergy(), this.menu.getMaxEnergy())
                                    .withStyle(net.minecraft.ChatFormatting.YELLOW),
                            Component.translatable("gui.basecore.energy_hint")
                                    .withStyle(net.minecraft.ChatFormatting.YELLOW)),
                    mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int lx = (int) (mouseX - leftPos);
            int ly = (int) (mouseY - topPos);
            if (isInTab(lx, ly, TAB_MODULES_X)) {
                return true;
            }
            if (isInTab(lx, ly, TAB_MEMBERS_X)) {
                BasecoreScreenSwitcher.switchTo(this.minecraft,
                        new SovietComputerMemberScreen(this.menu, this.playerInv, this.title));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // ----- 绘制工具（成员界面复用） -----

    static void drawPanel(GuiGraphics graphics, int x1, int y1, int x2, int y2) {
        graphics.fill(x1, y1, x2, y2, BLACK);
        graphics.fill(x1, y1, x2, y1 + 2, YELLOW_DIM);
        graphics.fill(x1, y2 - 2, x2, y2, YELLOW_DIM);
        graphics.fill(x1, y1, x1 + 2, y2, YELLOW_DIM);
        graphics.fill(x2 - 2, y1, x2, y2, YELLOW_DIM);
    }

    static void drawTab(GuiGraphics graphics, int x, int y, boolean active) {
        int color = active ? YELLOW : YELLOW_DIM;
        graphics.fill(x, y, x + TAB_W, y + TAB_H, BLACK);
        graphics.fill(x, y, x + TAB_W, y + 1, color);
        graphics.fill(x, y + TAB_H - 1, x + TAB_W, y + TAB_H, color);
        graphics.fill(x, y, x + 1, y + TAB_H, color);
        graphics.fill(x + TAB_W - 1, y, x + TAB_W, y + TAB_H, color);
    }

    private void drawTabLabel(GuiGraphics graphics, int tabX, String label, boolean active) {
        int color = active ? YELLOW : YELLOW_TEXT_DIM;
        int textX = tabX + (TAB_W - this.font.width(label)) / 2;
        graphics.drawString(this.font, label, textX, TAB_Y + (TAB_H - 8) / 2, color, false);
    }

    static boolean isInTab(int lx, int ly, int tabX) {
        return lx >= tabX && lx < tabX + TAB_W && ly >= TAB_Y && ly < TAB_Y + TAB_H;
    }
}
