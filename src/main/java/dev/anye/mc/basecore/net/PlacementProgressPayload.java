package dev.anye.mc.basecore.net;

import dev.anye.mc.basecore.BaseCore;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * S2C payload to sync placement progress to the client.
 * @param remainingTicks >0 = progress update, 0 = completed, -1 = cancelled
 * @param displayName The block name to display on the HUD
 */
public record PlacementProgressPayload(int remainingTicks, String displayName) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlacementProgressPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(BaseCore.MOD_ID, "placement_progress"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlacementProgressPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public @NotNull PlacementProgressPayload decode(RegistryFriendlyByteBuf buf) {
                    return new PlacementProgressPayload(buf.readInt(), buf.readUtf(256));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, PlacementProgressPayload value) {
                    buf.writeInt(value.remainingTicks);
                    buf.writeUtf(value.displayName, 256);
                }
            };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
