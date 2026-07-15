package dev.anye.mc.basecore.event;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.basecore.BasecoreServerHelper;
import dev.anye.mc.basecore.block.BlockEntityRegister;
import dev.anye.mc.basecore.block.BlockRegister;
import dev.anye.mc.basecore.block.IOwner;
import dev.anye.mc.basecore.block.basecore.BaseCoreBlock;
import dev.anye.mc.basecore.block.entity.DamageBlockEntity;
import dev.anye.mc.basecore.block.entity.DefendBlockEntity;
import dev.anye.mc.basecore.block.entity.HashChestBlockEntity;
import dev.anye.mc.basecore.block.entity.NothingBlockEntity;
import dev.anye.mc.basecore.block.entity.PlaceholderBlockEntity;
import dev.anye.mc.basecore.block.entity.ToDamageBlockEntity;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.cap.PartHolder;
import dev.anye.mc.basecore.command.BasecoreCommands;
import dev.anye.mc.basecore.effect.EffectRegister;
import dev.anye.mc.basecore.item.ItemRegister;
import dev.anye.mc.basecore.item.module.BaseModuleItem;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = BaseCore.MOD_ID)
public class ForgeEvent {
    private static final Logger LOGGER = LogUtils.getLogger();
    @SubscribeEvent
    public static void onChunk(ChunkEvent.Load event){
        if (event.getLevel() instanceof ServerLevel serverLevel){
            event.getChunk().getBlockEntitiesPos().forEach(blockPos -> {
                event.getChunk().getBlockEntity(blockPos,BlockEntityRegister.BASECORE.get()).ifPresent(BasecoreServerHelper::addEntity);
            });
            BasecoreServerHelper.upToClient();
        }
    }
    @SubscribeEvent
    public static void onChunk(ChunkEvent.Unload event){
        if (event.getLevel() instanceof ServerLevel serverLevel){
            event.getChunk().getBlockEntitiesPos().forEach(blockPos -> {
                event.getChunk().getBlockEntity(blockPos,BlockEntityRegister.BASECORE.get()).ifPresent(BasecoreServerHelper::delEntity);
            });
            BasecoreServerHelper.upToClient();
        }
    }

    @SubscribeEvent
    public static void regCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("base")

