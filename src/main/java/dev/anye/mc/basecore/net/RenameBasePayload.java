package dev.anye.mc.basecore.net;

import dev.anye.mc.basecore.menu.BaseCoreMenu;
import dev.anye.mc.basecore.menu.SovietComputerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * C2S: 修改基地名称（仅领主可用），普通基地核心与苏式基地计算机共用。
 */
public record RenameBasePayload(String name) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.parse("basecore:rename_base");
    public static final CustomPacketPayload.Type<RenameBasePayload> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final int MAX_NAME_LENGTH = 32;

    public static final StreamCodec<FriendlyByteBuf, RenameBasePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeUtf(payload.name, MAX_NAME_LENGTH),
            buf -> new RenameBasePayload(buf.readUtf(MAX_NAME_LENGTH))
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RenameBasePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (serverPlayer.containerMenu instanceof BaseCoreMenu menu) {
                    menu.renameBase(payload.name());
                } else if (serverPlayer.containerMenu instanceof SovietComputerMenu menu) {
                    menu.renameBase(payload.name());
                }
            }
        });
    }
}
