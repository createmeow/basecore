package dev.anye.mc.basecore.net;

import dev.anye.mc.basecore.net.easy_net.EasyNetPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

public class Net {
    public static final String EASY_NET_KEY = "easy_net.key";

    public static void sendToServer(EasyNetPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    public static void sendToPlayer(EasyNetPayload payload, ServerPlayer serverPlayer) {
        serverPlayer.connection.send(payload);
    }

    public static void sendToTracking(EasyNetPayload payload, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    public static void sendToAllPlayers(EasyNetPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }
}