                        .then(Commands.literal("member")
                                        .then(Commands.argument("basename", StringArgumentType.string())
                                                .then(Commands.literal("add")
                                                        .then(Commands.argument("player",EntityArgument.player())
                                                                .executes(BasecoreCommands::add)
                                                        )
                                                )
                                                .then(Commands.literal("rm")
                                                        .then(Commands.argument("player",EntityArgument.player())
                                                                .executes(BasecoreCommands::rm)
                                                        )
                                                )
                                        )
                        )
                        .then(Commands.literal("leave")
                                .then(Commands.argument("basename", StringArgumentType.string())
                                        .executes(BasecoreCommands::leave)
                                )
                        )
                        .then(Commands.literal("rename")
                                .then(Commands.argument("old_name",StringArgumentType.string())
                                .then(Commands.argument("new_name",StringArgumentType.string())
                                        .executes(BasecoreCommands::rename)
                                ))

                        )
                        .then(Commands.literal("parts")
                                .then(Commands.literal("check")
                                        .executes(BasecoreCommands::partsCheck)
                                )
                                .then(Commands.literal("store")
                                        .executes(BasecoreCommands::partsStore)
                                )
                                .then(Commands.literal("extract")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(BasecoreCommands::partsExtract)
                                        )
                                        .then(Commands.literal("all")
                                                .executes(BasecoreCommands::partsExtractAll)
                                        )
                                )
                        )
        );
    }
    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickBlock event){
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (!BasecoreServerHelper.hasPermission(event.getLevel(), event.getPos().getCenter(), serverPlayer.getUUID())) {
                if (event.getLevel().getBlockEntity(event.getPos()) instanceof HashChestBlockEntity hashChestBlockEntity){
                    if (hashChestBlockEntity.canUse(serverPlayer.getUUID())){
                        return;
                    }
                }
                BaseCoreBlockEntity baseCoreBlockEntity = BasecoreServerHelper.getBasecore(serverPlayer.level(), event.getPos().getCenter());
                if (baseCoreBlockEntity != null && baseCoreBlockEntity.getEntityData().getInterferenceTime() > 0) {
                    BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
                    if (be == null || !(be instanceof MenuProvider)) {
                        return;
                    }
                }
                event.getEntity().sendSystemMessage(Component.translatable("error.basecore.permission.right_click_block"));
                event.setCanceled(true);
            }
        }
    }
    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.LeftClickBlock event){
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            BaseCoreBlockEntity baseCoreBlockEntity = BasecoreServerHelper.getBasecore(serverPlayer.level(),event.getPos().getCenter());
            if (baseCoreBlockEntity == null) return;
            if (!baseCoreBlockEntity.canUse(serverPlayer.getUUID())) {
                if (serverPlayer.level().getBlockEntity(event.getPos()) instanceof MenuProvider menuProvider) {
                    if (baseCoreBlockEntity.getEntityData().getInterferenceTime() > 0) {
                        if (menuProvider instanceof DefendBlockEntity || menuProvider instanceof BaseCoreBlockEntity)
                            return;
                        if (menuProvider instanceof net.minecraft.world.level.block.entity.BaseContainerBlockEntity)
                            return;
                    }
                    event.getEntity().sendSystemMessage(Component.translatable("error.basecore.permission.left_click_block"));
                    event.setCanceled(true);
                }else if (baseCoreBlockEntity.getModuleLvl(ItemRegister.SecureModule.get()) > 0 && baseCoreBlockEntity.getEntityData().getInterferenceTime() <= 0){
                    event.getEntity().sendSystemMessage(Component.translatable("error.basecore.permission.left_click_block"));
                    event.setCanceled(true);
                }
            }
        }
    }

    private static boolean canBreakHashChest(HashChestBlockEntity hashChest, ServerPlayer player) {
        return player.getUUID().equals(hashChest.getOwner());
    }

    /**
     * Check if the player is a non-base-member (invader) within an invaded base.
     */
    private static boolean isInvaderWithinBase(ServerPlayer player, BlockPos pos) {
        BaseCoreBlockEntity baseCore = BasecoreServerHelper.getBasecore(player.level(), pos.getCenter());
        if (baseCore == null) return false;
        if (baseCore.getEntityData().getInterferenceTime() <= 0) return false;
        return !baseCore.canUse(player.getUUID());
    }

    private static <T extends BaseModuleItem> boolean hasThornsModuleLevel(ToDamageBlockEntity<T> blockEntity) {
        @SuppressWarnings("unchecked")
        T thornsModuleItem = (T) ItemRegister.ThornsModule.get();
        return blockEntity.getModuleLvl(thornsModuleItem) > 0;
    }
    @SubscribeEvent
    public static void onDestroyBlock(BlockEvent.BreakEvent event){
        if (event.getPlayer() instanceof ServerPlayer serverPlayer){
            BlockEntity be = serverPlayer.level().getBlockEntity(event.getPos());
            if (be instanceof ToDamageBlockEntity<?> blockEntity){
                if (blockEntity.getOwner() != null && !serverPlayer.getUUID().equals(blockEntity.getOwner())){
                    // Damage from player's held weapon/tool (1 for empty hand)
                    ItemStack weapon = serverPlayer.getMainHandItem();
                    float dmg = (float) serverPlayer.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                    if (dmg < 1f) dmg = 1f;
                    blockEntity.damage((int) dmg);
                    // Members cannot damage their own BaseCoreBlockEntity
                    if (blockEntity instanceof BaseCoreBlockEntity baseCoreBlockEntity && baseCoreBlockEntity.canUse(serverPlayer.getUUID())){
                        event.setCanceled(false);
                        return;
                    }
                    if (blockEntity instanceof BaseCoreBlockEntity baseCoreBlockEntity && baseCoreBlockEntity.getModuleLvl(ItemRegister.ThornsModule.get()) > 0){
                        serverPlayer.hurt(serverPlayer.damageSources().fellOutOfWorld(),3);
                    }
                    if (blockEntity instanceof DefendBlockEntity defendBlockEntity && defendBlockEntity.hasThorns()){
                        serverPlayer.hurt(serverPlayer.damageSources().fellOutOfWorld(),3);
                    }
                    event.setCanceled(true);
                    if (blockEntity.getHealth() <= 0){
                        playDestroyEffects(serverPlayer, blockEntity.getBlockPos());
                        convertToDamageBlock(serverPlayer, blockEntity.getBlockPos(), blockEntity.getItems());
                        if (blockEntity instanceof BaseCoreBlockEntity baseCoreBlockEntity)
                            BasecoreServerHelper.delEntityAndSend(baseCoreBlockEntity);
                    }
                }else {
                    if (blockEntity instanceof BaseCoreBlockEntity baseCoreBlockEntity)
                        BasecoreServerHelper.delEntityAndSend(baseCoreBlockEntity);
                }
            } else if (be instanceof HashChestBlockEntity hashChest) {
                handleHashChestBreak(event, serverPlayer, hashChest);
            } else if (be != null) {
                if (be instanceof net.minecraft.world.level.block.entity.BaseContainerBlockEntity) {
                    BaseCoreBlockEntity baseCoreBlockEntity = BasecoreServerHelper.getBasecore(serverPlayer.level(), event.getPos().getCenter());
                    if (baseCoreBlockEntity != null && baseCoreBlockEntity.getEntityData().getInterferenceTime() > 0) {
                        // Base members break normally; non-members get Nothing block
                        if (baseCoreBlockEntity.canUse(serverPlayer.getUUID())) {
                            return;
                        }
                        event.setCanceled(true);
                        BlockState originalState = event.getState();
                        CompoundTag beData = be.saveWithId(serverPlayer.level().registryAccess());
                        serverPlayer.level().removeBlockEntity(event.getPos());
                        serverPlayer.level().setBlock(event.getPos(), BlockRegister.NOTHING.get().defaultBlockState(), Block.UPDATE_ALL);
                        NothingBlockEntity nothingBe = (NothingBlockEntity) serverPlayer.level().getBlockEntity(event.getPos());
                        if (nothingBe != null) {
                            nothingBe.setOriginalData(originalState, beData);
                        }
                    }
                }
            }
        }
    }

    private static void handleHashChestBreak(BlockEvent.BreakEvent event, ServerPlayer serverPlayer, HashChestBlockEntity hashChest) {
        BaseCoreBlockEntity baseCore = hashChest.getBaseCore();
        boolean inBaseRange = baseCore != null;

        // 1. Invasion: non-base-members → convert to Nothing block (items hidden)
        if (inBaseRange && baseCore.getEntityData().getInterferenceTime() > 0 && !baseCore.canUse(serverPlayer.getUUID())) {
            event.setCanceled(true);
            BlockState originalState = event.getState();
            CompoundTag beData = hashChest.saveWithId(serverPlayer.level().registryAccess());
            serverPlayer.level().removeBlockEntity(event.getPos());
            serverPlayer.level().setBlock(event.getPos(), BlockRegister.NOTHING.get().defaultBlockState(), Block.UPDATE_ALL);
            NothingBlockEntity nothingBe = (NothingBlockEntity) serverPlayer.level().getBlockEntity(event.getPos());
            if (nothingBe != null) {
                nothingBe.setOriginalData(originalState, beData);
            }
            return;
        }

        // 2. Owner can always break directly
        if (serverPlayer.getUUID().equals(hashChest.getOwner())) {
            return;
        }

        // 3. Non-owner inside a base → never allowed to break
        if (inBaseRange) {
            event.setCanceled(true);
            serverPlayer.sendSystemMessage(Component.translatable("error.basecore.permission.break_hash_chest"));
            return;
        }

        // 4. Outside base: non-owner → damage instead of break
        float dmg = (float) serverPlayer.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        if (dmg < 1f) dmg = 1f;
        hashChest.damage((int) dmg);
        event.setCanceled(true);

        if (hashChest.getHealth() <= 0) {
            playDestroyEffects(serverPlayer, hashChest.getBlockPos());
            convertToDamageBlock(serverPlayer, hashChest.getBlockPos(), hashChest.getItems());
        }
    }

    private static void playDestroyEffects(ServerPlayer serverPlayer, BlockPos pos) {
        serverPlayer.level().playSound(
                null,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.BLOCKS,
                1.0F,
                serverPlayer.level().getRandom().nextFloat() * 0.4F + 0.8F
        );

        if (serverPlayer.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION_EMITTER,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.0
            );
        }
    }

    private static void convertToDamageBlock(ServerPlayer serverPlayer, BlockPos pos, ItemStackHandler items) {
        DamageBlockEntity damageBlockEntity = new DamageBlockEntity(pos, BlockRegister.Damage.get().defaultBlockState());
        damageBlockEntity.setItems(items);
        serverPlayer.level().setBlock(pos, BlockRegister.Damage.get().defaultBlockState(), Block.UPDATE_ALL);
        serverPlayer.level().setBlockEntity(damageBlockEntity);
    }
    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event){
        if (event.getEntity() instanceof ServerPlayer serverPlayer){
            ItemStack itemStack = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND);
            if (itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == event.getPlacedBlock().getBlock()) {
                if (event.getLevel().getBlockEntity(event.getPos()) instanceof IOwner owner){
                    if (owner.getOwner() != null && !serverPlayer.getUUID().equals(owner.getOwner())){
                        serverPlayer.hurt(serverPlayer.damageSources().fellOutOfWorld(),2f);
                        event.setCanceled(true);
                        return;
                    }
                }
                if (event.getPlacedBlock().getBlock() instanceof BaseCoreBlock) {
                    if (!BasecoreServerHelper.check(serverPlayer.level(), event.getPos(), itemStack)) {
                        serverPlayer.sendSystemMessage(Component.translatable("error.basecore.permission.range_has_basecore"));
                        event.setCanceled(true);
                    } else {
                        serverPlayer.level().getBlockEntity(event.getPos(), BlockEntityRegister.BASECORE.get()).ifPresent(baseCoreBlockEntity -> {
                        baseCoreBlockEntity.setOwnerWithName(serverPlayer.getUUID(), serverPlayer.getName().getString());
                        BasecoreServerHelper.addEntityAndSend(baseCoreBlockEntity);
                    });
                    }
                } else {
                    BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());
                    if (blockEntity instanceof IOwner owner) {
                        owner.setOwner(serverPlayer.getUUID());

                    }
                }
            }
        }
    }
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event){
        if (event.getEntity() instanceof ServerPlayer serverPlayer){
            BasecoreServerHelper.upToPlayer(serverPlayer);
        }
    }
    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event){
        if (event.getEntity() instanceof ServerPlayer serverPlayer){
            BasecoreServerHelper.upToPlayer(serverPlayer);
        }
    }
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // PersistentData is not copied by default on death respawn
        // Copy stored parts from the old player to the new one
        if (!event.isWasDeath()) return;
        Player original = event.getOriginal();
        Player clone = event.getEntity();
        int oldParts = PartHolder.getValue(original);
        if (oldParts > 0) {
            PartHolder.setValue(clone, oldParts);
        }
    }

    @SubscribeEvent
    public static void onExp(ExplosionEvent.Detonate event){
        if (event.getLevel().isClientSide()) return;
        HashMap<Player, Vec3> players = new HashMap<>(event.getExplosion().getHitPlayers());
        players.forEach((player, vec3) -> {
            BaseCoreBlockEntity baseCoreBlockEntity = BasecoreServerHelper.getBasecore((ServerPlayer) player);
            if (baseCoreBlockEntity != null && baseCoreBlockEntity.getModuleLvl(ItemRegister.ExpDefModule.get()) > 0 && baseCoreBlockEntity.canUse(player.getUUID())) event.getExplosion().getHitPlayers().remove(player);
        });
        List<BlockPos> blockPoss = new ArrayList<>(event.getExplosion().getToBlow());
        for (BlockPos blockPos : blockPoss) {
            BaseCoreBlockEntity baseCoreBlockEntity = BasecoreServerHelper.getBasecore(event.getLevel(),blockPos.getCenter());
            if (baseCoreBlockEntity != null) {
                if (baseCoreBlockEntity.getModuleLvl(ItemRegister.ExpDefModule.get()) > 0){
                    event.getExplosion().getToBlow().remove(blockPos);
                } else if (baseCoreBlockEntity.getBlockPos().equals(blockPos)) {
                    baseCoreBlockEntity.damage(25);
                    event.getExplosion().getToBlow().remove(blockPos);
                }
            }
        }
    }
    @SubscribeEvent
    public static void onEffect(MobEffectEvent.Added event){
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (event.getEffectInstance().getEffect().equals(EffectRegister.Disguise.get())) {
                serverPlayer.getPersistentData().remove("bcrm.basecore.isCheck");
                serverPlayer.getPersistentData().remove("bcrm.basecore.isDiscern");

            }
        }
    }
    @SubscribeEvent
    public static void onEffect(MobEffectEvent.Remove event){
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (event.getEffect().equals(EffectRegister.Disguise.get())) {
                serverPlayer.getPersistentData().remove("bcrm.basecore.isCheck");
                serverPlayer.getPersistentData().remove("bcrm.basecore.isDiscern");

            }
        }
    }
    public static final Map<UUID, BlockPos> ACTIVE_PLACEMENTS = new HashMap<>();

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() == null) return;
        UUID uuid = event.getPlayer().getUUID();
        BlockPos pos = ACTIVE_PLACEMENTS.get(uuid);
        if (pos != null) {
            cancelPlayerPlacement(event.getPlayer(), event.getLevel(), pos);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() == null) return;
        UUID uuid = event.getEntity().getUUID();
        BlockPos pos = ACTIVE_PLACEMENTS.get(uuid);
        if (pos != null) {
            cancelPlayerPlacement(event.getEntity(), event.getLevel(), pos);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() == null) return;
        UUID uuid = event.getEntity().getUUID();
        BlockPos pos = ACTIVE_PLACEMENTS.get(uuid);
        if (pos != null && !event.getItemStack().isEmpty()) {
            cancelPlayerPlacement(event.getEntity(), event.getLevel(), pos);
        }
    }

    private static void cancelPlayerPlacement(Player player, net.minecraft.world.level.LevelAccessor levelAccessor, BlockPos pos) {
        if (levelAccessor instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            if (serverLevel.getBlockEntity(pos) instanceof PlaceholderBlockEntity be) {
                // Trigger cancellation via the BE's cancel logic
                be.cancelFor(player);
            }
        }
    }
}