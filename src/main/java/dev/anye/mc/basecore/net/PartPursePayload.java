package dev.anye.mc.basecore.net;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.cap.PartHolder;
import dev.anye.mc.basecore.item.ItemRegister;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * C2S packet for part purse operations: store, extract, extract_all
 */
public record PartPursePayload(String action, int amount) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.parse("basecore:part_purse");
    public static final CustomPacketPayload.Type<PartPursePayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PartPursePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUtf(payload.action);
                buf.writeInt(payload.amount);
            },
            buf -> new PartPursePayload(buf.readUtf(), buf.readInt())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PartPursePayload payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                switch (payload.action) {
                    case "store" -> {
                        int stored = 0;
                        for (int i = 0; i < serverPlayer.getInventory().getContainerSize(); i++) {
                            ItemStack stack = serverPlayer.getInventory().getItem(i);
                            if (stack.getItem() == ItemRegister.PART.get()) {
                                stored += stack.getCount();
                                serverPlayer.getInventory().setItem(i, ItemStack.EMPTY);
                            } else if (stack.getItem() == ItemRegister.PART_BUNDLE.get()) {
                                int count = dev.anye.mc.basecore.item.component.PartBundleItem.getCount(stack);
                                stored += count;
                                serverPlayer.getInventory().setItem(i, ItemStack.EMPTY);
                            }
                        }
                        if (stored > 0) {
                            PartHolder.modify(serverPlayer, stored);
                        }
                    }
                    case "extract" -> {
                        int amount = payload.amount;
                        if (amount <= 0) return;
                        int current = PartHolder.getValue(serverPlayer);
                        int toExtract = Math.min(amount, current);
                        if (toExtract <= 0) return;

                        int remaining = toExtract;
                        while (remaining > 0) {
                            int batchSize = Math.min(remaining, 64);
                            ItemStack giveStack = new ItemStack(ItemRegister.PART.get(), batchSize);
                            if (!serverPlayer.getInventory().add(giveStack)) {
                                serverPlayer.drop(giveStack, false);
                            }
                            remaining -= batchSize;
                        }
                        PartHolder.modify(serverPlayer, -toExtract);
                    }
                    case "extract_all" -> {
                        int allCurrent = PartHolder.getValue(serverPlayer);
                        if (allCurrent <= 0) return;

                        int remaining2 = allCurrent;
                        while (remaining2 > 0) {
                            int batchSize = Math.min(remaining2, 64);
                            ItemStack giveStack = new ItemStack(ItemRegister.PART.get(), batchSize);
                            if (!serverPlayer.getInventory().add(giveStack)) {
                                serverPlayer.drop(giveStack, false);
                            }
                            remaining2 -= batchSize;
                        }
                        PartHolder.modify(serverPlayer, -allCurrent);
                    }
                }
            }
        });
    }

    public static PartPursePayload storeAll() {
        return new PartPursePayload("store", 0);
    }

    public static PartPursePayload extract(int amount) {
        return new PartPursePayload("extract", amount);
    }

    public static PartPursePayload extractAll() {
        return new PartPursePayload("extract_all", 0);
    }
}
