package dev.anye.mc.basecore.net;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.net.easy_net.EasyNetRegister;

public class NetReg {
    public static final String BASECORE_BLOCK_KEY = BaseCore.MOD_ID + ":basecore_block";
    public static final String ACTIVITY_BASECORE_KEY = BaseCore.MOD_ID + ":activity_basecore";
    public static final String DEFEND_BLOCK_KEY = BaseCore.MOD_ID + ":defend_block";
    public static final String HASH_CHEST_HEALTH_KEY = BaseCore.MOD_ID + ":hash_chest_health";

    public static void registerNets() {
        EasyNetRegister.registerNet(BASECORE_BLOCK_KEY, new BasecoreBlockNet());
        EasyNetRegister.registerNet(ACTIVITY_BASECORE_KEY, new ActivityBasecoreNet());
        EasyNetRegister.registerNet(DEFEND_BLOCK_KEY, new dev.anye.mc.basecore.net.DefendNet());
        EasyNetRegister.registerNet(HASH_CHEST_HEALTH_KEY, new HashChestNet());
    }
}