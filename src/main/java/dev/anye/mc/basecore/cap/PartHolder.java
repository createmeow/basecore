package dev.anye.mc.basecore.cap;

import net.minecraft.world.entity.player.Player;

/**
 * Manages the player's stored parts count using persistent NBT data.
 * Using persistentData instead of DataAttachment for reliable save/load across sessions.
 */
public class PartHolder {
    private static final String PARTS_KEY = "basecore.parts";

    public static int getValue(Player player) {
        return player.getPersistentData().getInt(PARTS_KEY);
    }

    public static void setValue(Player player, int value) {
        player.getPersistentData().putInt(PARTS_KEY, Math.max(0, value));
    }

    public static void modify(Player player, int delta) {
        int current = getValue(player);
        setValue(player, current + delta);
    }

    public static boolean deduct(Player player, int amount) {
        int current = getValue(player);
        if (current < amount) return false;
        setValue(player, current - amount);
        return true;
    }
}
