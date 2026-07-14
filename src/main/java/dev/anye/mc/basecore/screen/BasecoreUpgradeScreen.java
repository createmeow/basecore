package dev.anye.mc.basecore.screen;

import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.menu.upgrade.BasecoreUpgradeMenu;
import dev.anye.mc.basecore.menu.upgrade.UpgradeEntry;
import dev.anye.mc.basecore.net.MemberActionPayload;
import dev.anye.mc.basecore.net.UpgradeActionPayload;
import dev.anye.mc.basecore.net.UpgradeRefreshPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BasecoreUpgradeScreen extends AbstractContainerScreen<BasecoreUpgradeMenu> {
    private static final int ENTRY_HEIGHT = 24;
    private static final int ENTRIES_PER_PAGE = 8;

    private int currentTab = 0;
    private Button tabLeft, tabRight;
    private final boolean isDefend;
    private final boolean hasMemberTab;

    // Upgrade tab
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private List<UpgradeEntry> entries = new ArrayList<>();
    private int playerParts = 0;

    // Member tab (base core only)
    private EditBox memberInput;
    private Button addMemberBtn;
    private final List<Button> removeButtons = new ArrayList<>();
    private int lastMemberCount = -1;

    public BasecoreUpgradeScreen(BasecoreUpgradeMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 230;
        this.imageHeight = 222;
        this.isDefend = pMenu.isDefend();
        this.hasMemberTab = !pMenu.isDefend();
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;

        // Left tab: always "升级管理"
        tabLeft = addRenderableWidget(Button.builder(
                Component.literal("§6升级管理"),
                b -> selectTab(0)
        ).bounds(leftPos + 2, topPos - 10, imageWidth / 2 - 2, 12).build());

        if (hasMemberTab) {
            // Right tab: only for base core
            tabRight = addRenderableWidget(Button.builder(
                    Component.literal("§6成员管理"),
                    b -> selectTab(1)
            ).bounds(leftPos + imageWidth / 2, topPos - 10, imageWidth / 2, 12).build());

            // Member management: input + add button
            memberInput = new EditBox(font, leftPos + 4, topPos + 22, 130, 14, Component.literal(""));
            memberInput.setMaxLength(16);
            addRenderableWidget(memberInput);

            addMemberBtn = addRenderableWidget(Button.builder(
                    Component.literal("§a添加"),
                    b -> {
                        String name = memberInput.getValue().trim();
                        if (!name.isEmpty()) {
                            PacketDistributor.sendToServer(new MemberActionPayload(name, true));
                            memberInput.setValue("");
                        }
                    }
            ).bounds(leftPos + 138, topPos + 20, 40, 16).build());
        }

        entries = menu.getEntries();
        maxScroll = Math.max(0, entries.size() - ENTRIES_PER_PAGE);

        PacketDistributor.sendToServer(new UpgradeRefreshPayload());
        selectTab(0);
    }

    private void selectTab(int tab) {
        currentTab = tab;
        if (tabLeft != null) tabLeft.active = tab != 0;
        if (tabRight != null) tabRight.active = tab != 1;

        if (hasMemberTab) {
            if (memberInput != null) {
                memberInput.visible = tab == 1;
                memberInput.setFocused(tab == 1);
            }
            if (addMemberBtn != null) addMemberBtn.visible = tab == 1;
            for (Button btn : removeButtons) btn.visible = tab == 1;
            if (tab == 1) rebuildRemoveButtons();
        }
    }

    public void updateEntries(List<UpgradeEntry> newEntries, int syncedParts) {
        this.entries = newEntries;
        this.playerParts = syncedParts;
        maxScroll = Math.max(0, entries.size() - ENTRIES_PER_PAGE);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        if (hasMemberTab) rebuildRemoveButtons();
    }

    private void rebuildRemoveButtons() {
        for (Button btn : removeButtons) removeWidget(btn);
        removeButtons.clear();
        if (!hasMemberTab) return;
        BaseCoreBlockEntity be = menu.getBaseCore();
        if (be == null) return;
        int startY = topPos + 56;
        int idx = 0;
        for (UUID uid : be.getEntityData().getPermittedPlayers()) {
            int y = startY + idx * 14;
            Button removeBtn = Button.builder(
                    Component.literal("§c✕"),
                    b -> PacketDistributor.sendToServer(new MemberActionPayload(be.getEntityData().getMemberName(uid), false))
            ).bounds(leftPos + imageWidth - 24, y, 18, 14).build();
            removeBtn.visible = currentTab == 1;
            addRenderableWidget(removeBtn);
            removeButtons.add(removeBtn);
            idx++;
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float v, int mx, int my) {
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
        g.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, 0xFF333333);

        if (currentTab == 0) {
            renderUpgradeTab(g, mx, my);
        } else if (hasMemberTab) {
            renderMemberTab(g, mx, my);
        }
    }

    private void renderUpgradeTab(GuiGraphics g, int mx, int my) {
        if (!isDefend) {
            g.drawString(font, "§e⛃ " + playerParts, leftPos + 6, topPos + 4, 0xFFAAAAAA, false);
        }

        int startY = topPos + (isDefend ? 4 : 16);
        for (int idx = 0; idx < ENTRIES_PER_PAGE; idx++) {
            int ei = scrollOffset + idx;
            if (ei >= entries.size()) break;
            UpgradeEntry entry = entries.get(ei);
            int y = startY + idx * ENTRY_HEIGHT;
            boolean canAfford = playerParts >= entry.getPartCost();

            g.fill(leftPos + 2, y, leftPos + imageWidth - 2, y + ENTRY_HEIGHT - 1, 0xFF444444);

            g.renderItem(entry.getDisplayStack(), leftPos + 4, y + 3);
            g.renderItemDecorations(font, entry.getDisplayStack(), leftPos + 4, y + 3);

            String name = entry.getDisplayName().getString();
            if (font.width(name) > 80) name = font.plainSubstrByWidth(name, 77) + "..";
            g.drawString(font, name, leftPos + 26, y + 3, 0xFFFFFFFF, false);

            String countStr = entry.getCurrentCount() + "/" + (entry.getMaxCount() > 0 ? entry.getMaxCount() : "∞");
            g.drawString(font, "§7" + countStr, leftPos + 26, y + 13, 0xFF888888, false);

            int btnSize = 14;
            int btnY = y + 5;
            int sellX = leftPos + imageWidth - 52;
            int buyX = leftPos + imageWidth - 28;

            boolean hoverSell = mx >= sellX && mx <= sellX + btnSize && my >= btnY && my <= btnY + btnSize;
            g.fill(sellX, btnY, sellX + btnSize, btnY + btnSize,
                    entry.canSell() ? (hoverSell ? 0xFFCC4444 : 0xFF993333) : 0xFF555555);
            g.drawCenteredString(font, Component.literal("-1"), sellX + btnSize / 2, btnY + 2,
                    entry.canSell() ? 0xFFFFFFFF : 0xFF888888);

            boolean hoverBuy = mx >= buyX && mx <= buyX + btnSize && my >= btnY && my <= btnY + btnSize;
            boolean canBuy = entry.canBuy() && canAfford;
            g.fill(buyX, btnY, buyX + btnSize, btnY + btnSize,
                    canBuy ? (hoverBuy ? 0xFF44CC44 : 0xFF339933) : 0xFF555555);
            g.drawCenteredString(font, Component.literal("+1"), buyX + btnSize / 2, btnY + 2,
                    canBuy ? 0xFFFFFFFF : 0xFF888888);

            String costStr = "§7" + entry.getPartCost();
            g.drawString(font, Component.literal(costStr), sellX - font.width(costStr) - 2, btnY + 2, 0xFFAAAAAA, false);
        }

        if (maxScroll > 0) {
            int barTop = startY;
            int barH = ENTRIES_PER_PAGE * ENTRY_HEIGHT;
            int bx = leftPos + imageWidth - 3;
            g.fill(bx, barTop, bx + 2, barTop + barH, 0xFF777777);
            int thumbH = Math.max(10, barH * ENTRIES_PER_PAGE / Math.max(1, entries.size()));
            int thumbY = barTop + (barH - thumbH) * scrollOffset / Math.max(1, maxScroll);
            g.fill(bx, thumbY, bx + 2, thumbY + thumbH, 0xFFCCCCCC);
        }
    }

    private void renderMemberTab(GuiGraphics g, int mx, int my) {
        BaseCoreBlockEntity be = menu.getBaseCore();
        if (be == null) return;

        // Refresh remove buttons if member list changed
        int count = be.getEntityData().getPermittedPlayers().size();
        if (count != lastMemberCount) {
            lastMemberCount = count;
            rebuildRemoveButtons();
        }

        String ownerName = be.getEntityData().getOwnerName();
        if (ownerName.isEmpty()) ownerName = "无";
        g.drawString(font, "§6基地领主: §e" + ownerName, leftPos + 6, topPos + 4, 0xFFFFFFFF, false);

        g.drawString(font, "§6成员 (" + be.getEntityData().getPermittedPlayers().size() + "):", leftPos + 6, topPos + 40, 0xFFFFFFFF, false);

        int startY = topPos + 56;
        int idx = 0;
        for (UUID uid : be.getEntityData().getPermittedPlayers()) {
            int y = startY + idx * 14;
            String name = be.getEntityData().getMemberName(uid);
            g.fill(leftPos + 4, y, leftPos + imageWidth - 28, y + 14, 0xFF444444);
            g.drawString(font, "§7● §f" + name, leftPos + 8, y + 2, 0xFFFFFFFF, false);
            idx++;
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);

        if (currentTab == 0) {
            int startY = topPos + (isDefend ? 4 : 16);
            int btnSize = 14;

            for (int idx = 0; idx < ENTRIES_PER_PAGE; idx++) {
                int ei = scrollOffset + idx;
                if (ei >= entries.size()) break;
                int y = startY + idx * ENTRY_HEIGHT;
                int btnY = y + 5;

                int sellX = leftPos + imageWidth - 52;
                int buyX = leftPos + imageWidth - 28;

                if (mx >= sellX && mx <= sellX + btnSize && my >= btnY && my <= btnY + btnSize) {
                    UpgradeEntry entry = entries.get(ei);
                    if (entry.canSell()) {
                        PacketDistributor.sendToServer(new UpgradeActionPayload(ei, false));
                    }
                    return true;
                }
                if (mx >= buyX && mx <= buyX + btnSize && my >= btnY && my <= btnY + btnSize) {
                    UpgradeEntry entry = entries.get(ei);
                    if (entry.canBuy()) {
                        PacketDistributor.sendToServer(new UpgradeActionPayload(ei, true));
                        if (entry.getPartCost() <= playerParts) {
                            playerParts -= entry.getPartCost();
                        }
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (currentTab == 0 && maxScroll > 0) {
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) Math.signum(scrollY)));
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            if (hasMemberTab && currentTab == 1 && memberInput != null && memberInput.isFocused()) {
                return true;
            }
            onClose();
            return true;
        }
        if (hasMemberTab && currentTab == 1 && memberInput != null && memberInput.isFocused()
                && (keyCode == 257 || keyCode == 335)) {
            String name = memberInput.getValue().trim();
            if (!name.isEmpty()) {
                PacketDistributor.sendToServer(new MemberActionPayload(name, true));
                memberInput.setValue("");
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        super.render(g, mx, my, partialTick);
        // Module tooltip on hover
        if (currentTab == 0) {
            int startY = topPos + (isDefend ? 4 : 16);
            for (int idx = 0; idx < ENTRIES_PER_PAGE; idx++) {
                int ei = scrollOffset + idx;
                if (ei >= entries.size()) break;
                int y = startY + idx * ENTRY_HEIGHT;
                int iconX = leftPos + 4;
                int iconY = y + 3;
                if (mx >= iconX && mx <= iconX + 18 && my >= iconY && my <= iconY + 18) {
                    UpgradeEntry entry = entries.get(ei);
                    g.renderTooltip(font, entry.getDisplayStack(), mx, my);
                    break;
                }
            }
        }
        this.renderTooltip(g, mx, my);
    }
}
