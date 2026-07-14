package dev.anye.mc.basecore.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.anye.mc.basecore.menu.DefendMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DefendScreen extends AbstractContainerScreen<DefendMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("basecore:textures/screens/gui_basecore.png");
    private final int textureWidth = 176, textureHeight = 166;

    public DefendScreen(DefendMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    protected void renderBg(GuiGraphics g, float v, int mx, int my) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        g.blit(TEXTURE, i, j, imageWidth, textureHeight, 0, 0, imageWidth, textureHeight, textureWidth, textureHeight);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
}
