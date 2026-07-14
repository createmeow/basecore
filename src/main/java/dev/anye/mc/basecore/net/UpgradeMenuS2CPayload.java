package dev.anye.mc.basecore.net;

import dev.anye.mc.basecore.item.module.BasecoreModuleItem;
import dev.anye.mc.basecore.menu.upgrade.UpgradeEntry;
import dev.anye.mc.basecore.screen.BasecoreUpgradeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * S2C: Sync upgrade entries + player parts count to the client.
 * Member data is synced separately via EasyNet (BasecoreBlockNet).
 */
public class UpgradeMenuS2CPayload implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.parse("basecore:upgrade_menu_s2c");
    public static final CustomPacketPayload.Type<UpgradeMenuS2CPayload> TYPE = new CustomPacketPayload.Type<>(ID);

    private final List<UpgradeEntry> entries;
    private final int playerParts;

    public UpgradeMenuS2CPayload(List<UpgradeEntry> entries, int playerParts) {
        this.entries = entries;
        this.playerParts = playerParts;
    }

    public List<UpgradeEntry> getEntries() { return entries; }
    public int getPlayerParts() { return playerParts; }

    public static final StreamCodec<FriendlyByteBuf, UpgradeMenuS2CPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.playerParts);
                buf.writeInt(payload.entries.size());
                for (UpgradeEntry e : payload.entries) {
                    buf.writeUtf(BuiltInRegistries.ITEM.getKey(e.getModule()).toString());
                    buf.writeInt(e.getPartCost());
                    buf.writeInt(e.getCurrentCount());
                    buf.writeInt(e.getMaxCount());
                }
            },
            buf -> {
                int parts = buf.readInt();
                int size = buf.readInt();
                List<UpgradeEntry> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(buf.readUtf()));
                    int cost = buf.readInt();
                    int current = buf.readInt();
                    int max = buf.readInt();
                    if (item instanceof BasecoreModuleItem module) {
                        list.add(new UpgradeEntry(module, cost, new ItemStack(module), max, current));
                    }
                }
                return new UpgradeMenuS2CPayload(list, parts);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(UpgradeMenuS2CPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof BasecoreUpgradeScreen screen) {
                screen.updateEntries(payload.getEntries(), payload.getPlayerParts());
            }
        });
    }
}
