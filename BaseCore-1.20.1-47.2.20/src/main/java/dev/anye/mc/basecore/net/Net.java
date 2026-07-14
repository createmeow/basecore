package dev.anye.mc.basecore.net;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.net.easy_net.EasyNetCTS;
import dev.anye.mc.basecore.net.easy_net.EasyNetSTC;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class Net {
    public static final String EASY_NET_KEY = "easy_net.key";
    private static final String PROTOCOL_VERSION = "1";
    private static int packetId = 0;
    private static int id(){
        return packetId++;
    }
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(BaseCore.MOD_ID, "net.normal"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    public static final SimpleChannel EASY_NET = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(BaseCore.MOD_ID, "net.easy_net"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    public static void  reg(){
        EASY_NET.messageBuilder(EasyNetSTC.class,id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(EasyNetSTC::new)
                .encoder(EasyNetSTC::toBytes)
                .consumerMainThread(EasyNetSTC::handle).add();

        EASY_NET.messageBuilder(EasyNetCTS.class,id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(EasyNetCTS::new)
                .encoder(EasyNetCTS::toBytes)
                .consumerMainThread(EasyNetCTS::handle).add();
    }
    public static <MSG> void sendToServer(MSG msg){
        INSTANCE.sendToServer(msg);
    }
    public static <MSG> void sendToPlayer(MSG msg, ServerPlayer serverPlayer){
        INSTANCE.send(PacketDistributor.PLAYER.with(()->serverPlayer), msg);
    }
    public static <MSG> void EasyNetCTS(MSG msg){
        EASY_NET.sendToServer(msg);
    }
    @Deprecated
    public static <MSG> void EasyNetSTC(MSG msg, ServerPlayer serverPlayer){
        EasyNetSTC(PacketDistributor.PLAYER.with(()->serverPlayer), msg);
    }
    public static <MSG> void EasyNetSTP(MSG msg, ServerPlayer serverPlayer){
        EasyNetSTC(PacketDistributor.PLAYER.with(()->serverPlayer), msg);
    }
    public static <MSG> void EasyNetSTTE(MSG msg, Entity entity){
        EasyNetSTC(PacketDistributor.TRACKING_ENTITY.with(()->entity), msg);
    }
    public static <MSG> void EasyNetSTC(PacketDistributor.PacketTarget target, MSG msg){
        EASY_NET.send(target, msg);
    }
}
