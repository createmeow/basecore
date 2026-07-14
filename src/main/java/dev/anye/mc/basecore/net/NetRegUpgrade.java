package dev.anye.mc.basecore.net;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.net.easy_net.EasyNet;
import dev.anye.mc.basecore.net.easy_net.EasyNetRegister;
import net.minecraft.nbt.CompoundTag;

/**
 * Network handlers for the upgrade menu.
 * S2C: Send updated entry list to the client
 * Note: C2S uses dedicated UpgradeActionPayload for player context.
 */
public class NetRegUpgrade {
    public static final String UPGRADE_MENU_KEY = BaseCore.MOD_ID + ":upgrade_menu";
    public static final String UPGRADE_ACTION_KEY = BaseCore.MOD_ID + ":upgrade_action";

    public static void registerNets() {
        // S2C: Sync upgrade entries to client
        EasyNetRegister.registerNet(UPGRADE_MENU_KEY, new EasyNet() {
            @Override
            public void client(CompoundTag dat) {
                // Deprecated - use UpgradeMenuS2CPayload instead
            }

            @Override
            public void server(CompoundTag dat) {
                // Deprecated - use UpgradeActionPayload instead
            }
        });
    }
}
