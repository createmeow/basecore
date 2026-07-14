package dev.anye.mc.basecore.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArgumentTypeInfos.class)
public abstract class ArgumentTypeInfosMixin {
    @Shadow
    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ArgumentTypeInfo<A, T> register(Registry<ArgumentTypeInfo<?, ?>> pRegistry, String pId, Class<? extends A> pArgumentClass, ArgumentTypeInfo<A, T> pInfo) {
        return null;
    }

    @Inject(method = "bootstrap",at = @At("HEAD"))
    private static void telos$bootstrap$reg(Registry<ArgumentTypeInfo<?, ?>> pRegistry, CallbackInfoReturnable<ArgumentTypeInfo<?, ?>> cir){
    }
}