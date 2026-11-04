package dev.anye.mc.basecore.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 普通基地核心统一 GUI 风格（TimeXrebirth / terrainbuilder 风格）：
 * 面板颜色取自基地核心方块贴图 {@code basecore:block/basecore} 的真实平均色
 * #4B575C，上亮下暗渐变的中点即为该平均色；槽位只用高亮填充、不画边框。
 */
public final class BasecoreGuiTheme {
    /** 机器材质亮面（平均色提亮约 15%）。 */
    public static final int PANEL_TOP = 0xFF566268;
    /** 机器材质暗面（平均色压暗约 20%），与亮面渐变中点约等于平均色 #4B575C。 */
    public static final int PANEL_BOTTOM = 0xFF3E484D;
    /** 按钮底色。 */
    public static final int BUTTON = 0xFF2F393E;
    /** 按钮悬停底色。 */
    public static final int BUTTON_HOVER = 0xFF47545A;
    /** 标题白。 */
    public static final int TITLE = 0xFFF2F6F8;
    /** 正文。 */
    public static final int TEXT = 0xFFDDE4E8;
    /** 暗文本。 */
    public static final int TEXT_DIM = 0xFFA9B3BA;
    /** 槽位底衬。 */
    public static final int SLOT = 0x33FFFFFF;
    /** 槽位悬停高亮。 */
    public static final int SLOT_HOVER = 0xB0FFFFFF;
    /** 计/仪表底。 */
    public static final int GAUGE_BG = 0xFF1C1C1E;
    /** 能源条填充色（偏冷的浅蓝，与蓝灰机身协调）。 */
    public static final int GAUGE_FILL = 0xFF63C4EA;
    /** 当前页签。 */
    public static final int TAB_ACTIVE = 0xFFEFF5F9;
    /** 非当前页签。 */
    public static final int TAB_IDLE = 0xFF8D98A0;

    private BasecoreGuiTheme() {
    }

    /** 机器主色调面板：上亮下暗渐变 + 单像素提亮边框。 */
    public static void panel(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fillGradient(x, y, x + w, y + h, PANEL_TOP, PANEL_BOTTOM);
        graphics.renderOutline(x, y, w, h, outline());
    }

    private static int outline() {
        int r = Math.min(0xFF, ((PANEL_TOP >> 16) & 0xFF) + 0x20);
        int g = Math.min(0xFF, ((PANEL_TOP >> 8) & 0xFF) + 0x20);
        int b = Math.min(0xFF, (PANEL_TOP & 0xFF) + 0x20);
        return 0xBF000000 | (r << 16) | (g << 8) | b;
    }

    /** 物品槽位底衬（16×16 填充，无框线）。 */
    public static void slot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 16, y + 16, SLOT);
    }

    /** 页签：黑底 + 顶部/左右 1px 描边，当前页签用亮色。 */
    public static void tab(GuiGraphics graphics, int x, int y, int w, int h, boolean active) {
        int color = active ? TAB_ACTIVE : TAB_IDLE;
        graphics.fill(x, y, x + w, y + h, 0xFF1A1F22);
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }

    /** 能源条：深色底 + 按比例的填充。 */
    public static void energyBar(GuiGraphics graphics, int x, int y, int w, int h, int value, int max) {
        graphics.fill(x, y, x + w, y + h, GAUGE_BG);
        if (max <= 0 || value <= 0) return;
        int filled = Math.max(1, Math.min(w, (int) ((long) value * w / max)));
        graphics.fill(x, y, x + filled, y + h, GAUGE_FILL);
        // 顶部 1px 高光，让填充看起来有金属感
        graphics.fill(x, y, x + filled, y + 1, 0x66FFFFFF);
    }

    /** 输入框底衬：深色填充 + 底部一条亮线，替代原版按钮式边框。 */
    public static void input(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, 0xFF262E32);
        graphics.fill(x, y + h - 1, x + w, y + h, 0xFF6C7A82);
    }

    /** 主题风格按钮：深色底 + 悬停提亮 + 顶/左/右描边，文字居中。 */
    public static void button(GuiGraphics graphics, Font font, int x, int y, int w, int h,
                              String label, boolean hovered) {
        graphics.fill(x, y, x + w, y + h, hovered ? BUTTON_HOVER : BUTTON);
        graphics.fill(x, y, x + w, y + 1, hovered ? TITLE : TAB_IDLE);
        graphics.fill(x, y + h - 1, x + w, y + h, 0xFF1E2528);
        graphics.fill(x, y, x + 1, y + h, 0xFF1E2528);
        graphics.fill(x + w - 1, y, x + w, y + h, 0xFF1E2528);
        graphics.drawString(font, label, x + (w - font.width(label)) / 2, y + (h - 8) / 2,
                hovered ? TITLE : TEXT, false);
    }

    public static boolean hover(int x, int y, int w, int h, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}
