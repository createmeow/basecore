package dev.anye.mc.basecore.client.data;

/**
 * Client-side cache for placement progress synced from the server.
 */
public class ClientPlacementData {
    public static final ClientPlacementData INSTANCE = new ClientPlacementData();

    public boolean active = false;
    public int remainingTicks = 0;
    public String displayName = "";

    public void update(int remainingTicks, String displayName) {
        this.active = remainingTicks > 0;
        this.remainingTicks = remainingTicks;
        this.displayName = displayName;
    }

    public void reset() {
        this.active = false;
        this.remainingTicks = 0;
        this.displayName = "";
    }
}
