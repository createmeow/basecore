package dev.anye.mc.basecore.mixin;

import dev.anye.mc.basecore.cap.PartHolder;
import dev.anye.mc.basecore.config.BasecoreConfig;
import dev.anye.mc.basecore.item.component.PartBundleItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerDeathMixin {

    @Inject(method = {"destroyVanishingCursedItems"}, at = {@At("TAIL")})
    public void basecore$onServerDeath(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        Level world = player.level();

        if (world.isClientSide) return;
        if (!BasecoreConfig.isComponentMode()) return;

        int parts = PartHolder.getValue(player);
        if (parts <= 0) return;

        float dropPercentage = BasecoreConfig.getPartDeathDropPercent() * 0.01f;
        int dropped = (int) (parts * dropPercentage);
        if (dropped <= 0) return;

        // Drop a PartBundle containing the dropped parts
        ItemStack bundleStack = PartBundleItem.create(dropped);
        player.drop(bundleStack, true, false);

        // Deduct dropped amount from stored parts
        PartHolder.modify(player, -dropped);
    }
}
