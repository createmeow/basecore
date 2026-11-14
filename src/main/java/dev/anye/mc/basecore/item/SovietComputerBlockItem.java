package dev.anye.mc.basecore.item;

import dev.anye.mc.basecore.block.BlockRegister;
import dev.anye.mc.basecore.block.soviet_computer.SovietComputerStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 苏式基地计算机物品：放置需要经过 deployment_seconds 部署进度（与基地核心/防御炮一致），
 * 完成后才生成控制器方块并自动组装多方块结构。
 */
public class SovietComputerBlockItem extends BlockItem {
    public SovietComputerBlockItem() {
        super(BlockRegister.SOVIET_COMPUTER.get(), new Properties().rarity(Rarity.EPIC));
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (!dev.anye.mc.basecore.config.BasecoreConfig.isSovietComputerEnabled()) {
            if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                        Component.translatable("error.basecore.soviet_computer.disabled"), true);
            }
            return InteractionResult.FAIL;
        }
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            // 先校验 3x3x3 空间，避免消耗物品后才发现无法组装
            BlockPos targetPos = PlacementHelper.resolvePlacementPos(context);
            if (targetPos == null || !SovietComputerStructure.canFill(context.getLevel(), targetPos)) {
                serverPlayer.displayClientMessage(
                        Component.translatable("error.basecore.soviet_computer.no_space"), true);
                return InteractionResult.FAIL;
            }
            // 与基地核心一致：范围不能与已有基地重叠（用结构中心而非控制器位置做判定）
            if (!dev.anye.mc.basecore.basecore.BasecoreServerHelper.check(
                    context.getLevel(), targetPos.above(), context.getItemInHand())) {
                serverPlayer.displayClientMessage(
                        Component.translatable("error.basecore.permission.range_has_basecore"), true);
                return InteractionResult.FAIL;
            }
            if (PlacementHelper.startPlacement(serverPlayer, context, context.getItemInHand(), BlockRegister.SOVIET_COMPUTER.get())) {
                return InteractionResult.SUCCESS;
            }
            return super.useOn(context);
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("block.basecore.soviet_computer.desc"));
        tooltip.add(Component.translatable("block.basecore.soviet_computer.desc.temperature"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
