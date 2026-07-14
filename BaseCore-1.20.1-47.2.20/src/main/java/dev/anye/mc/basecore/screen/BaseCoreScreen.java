package dev.anye.mc.basecore.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.menu.BaseCoreMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BaseCoreScreen extends AbstractContainerScreen<BaseCoreMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(BaseCore.MOD_ID,"textures/screens/gui_basecore.png");
    private int x,y;
    private final int textureWidth = 176,textureHeight = 166;
    public BaseCoreScreen(BaseCoreMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }
    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
        imageHeight = 166;
        imageWidth = 176;
        leftPos = x = (width - imageWidth)/2;
        topPos = y = (height - imageHeight)/2;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F,1.0F,1.0F,1.0F);
        RenderSystem.setShaderTexture(0,TEXTURE);
        guiGraphics.blit(TEXTURE,x,y,imageWidth,imageHeight,0,0,imageWidth,imageHeight,textureWidth,textureHeight);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics,pMouseX,pMouseY);
    }
}
