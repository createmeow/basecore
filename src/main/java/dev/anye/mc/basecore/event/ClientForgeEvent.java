package dev.anye.mc.basecore.event;

import com.mojang.logging.LogUtils;
import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.basecore.BasecoreClientHelper;
import dev.anye.mc.basecore.block.entity.basecore.BasecoreBlockEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import org.slf4j.Logger;

@EventBusSubscriber(modid = BaseCore.MOD_ID, value = Dist.CLIENT)
public class ClientForgeEvent {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int BAR_BG_COLOR = 0xFF404040;
    private static final int BAR_FG_COLOR = 0xFFAA00AA;
    private static final int DEFEND_BAR_FG_COLOR = 0xFF00AA00;
    private static final int DEFEND_BAR_BG_COLOR = 0xFF305030;
    private static final int HASH_CHEST_BAR_FG_COLOR = 0xFFAAAA00;
    private static final int HASH_CHEST_BAR_BG_COLOR = 0xFF505030;

    @SubscribeEvent
    public static void onRender(RenderGuiLayerEvent.Pre event){
        renderBasecoreBar(event);
        renderDefendBar(event);
        renderHashChestBar(event);
    }

    private static void renderBasecoreBar(RenderGuiLayerEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        BlockPos blockPos = BasecoreClientHelper.getNear(minecraft.player.position());
        if (blockPos == null) return;
        BasecoreBlockEntityData basecoreBlockEntityData = BasecoreClientHelper.getData(blockPos);
        if (basecoreBlockEntityData == null) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int w = 182;
        int y = 6;
        int cx = guiGraphics.guiWidth() >> 1;
        int x = cx - 91;

        int ws = w * basecoreBlockEntityData.getHealth() / basecoreBlockEntityData.getMaxHealth();
        String name = basecoreBlockEntityData.getName();
        if (name.isEmpty()) name = "No Name";
        guiGraphics.drawCenteredString(minecraft.font, name, cx, y, 0xff00ff00);
        y += minecraft.font.lineHeight;

        guiGraphics.fill(x, y, x + w, y + 3, BAR_BG_COLOR);
        guiGraphics.fill(x, y, x + ws, y + 3, 0xFFCC0000);

        y += 4;

        guiGraphics.drawString(minecraft.font, basecoreBlockEntityData.getHealth() + "/" + basecoreBlockEntityData.getMaxHealth(), x, y, 0xffffff00);

        y += minecraft.font.lineHeight;

        if (basecoreBlockEntityData.getInterferenceTime() > 0) {
            guiGraphics.drawString(minecraft.font, Component.translatable("gui.basecore.basecore.interference", basecoreBlockEntityData.getInterferenceTime() / 20), x, y, 0xffffff00);
        }
    }

    private static void renderDefendBar(RenderGuiLayerEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        var defendEntry = BasecoreClientHelper.getNearDefend(minecraft.player.position());
        if (defendEntry == null) return;
        var data = defendEntry.getValue();
        if (data.showHealthTick <= 0 || data.maxHealth <= 0) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int w = 182;
        int y = guiGraphics.guiHeight() - 80;
        int cx = guiGraphics.guiWidth() >> 1;
        int x = cx - 91;

        int ws = w * data.health / data.maxHealth;
        String name = Component.translatable("block_entity.basecore.defend").getString();

        guiGraphics.drawCenteredString(minecraft.font, name, cx, y - 3, 0xff00ff00);

        guiGraphics.fill(x, y + 6, x + w, y + 9, DEFEND_BAR_BG_COLOR);
        guiGraphics.fill(x, y + 6, x + ws, y + 9, DEFEND_BAR_FG_COLOR);

        guiGraphics.drawString(minecraft.font, data.health + "/" + data.maxHealth, x, y + 13, 0xffffff00);
    }

    private static void renderHashChestBar(RenderGuiLayerEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        var hashChestEntry = BasecoreClientHelper.getNearHashChest(minecraft.player.position());
        if (hashChestEntry == null) return;
        var data = hashChestEntry.getValue();
        if (data.showHealthTick <= 0 || data.maxHealth <= 0) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int w = 182;
        int y = guiGraphics.guiHeight() - 60;
        int cx = guiGraphics.guiWidth() >> 1;
        int x = cx - 91;

        int ws = w * data.health / data.maxHealth;
        String name = Component.translatable("block_entity.basecore.hash_chest").getString();

        guiGraphics.drawCenteredString(minecraft.font, name, cx, y - 3, 0xff00ff00);

        guiGraphics.fill(x, y + 6, x + w, y + 9, HASH_CHEST_BAR_BG_COLOR);
        guiGraphics.fill(x, y + 6, x + ws, y + 9, HASH_CHEST_BAR_FG_COLOR);

        guiGraphics.drawString(minecraft.font, data.health + "/" + data.maxHealth, x, y + 13, 0xffffff00);
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event){
        BasecoreClientHelper.tick();
    }
}