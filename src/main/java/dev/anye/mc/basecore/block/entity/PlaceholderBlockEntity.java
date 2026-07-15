package dev.anye.mc.basecore.block.entity;

import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.BlockRegister;
import dev.anye.mc.basecore.net.PlacementProgressPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlaceholderBlockEntity extends BlockEntity {
    private UUID placerUUID;
    private CompoundTag blockEntityData;
    private ItemStack returnStack = ItemStack.EMPTY;
    private String targetBlockId;      // e.g. "basecore:basecore"
    private String displayName;         // for HUD
    private int remainingTicks = 600;   // 30 seconds
    private static final int TOTAL_TICKS = 600;
    private static final double MAX_DISTANCE_SQ = 4.4 * 4.4; // 4.4 blocks radius squared

    public PlaceholderBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegister.PLACEHOLDER.get(), pPos, pBlockState);
    }

    public void init(UUID placer, CompoundTag data, String blockId, String name, ItemStack originalStack) {
        this.placerUUID = placer;
        this.blockEntityData = data;
        this.returnStack = originalStack.copy();
        this.returnStack.setCount(1);
        this.targetBlockId = blockId;
        this.displayName = name;
        this.remainingTicks = TOTAL_TICKS;
        dev.anye.mc.basecore.event.ForgeEvent.ACTIVE_PLACEMENTS.put(placer, worldPosition);
        setChanged();
        syncToClient();
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (level.isClientSide()) return;
        if (remainingTicks <= 0) return;

        // Find the placer
        if (placerUUID == null) {
            cancelPlacement(level, blockPos, null);
            return;
        }
        Player player = level.getPlayerByUUID(placerUUID);
        if (player == null || !player.isAlive()) {
            cancelPlacement(level, blockPos, null);
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer) player;

        // Check cancel conditions
        String cancelReason = checkCancelConditions(level, serverPlayer, blockPos);
        if (cancelReason != null) {
            cancelPlacement(level, blockPos, serverPlayer);
            if (!cancelReason.isEmpty()) {
                serverPlayer.displayClientMessage(net.minecraft.network.chat.Component.literal(cancelReason), true);
            }
            return;
        }

        remainingTicks--;
        // Sync every tick for smooth progress bar animation
        syncToClient();

        if (remainingTicks <= 0) {
            completePlacement(level, blockPos);
        }
    }

    private String checkCancelConditions(Level level, ServerPlayer player, BlockPos pos) {
        // Distance check (> 4.4 blocks)
        Vec3 playerPos = player.position();
        double dx = playerPos.x - (pos.getX() + 0.5);
        double dy = playerPos.y - (pos.getY() + 0.5);
        double dz = playerPos.z - (pos.getZ() + 0.5);
        if (dx * dx + dy * dy + dz * dz > MAX_DISTANCE_SQ) {
            return "§c放置取消：§7你离目标太远了";
        }

        // Sprinting check
        if (player.isSprinting()) {
            return "§c放置取消：§7疾跑中无法放置";
        }

        // Jumping check (not on ground)
        if (!player.onGround()) {
            return "§c放置取消：§7在空中无法放置";
        }

        return null;
    }

    /**
     * Called from ForgeEvent when the player tries to break/place another block.
     */
    public void cancelFor(Player player) {
        if (level instanceof ServerLevel serverLevel) {
            cancelPlacement(serverLevel, worldPosition, player instanceof ServerPlayer sp ? sp : null);
        }
    }

    private void cancelPlacement(Level level, BlockPos pos, ServerPlayer player) {
        remainingTicks = 0;
        dev.anye.mc.basecore.event.ForgeEvent.ACTIVE_PLACEMENTS.remove(placerUUID);

        // Remove the placeholder block
        level.removeBlock(pos, false);

        // Return the original item stack to the player
        if (player != null && !returnStack.isEmpty()) {
            if (!player.getInventory().add(returnStack)) {
                player.drop(returnStack, false);
            }
        }

        // Send cancel signal to client
        sendProgress(-1);
        setChanged();
    }

    private void completePlacement(Level level, BlockPos pos) {
        dev.anye.mc.basecore.event.ForgeEvent.ACTIVE_PLACEMENTS.remove(placerUUID);

        // Remove placeholder
        level.removeBlock(pos, false);

        // Place the real block
        var blockKey = net.minecraft.core.registries.Registries.BLOCK;
        var blockHolder = level.registryAccess().lookupOrThrow(blockKey)
                .get(net.minecraft.resources.ResourceKey.create(blockKey, net.minecraft.resources.ResourceLocation.parse(targetBlockId)));
        if (blockHolder.isEmpty()) return;

        BlockState targetState = blockHolder.get().value().defaultBlockState();
        level.setBlock(pos, targetState, Block.UPDATE_ALL);

        // Load NBT data onto the auto-created BE
        if (blockEntityData != null && !blockEntityData.isEmpty()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be != null) {
                // handleUpdateTag reads owner, entityData, and Inventory key
                // The saveToItem NBT has inventory under "basecore.inventory.item",
                // but handleUpdateTag expects "Inventory" — copy it
                if (blockEntityData.contains("basecore.inventory.item")) {
                    blockEntityData.put("Inventory", blockEntityData.getCompound("basecore.inventory.item"));
                }
                be.handleUpdateTag(blockEntityData, level.registryAccess());
                be.setChanged();
                // Register the basecore in the server helper cache
                if (be instanceof dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity coreBE) {
                    dev.anye.mc.basecore.basecore.BasecoreServerHelper.addEntity(coreBE);
                }
            }
            level.sendBlockUpdated(pos, targetState, targetState, Block.UPDATE_ALL);
        } else {
            // No NBT data at all — ensure at least the placer UUID is set
            if (placerUUID != null) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof dev.anye.mc.basecore.block.IOwner ownerBe) {
                    ownerBe.setOwner(placerUUID);
                    be.setChanged();
                }
            }
        }

        sendProgress(0);
    }

    private void syncToClient() {
        sendProgress(remainingTicks);
    }

    private void sendProgress(int ticks) {
        if (placerUUID == null) return;
        if (level instanceof ServerLevel serverLevel) {
            Player player = serverLevel.getPlayerByUUID(placerUUID);
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer,
                        new PlacementProgressPayload(ticks, displayName));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (placerUUID != null) tag.putUUID("placer", placerUUID);
        if (blockEntityData != null) tag.put("data", blockEntityData);
        if (targetBlockId != null) tag.putString("target_block", targetBlockId);
        if (displayName != null) tag.putString("display_name", displayName);
        if (!returnStack.isEmpty()) {
            tag.put("return_stack", returnStack.save(registries));
        }
        tag.putInt("ticks", remainingTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("placer")) placerUUID = tag.getUUID("placer");
        if (tag.contains("data")) blockEntityData = tag.getCompound("data");
        if (tag.contains("target_block")) targetBlockId = tag.getString("target_block");
        if (tag.contains("display_name")) displayName = tag.getString("display_name");
        if (tag.contains("return_stack")) {
            returnStack = ItemStack.parse(registries, tag.getCompound("return_stack")).orElse(ItemStack.EMPTY);
        }
        if (tag.contains("ticks")) remainingTicks = tag.getInt("ticks");
        if (placerUUID != null) {
            dev.anye.mc.basecore.event.ForgeEvent.ACTIVE_PLACEMENTS.put(placerUUID, worldPosition);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadAdditional(tag, registries);
    }
}
