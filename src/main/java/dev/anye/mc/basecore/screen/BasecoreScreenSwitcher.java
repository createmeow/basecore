package dev.anye.mc.basecore.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Utility to switch between BaseCoreScreen and BaseCoreMemberScreen
 * without closing the underlying container on the server.
 */
public class BasecoreScreenSwitcher {
    private static boolean switching = false;

    /**
     * Switch to the given screen, suppressing container close.
     */
    public static void switchTo(Minecraft minecraft, Screen screen) {
        switching = true;
        minecraft.setScreen(screen);
        switching = false;
    }

    /**
     * Whether a screen switch is in progress (container should not be closed).
     */
    public static boolean isSwitching() {
        return switching;
    }
}
