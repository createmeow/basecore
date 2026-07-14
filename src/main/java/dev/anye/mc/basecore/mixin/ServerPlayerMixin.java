package dev.anye.mc.basecore.mixin;

import com.mojang.authlib.GameProfile;
import dev.anye.mc.basecore.basecore.BasecoreServerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    public ServerPlayerMixin(Level pLevel, BlockPos pPos, float pYRot, GameProfile pGameProfile) {
        super(pLevel, pPos, pYRot, pGameProfile);
    }

    @Inject(method = "openMenu", at = @At("HEAD"), cancellable = true)
    private void basecore$openMenu$add(MenuProvider pMenu, CallbackInfoReturnable<OptionalInt> cir) {
        if (!BasecoreServerHelper.hasPermission((ServerLevel) this.level(),this.position(),this.uuid)) {
            sendSystemMessage(Component.translatable("error.basecore.permission.open_menu"));
            cir.setReturnValue(OptionalInt.empty());
        }
    }
}