package dev.anye.mc.basecore.client;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.client.data.ClientPlacementData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class PlacementHudRenderer {
    private static final int BAR_WIDTH = 120;
    private static final int BAR_HEIGHT = 4;
    // Bar color: 00A8F3FF → ARGB = 0xFF00A8F3 → BGR equiv in fill is direct int
    private static final int BAR_FG_COLOR = 0xFF00A8F3;
    private static final int BAR_BG_COLOR = 0x5E000000;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    public static final LayeredDraw.Layer PLACEMENT_PROGRESS_LAYER = (guiGraphics, deltaTracker) -> {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !player.isAlive()) return;
        if (player.isSpectator()) return;

        ClientPlacementData data = ClientPlacementData.INSTANCE;
        if (!data.active) return;

        int remaining = data.remainingTicks;
        String name = data.displayName;

        int totalTicks = 600;
        float progress;
        if (remaining <= 0) {
            progress = 1.0f;
        } else {
            progress = 1.0f - (float) remaining / totalTicks;
        }
        progress = Math.min(1.0f, Math.max(0.0f, progress));

        int cx = guiGraphics.guiWidth() >> 1;
        int x = cx - BAR_WIDTH / 2;
        int y = guiGraphics.guiHeight() - 80;

        // Draw block name
        if (name != null) {
            guiGraphics.drawCenteredString(mc.font, "§7正在放置: §f" + name, cx, y - 14, TEXT_COLOR);
        }

        // Background bar
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, BAR_BG_COLOR);
        // Foreground bar
        int barWidth = (int) (BAR_WIDTH * progress);
        if (barWidth > 0) {
            guiGraphics.fill(x, y, x + barWidth, y + BAR_HEIGHT, BAR_FG_COLOR);
        }

        // Progress text
        int pct = (int) (progress * 100);
        String progressText = pct + "%";
        int textWidth = mc.font.width(progressText);
        guiGraphics.drawString(mc.font, progressText, cx - textWidth / 2, y + 6, TEXT_COLOR);
    };

    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR,
                ResourceLocation.fromNamespaceAndPath(BaseCore.MOD_ID, "placement_progress"),
                PLACEMENT_PROGRESS_LAYER);
    }
}
