package dev.anye.mc.basecore.item;

import dev.anye.mc.basecore.basecore.BasecoreServerHelper;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

import java.util.Random;

public class ProtectionSignalShieldItem extends EasyItem {
    private static final Random RANDOM = new Random();

    public ProtectionSignalShieldItem() {
        super(Rarity.UNCOMMON, 1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pPlayer instanceof ServerPlayer serverPlayer) {
            ItemStack itemStack = serverPlayer.getItemInHand(pUsedHand);
            if (itemStack.is(this)) {
                BaseCoreBlockEntity baseCoreBlockEntity = BasecoreServerHelper.getBasecore(pLevel, pPlayer.position(), 3);
                if (baseCoreBlockEntity == null) {
                    serverPlayer.sendSystemMessage(Component.translatable("error.basecore.pssi.no_basecore"));
                    return InteractionResultHolder.fail(itemStack);
                }
                if (baseCoreBlockEntity.canUse(serverPlayer.getUUID())) {
                    serverPlayer.sendSystemMessage(Component.translatable("error.basecore.pssi.has_permission"));
                    return InteractionResultHolder.fail(itemStack);
                }
                if (baseCoreBlockEntity.getEntityData().getInterferenceTime() > 0) {
                    serverPlayer.sendSystemMessage(Component.translatable("error.basecore.pssi.interfered"));
                    return InteractionResultHolder.fail(itemStack);
                }
                itemStack.shrink(1);
                baseCoreBlockEntity.getEntityData().setInterferenceTime(6000);
                baseCoreBlockEntity.updateToClient();
                giveRandomItems(serverPlayer);
                return InteractionResultHolder.success(itemStack);
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    private void giveRandomItems(ServerPlayer serverPlayer) {
        net.minecraft.world.item.Item[] salvageItems = {
                dev.anye.mc.basecore.item.ItemRegister.Chip.get(),
                dev.anye.mc.basecore.item.ItemRegister.Wire.get(),
                dev.anye.mc.basecore.item.ItemRegister.Antenna.get(),
                dev.anye.mc.basecore.item.ItemRegister.Copper_Wire.get(),
        };
        int count = 1 + RANDOM.nextInt(2); // 1-2
        for (int i = 0; i < count; i++) {
            serverPlayer.getInventory().placeItemBackInInventory(new ItemStack(salvageItems[RANDOM.nextInt(salvageItems.length)], 1));
        }
    }
}