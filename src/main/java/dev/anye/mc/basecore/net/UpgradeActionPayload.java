package dev.anye.mc.basecore.net;

import dev.anye.mc.basecore.menu.upgrade.BasecoreUpgradeMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * C2S: Player clicks [+1] (buy) or [-1] (sell-back) on an upgrade entry.
 */
public record UpgradeActionPayload(int entryIndex, boolean isBuy) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.parse("basecore:upgrade_action");
    public static final CustomPacketPayload.Type<UpgradeActionPayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, UpgradeActionPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.entryIndex);
                buf.writeBoolean(payload.isBuy);
            },
            buf -> new UpgradeActionPayload(buf.readInt(), buf.readBoolean())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpgradeActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (serverPlayer.containerMenu instanceof BasecoreUpgradeMenu menu) {
                    menu.processAction(payload.entryIndex(), payload.isBuy());
                }
            }
        });
    }
}
