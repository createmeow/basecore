package dev.anye.mc.basecore.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.menu.HashChestMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class HashChestScreen extends AbstractContainerScreen<HashChestMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("basecore:textures/screens/generic_54.png");
    private int x,y;
    private final int textureWidth = 256,textureHeight = 256;
    public HashChestScreen(HashChestMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }
    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
        imageHeight = 240;
        imageWidth = 176;
        leftPos = x = (width - imageWidth)/2;
        topPos = y = (height - imageHeight)/2;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
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