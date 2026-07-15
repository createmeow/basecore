package dev.anye.mc.basecore.item;

import dev.anye.mc.basecore.block.BlockRegister;
import dev.anye.mc.basecore.block.entity.PlaceholderBlockEntity;
import dev.anye.mc.basecore.block.placeholder.PlaceholderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Helper for starting a 30-second placement for blocks that require it.
 */
public class PlacementHelper {

    /**
     * Attempt to start placing a block with a 30s timer.
     * Called from BlockItem.useOn().
     *
     * @return true if placement was started, false if it should fall through to normal placement
     */
    public static boolean startPlacement(ServerPlayer player, UseOnContext ctx, ItemStack stack, Block targetBlock) {
        Level level = ctx.getLevel();

        // Calculate where the block would be placed
        BlockPlaceContext placeContext = new BlockPlaceContext(ctx);
        BlockPos targetPos = placeContext.getClickedPos();
        BlockState existingState = level.getBlockState(targetPos);

        // Check if the position is valid (replaceable or air)
        if (!existingState.isAir() && !existingState.canBeReplaced()) {
            // Try adjacent position
            targetPos = placeContext.getClickedPos().relative(placeContext.getClickedFace());
            BlockState adjacentState = level.getBlockState(targetPos);
            if (!adjacentState.isAir() && !adjacentState.canBeReplaced()) {
                return false;
            }
        }

        // Check if there's already a placement here
        if (level.getBlockEntity(targetPos) instanceof PlaceholderBlockEntity) {
            player.displayClientMessage(Component.literal("§c该位置已有正在放置的方块"), true);
            return true;
        }

        // Get the item's NBT data (owner, modules, etc.)
        CompoundTag data = null;
        CustomData customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (customData != null) {
            data = customData.copyTag();
        }

        // Ensure the owner is always set
        UUID playerUUID = player.getUUID();
        if (data == null) {
            data = new CompoundTag();
        }
        if (!data.hasUUID("owner")) {
            data.putUUID("owner", playerUUID);
        }
        // Ensure owner_name is set for UI display
        if (!data.contains("owner_name") || data.getString("owner_name").isEmpty()) {
            data.putString("owner_name", player.getName().getString());
        }
        // Set default base name if empty: "<player_name>的基地"
        if (!data.contains("name") || data.getString("name").isEmpty()) {
            data.putString("name", player.getName().getString() + "的基地");
        }

        String blockId = net.minecraft.resources.ResourceLocation.parse(
                targetBlock.builtInRegistryHolder().key().location().toString()).toString();
        String displayName = stack.getHoverName().getString();

        // Place the placeholder block
        level.setBlock(targetPos, BlockRegister.PLACEHOLDER.get().defaultBlockState(), 3);

        // Initialize the placeholder BE
        if (level.getBlockEntity(targetPos) instanceof PlaceholderBlockEntity be) {
            // Save a COPY of the original stack for item return on cancel
            ItemStack returnStack = stack.copy();
            returnStack.setCount(1);
            be.init(player.getUUID(), data != null ? data : new CompoundTag(), blockId, displayName, returnStack);
        }

        // Consume the item
        stack.shrink(1);

        player.displayClientMessage(Component.literal("§a开始放置 §e" + displayName + " §a..."), true);
        return true;
    }
}
