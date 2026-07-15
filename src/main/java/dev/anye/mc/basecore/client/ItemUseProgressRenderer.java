package dev.anye.mc.basecore.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.anye.mc.basecore.BaseCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class ItemUseProgressRenderer {
    private static final int BAR_WIDTH = 120;
    private static final int BAR_HEIGHT = 3;
    private static final int BAR_BG_COLOR = 0xFF404040;
    private static final int BAR_FG_COLOR = 0xFF00AA00;

    public static final LayeredDraw.Layer USE_PROGRESS_LAYER = (guiGraphics, deltaTracker) -> {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !player.isAlive()) return;
        if (player.isSpectator()) return;

        ItemStack useItem = player.getUseItem();
        if (useItem.isEmpty()) return;

        String itemId = useItem.getItem().builtInRegistryHolder().key().location().toString();
        if (!itemId.startsWith(BaseCore.MOD_ID + ":")) return;

        int useDuration = useItem.getUseDuration(player);
        int remainingTicks = player.getUseItemRemainingTicks();
        if (useDuration <= 0 || remainingTicks <= 0) return;

        int usedTicks = useDuration - remainingTicks;
        float progress = (float) usedTicks / useDuration;

        RenderSystem.enableBlend();

        int cx = guiGraphics.guiWidth() >> 1;
        int x = cx - BAR_WIDTH / 2;
        int y = guiGraphics.guiHeight() - 80;

        String name = useItem.getHoverName().getString();
        guiGraphics.drawCenteredString(mc.font, name, cx, y - 12, 0xffffff00);

        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, BAR_BG_COLOR);
        int barWidth = (int) (BAR_WIDTH * progress);
        guiGraphics.fill(x, y, x + barWidth, y + BAR_HEIGHT, BAR_FG_COLOR);

        String progressText = (int) (progress * 100) + "%";
        int textWidth = mc.font.width(progressText);
        guiGraphics.drawString(mc.font, progressText, cx - textWidth / 2, y + 5, 0xffffff00);

        RenderSystem.disableBlend();
    };

    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR,
                ResourceLocation.fromNamespaceAndPath(BaseCore.MOD_ID, "item_use_progress"),
                USE_PROGRESS_LAYER);
    }
}
