package dev.anye.mc.basecore.basecore;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.anye.mc.basecore.block.entity.basecore.BasecoreBlockEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BasecoreClientHelper {
    private static ConcurrentHashMap<BlockPos, BasecoreBlockEntityData> DATA_MAP = new ConcurrentHashMap<>();
    private static ArrayList<BlockPos> POSS = new ArrayList<>();
    private static Map<BlockPos, DefendHealthData> DEFEND_DATA_MAP = new ConcurrentHashMap<>();
    private static Map<BlockPos, DefendHealthData> HASH_CHEST_HEALTH_MAP = new ConcurrentHashMap<>();

    public static class DefendHealthData {
        public int health;
        public int maxHealth;
        public int showHealthTick;
        public int type; // 1=monster, 2=player, 3=all
    }

    public static void addData(BlockPos blockPos, CompoundTag dat){
        BasecoreBlockEntityData data = DATA_MAP.getOrDefault(blockPos, new BasecoreBlockEntityData());
        data.handle(dat);
        DATA_MAP.put(blockPos, data);
        if (!POSS.contains(blockPos)) POSS.add(blockPos);
    }

    public static void addDefendData(BlockPos blockPos, CompoundTag dat) {
        DefendHealthData data = new DefendHealthData();
        data.health = dat.getInt("health");
        data.maxHealth = dat.getInt("maxHealth");
        data.showHealthTick = dat.getInt("showHealthTick");
        data.type = dat.contains("type") ? dat.getInt("type") : 1;
        // Clear other defends' health bar to prevent overlap when interacting with a new one
        DEFEND_DATA_MAP.forEach((pos, d) -> {
            if (!pos.equals(blockPos)) d.showHealthTick = 0;
        });
        DEFEND_DATA_MAP.put(blockPos, data);
    }

    public static void addHashChestHealthData(BlockPos blockPos, CompoundTag dat) {
        DefendHealthData data = HASH_CHEST_HEALTH_MAP.getOrDefault(blockPos, new DefendHealthData());
        data.health = dat.getInt("health");
        data.maxHealth = dat.getInt("maxHealth");
        data.showHealthTick = dat.getInt("showHealthTick");
        HASH_CHEST_HEALTH_MAP.put(blockPos, data);
    }

    public static void renderBlockEntityHealthBar(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        if (minecraft.screen != null) return;
        Vec3 vec3 = minecraft.player.getEyePosition(0f);
        BlockPos blockPos = getNear(vec3);
        if (blockPos == null) return;
        BasecoreBlockEntityData data = getData(blockPos);
        if (data == null || data.getMaxHealth() <= 0) return;
        RenderSystem.enableDepthTest();
        float health = data.getHealth();
        float maxHealth = data.getMaxHealth();

        float f = (float) minecraft.getWindow().getGuiScaledWidth() / 2f;
        float f1 = (float) minecraft.getWindow().getGuiScaledHeight() / 2f;

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        int width = 200;
        int height = 12;

        net.minecraft.client.gui.Font font = minecraft.font;
        ResourceLocation bgBar = ResourceLocation.tryBuild("basecore", "textures/gui/basecore_health_bar_bg.png");
        ResourceLocation bar = ResourceLocation.tryBuild("basecore", "textures/gui/basecore_health_bar.png");

        guiGraphics.blit(bgBar, (int) f - width / 2, (int) f1 - height - 14, 0, 0, width, height, width, height);
        int barWidth = (int) ((health / maxHealth) * width);
        guiGraphics.blit(bar, (int) f - width / 2, (int) f1 - height - 14, 0, 0, barWidth, height, barWidth, height);

        float scale = 1.5f;
        String healthText = Component.translatable("basecore.health_bar", health, maxHealth).getString();
        String nameText = data.getName();
        String ownerText = data.getOwner() != null ? data.getOwner().toString() : "";
        int textWidth = (int) (font.width(healthText) * scale);
        int textHeight = (int) (font.lineHeight * scale);
        int nameWidth = (int) (font.width(nameText) * scale);
        int ownerWidth = (int) (font.width(ownerText) * scale);
        int centerX = (int) (f / scale);
        int centerTextY = (int) ((f1 - 7 - 6) / scale);

        guiGraphics.drawString(font, nameText, (int) (centerX - nameWidth / 2f), centerTextY - textHeight, 0xffffffff, true);
        guiGraphics.drawString(font, healthText, (int) (centerX - textWidth / 2f), centerTextY, 0xffffffff, true);
        guiGraphics.drawString(font, ownerText, (int) (centerX - ownerWidth / 2f), centerTextY + textHeight, 0xffffffff, true);
    }

    public static BasecoreBlockEntityData getData(BlockPos blockPos) {
        return DATA_MAP.getOrDefault(blockPos, null);
    }

    public static void setData(BlockPos blockPos, BasecoreBlockEntityData data) {
        DATA_MAP.put(blockPos, data);
    }

    public static BlockPos getNear(Vec3 vec3) {
        for (BlockPos p : POSS) {
            BasecoreBlockEntityData data = DATA_MAP.get(p);
            if (data == null) continue;
            int range = data.getRange();
            AABB box = new AABB(
                p.getX() + 0.5 - range,
                p.getY() + 0.5 - range,
                p.getZ() + 0.5 - range,
                p.getX() + 0.5 + range,
                p.getY() + 0.5 + range,
                p.getZ() + 0.5 + range
            );
            if (box.contains(vec3)) return p;
        }
        return null;
    }

    public static Map.Entry<BlockPos, DefendHealthData> getNearDefend(Vec3 vec3) {
        for (Map.Entry<BlockPos, DefendHealthData> entry : DEFEND_DATA_MAP.entrySet()) {
            AABB box = new AABB(entry.getKey()).inflate(20);
            if (box.contains(vec3)) {
                return entry;
            }
        }
        return null;
    }

    public static Map.Entry<BlockPos, DefendHealthData> getNearHashChest(Vec3 vec3) {
        for (Map.Entry<BlockPos, DefendHealthData> entry : HASH_CHEST_HEALTH_MAP.entrySet()) {
            AABB box = new AABB(entry.getKey()).inflate(10);
            if (box.contains(vec3)) {
                return entry;
            }
        }
        return null;
    }

    public static Map<BlockPos, DefendHealthData> getDefendDataMap() {
        return DEFEND_DATA_MAP;
    }

    public static Map<BlockPos, DefendHealthData> getHashChestHealthMap() {
        return HASH_CHEST_HEALTH_MAP;
    }

    public static void clear() {
        DATA_MAP.clear();
        POSS.clear();
        DEFEND_DATA_MAP.clear();
        HASH_CHEST_HEALTH_MAP.clear();
    }

    public static void clearPos() {
        POSS.clear();
    }

    public static void addPos(BlockPos pos) {
        if (!POSS.contains(pos)) POSS.add(pos);
    }

    public static void tick() {
        DATA_MAP.forEach((blockPos, data) -> data.tick());
        DEFEND_DATA_MAP.values().forEach(data -> {
            if (data.showHealthTick > 0) data.showHealthTick--;
        });
        HASH_CHEST_HEALTH_MAP.values().forEach(data -> {
            if (data.showHealthTick > 0) data.showHealthTick--;
        });
        // Clean up stale entries where showHealthTick has expired
        DEFEND_DATA_MAP.entrySet().removeIf(entry -> entry.getValue().showHealthTick <= 0);
        HASH_CHEST_HEALTH_MAP.entrySet().removeIf(entry -> entry.getValue().showHealthTick <= 0);
    }

    public static Map<BlockPos, BasecoreBlockEntityData> getDataMap() {
        return DATA_MAP;
    }

    public static List<BlockPos> getPOSS() {
        return POSS;
    }
}
