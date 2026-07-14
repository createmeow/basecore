package dev.anye.mc.basecore.net;

import dev.anye.mc.basecore.block.entity.DefendBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * C2S: Switch the defense turret to a specific mode (1=monster, 2=player, 3=all).
 */
public record DefendModePayload(BlockPos turretPos, int mode) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.parse("basecore:defend_mode");
    public static final CustomPacketPayload.Type<DefendModePayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, DefendModePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBlockPos(payload.turretPos);
                buf.writeInt(payload.mode);
            },
            buf -> new DefendModePayload(buf.readBlockPos(), buf.readInt())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(DefendModePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (serverPlayer.level().getBlockEntity(payload.turretPos()) instanceof DefendBlockEntity turret) {
                    if (turret.canUse(serverPlayer.getUUID())) {
                        turret.setType(payload.mode());
                        turret.updateToTracking();
                        String modeName = switch (payload.mode()) {
                            case 1 -> "敌对生物";
                            case 2 -> "玩家";
                            case 3 -> "所有";
                            default -> "未知";
                        };
                        serverPlayer.sendSystemMessage(
                                net.minecraft.network.chat.Component.literal("§a防御炮模式已切换为: §e" + modeName));
                    } else {
                        serverPlayer.sendSystemMessage(
                                net.minecraft.network.chat.Component.translatable("error.basecore.permission.right_click_block"));
                    }
                }
            }
        });
    }
}
