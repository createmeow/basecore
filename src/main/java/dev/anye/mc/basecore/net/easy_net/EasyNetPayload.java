package dev.anye.mc.basecore.net.easy_net;

import dev.anye.mc.basecore.BaseCore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record EasyNetPayload(CompoundTag tag) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.parse("basecore:easy_net");
    public static final CustomPacketPayload.Type<EasyNetPayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, EasyNetPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeNbt(payload.tag),
        buf -> new EasyNetPayload(buf.readNbt())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}