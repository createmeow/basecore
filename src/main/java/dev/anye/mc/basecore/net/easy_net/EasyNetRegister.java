package dev.anye.mc.basecore.net.easy_net;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.net.Net;
import dev.anye.mc.basecore.net.MemberActionPayload;
import dev.anye.mc.basecore.net.PartPursePayload;
import dev.anye.mc.basecore.net.UpgradeActionPayload;
import dev.anye.mc.basecore.net.UpgradeMenuS2CPayload;
import dev.anye.mc.basecore.net.UpgradeRefreshPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.concurrent.ConcurrentHashMap;

public class EasyNetRegister {
    public static final ConcurrentHashMap<String, EasyNet> EASY_NET_MAP = new ConcurrentHashMap<>();

    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(BaseCore.MOD_ID).versioned("1").optional();

        // Existing EasyNet handler
        registrar.playBidirectional(
            EasyNetPayload.TYPE,
            EasyNetPayload.STREAM_CODEC,
            (payload, context) -> {
                context.enqueueWork(() -> {
                    String key = payload.tag().getString(Net.EASY_NET_KEY);
                    EasyNet easyNet = EASY_NET_MAP.get(key);
                    if (easyNet != null) {
                        if (context.flow().isServerbound()) {
                            easyNet.server(payload.tag());
                        } else {
                            easyNet.client(payload.tag());
                        }
                    }
                });
            }
        );

        // Part purse C2S handler
        registrar.playToServer(
            PartPursePayload.TYPE,
            PartPursePayload.STREAM_CODEC,
            PartPursePayload::handle
        );

        // Upgrade action C2S
        registrar.playToServer(
            UpgradeActionPayload.TYPE,
            UpgradeActionPayload.STREAM_CODEC,
            UpgradeActionPayload::handle
        );

        // Upgrade menu S2C
        registrar.playToClient(
            UpgradeMenuS2CPayload.TYPE,
            UpgradeMenuS2CPayload.STREAM_CODEC,
            UpgradeMenuS2CPayload::handle
        );

        // Upgrade refresh C2S
        registrar.playToServer(
            UpgradeRefreshPayload.TYPE,
            UpgradeRefreshPayload.STREAM_CODEC,
            UpgradeRefreshPayload::handle
        );

        // Member action C2S
        registrar.playToServer(
            MemberActionPayload.TYPE,
            MemberActionPayload.STREAM_CODEC,
            MemberActionPayload::handle
        );
    }

    public static void registerNet(String key, EasyNet easyNet) {
        EASY_NET_MAP.put(key, easyNet);
    }
}