package dev.anye.mc.basecore.net;

import dev.anye.mc.basecore.menu.upgrade.BasecoreUpgradeMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * C2S: Client requests a fresh sync from the server on screen open.
 */
public record UpgradeRefreshPayload() implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.parse("basecore:upgrade_refresh");
    public static final CustomPacketPayload.Type<UpgradeRefreshPayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, UpgradeRefreshPayload> STREAM_CODEC = StreamCodec.unit(new UpgradeRefreshPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(UpgradeRefreshPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (serverPlayer.containerMenu instanceof BasecoreUpgradeMenu menu) {
                    menu.syncToClient();
                }
            }
        });
    }
}
