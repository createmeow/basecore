package dev.anye.mc.basecore.net.easy_net;

import dev.anye.mc.basecore.net.Net;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EasyNetSTC {
    private final CompoundTag data;
    public EasyNetSTC(CompoundTag data){
        this.data = data;
    }
    public EasyNetSTC(FriendlyByteBuf buf){
        this.data = buf.readNbt();
    }
    public void toBytes(FriendlyByteBuf buf){
        buf.writeNbt(this.data);
    }
    public void handle(Supplier<NetworkEvent.Context> ctx){
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(()-> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () ->
                () -> {
            String key = this.data.getString(Net.EASY_NET_KEY);
            EasyNet easyNet = EasyNetRegister.REGISTRY.get().getValue(ResourceLocation.tryParse(key));
            if (easyNet != null){
                easyNet.client(ctx,this.data);
            }
                }));
    }
}
