package dev.anye.mc.basecore.net;

import dev.anye.mc.basecore.menu.BaseCoreMenu;
import dev.anye.mc.basecore.menu.upgrade.BasecoreUpgradeMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * C2S: Add or remove a member from the base.
 */
public record MemberActionPayload(String playerName, boolean isAdd) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.parse("basecore:member_action");
    public static final CustomPacketPayload.Type<MemberActionPayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, MemberActionPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUtf(payload.playerName);
                buf.writeBoolean(payload.isAdd);
            },
            buf -> new MemberActionPayload(buf.readUtf(), buf.readBoolean())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(MemberActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (serverPlayer.containerMenu instanceof BasecoreUpgradeMenu menu) {
                    if (payload.isAdd()) {
                        menu.addMember(payload.playerName());
                    } else {
                        menu.removeMember(payload.playerName());
                    }
                } else if (serverPlayer.containerMenu instanceof BaseCoreMenu menu) {
                    if (payload.isAdd()) {
                        menu.addMember(payload.playerName());
                    } else {
                        menu.removeMember(payload.playerName());
                    }
                }
            }
        });
    }
}
