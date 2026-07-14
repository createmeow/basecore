package dev.anye.mc.basecore.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.menu.BaseCoreMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Module management screen for modular base core.
 * Only shows the 27 module slots + player inventory.
 * Member management is handled by BaseCoreMemberScreen.
 */
public class BaseCoreScreen extends AbstractContainerScreen<BaseCoreMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("basecore:textures/screens/gui_basecore.png");
    private final int textureWidth = 176, textureHeight = 166;
    private final Inventory playerInv;

    public BaseCoreScreen(BaseCoreMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.playerInv = pPlayerInventory;
        imageHeight = 200;
        imageWidth = 176;
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;

        // Tab buttons - "成员管理" opens the member screen
        addRenderableWidget(Button.builder(
                Component.literal("§6模块管理"),
                b -> { /* already here, no-op */ }
        ).bounds(leftPos + 2, topPos - 10, imageWidth / 2 - 2, 12).build()).active = false;

        addRenderableWidget(Button.builder(
                Component.literal("§6成员管理"),
                b -> switchToMemberTab()
        ).bounds(leftPos + imageWidth / 2, topPos - 10, imageWidth / 2, 12).build());
    }

    private void switchToMemberTab() {
        BasecoreScreenSwitcher.switchTo(minecraft, new BaseCoreMemberScreen(menu, playerInv, title));
    }

    @Override
    protected void renderBg(GuiGraphics g, float v, int mx, int my) {
        // Draw container background
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int texH = Math.min(textureHeight, 166);
        g.blit(TEXTURE, leftPos, topPos, imageWidth, texH, 0, 0, imageWidth, texH, textureWidth, textureHeight);

        // Extended background below the texture
        if (imageHeight > texH) {
            g.fill(leftPos, topPos + texH, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
            g.fill(leftPos + 1, topPos + texH + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, 0xFF333333);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
}
