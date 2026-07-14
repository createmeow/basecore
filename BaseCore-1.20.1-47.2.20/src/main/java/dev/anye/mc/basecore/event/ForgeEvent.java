package dev.anye.mc.basecore.event;

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
import dev.anye.mc.basecore.block.entity.ToDamageBlockEntity;
import dev.anye.mc.basecore.block.entity.basecore.BaseCoreBlockEntity;
import dev.anye.mc.basecore.command.BasecoreCommands;
import dev.anye.mc.basecore.effect.EffectRegister;
import dev.anye.mc.basecore.item.ItemRegister;
import dev.anye.mc.basecore.item.module.BaseModuleItem;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mod.EventBusSubscriber(modid = BaseCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvent {
    private static final Logger LOGGER = LogUtils.getLogger();
    @SubscribeEvent
    public static void onChunk(ChunkEvent.Load event){
        if (event.getLevel() instanceof ServerLevel serverLevel){
            event.getChunk().getBlockEntitiesPos().forEach(blockPos -> {
                event.getChunk().getBlockEntity(blockPos,BlockEntityRegister.BASECORE.get()).ifPresent(BasecoreServerHelper::addEntity);
                /*
                if (event.getChunk().getBlockEntity(blockPos,BlockEntityRegister.BASECORE.get()).isPresent()) {
                    BasecoreServerHelper.add(blockPos);
                }

                 */
            });
            BasecoreServerHelper.upToClient();
        }
    }
    @SubscribeEvent
    public static void onChunk(ChunkEvent.Unload event){
        if (event.getLevel() instanceof ServerLevel serverLevel){
            event.getChunk().getBlockEntitiesPos().forEach(blockPos -> {
                event.getChunk().getBlockEntity(blockPos,BlockEntityRegister.BASECORE.get()).ifPresent(BasecoreServerHelper::delEntity);
                /*
                if (event.getChunk().getBlockEntity(blockPos,BlockEntityRegister.BASECORE.get()).isPresent()) {
                    BasecoreServerHelper.del(blockPos);
                }

                 */
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
                        /*
                        .then(Commands.literal("list")
                                .executes(BasecoreCommands::list)
                        )

                         */
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
    /*
    @SubscribeEvent
    public static void onDestroyBlock(LivingDestroyBlockEvent event){
        System.out.println("=====");
        System.out.println(event.getPos().toString());
    }

     */

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
                    blockEntity.damage(20);
                    if (blockEntity instanceof BaseCoreBlockEntity baseCoreBlockEntity && baseCoreBlockEntity.getModuleLvl(ItemRegister.ThornsModule.get()) > 0){
                        serverPlayer.hurt(serverPlayer.damageSources().fellOutOfWorld(),3);
                    }
                    event.setCanceled(true);
                    if (blockEntity.getHealth() <= 0){
                        serverPlayer.level().playSound(
                                null,
                                blockEntity.getBlockPos().getX() + 0.5,
                                blockEntity.getBlockPos().getY() + 0.5,
                                blockEntity.getBlockPos().getZ() + 0.5,
                                SoundEvents.GENERIC_EXPLODE,
                                SoundSource.BLOCKS,
                                1.0F,
                                serverPlayer.level().getRandom().nextFloat() * 0.4F + 0.8F
                        );

                        if (serverPlayer.level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(
                                    ParticleTypes.EXPLOSION_EMITTER,
                                    blockEntity.getBlockPos().getX() + 0.5,
                                    blockEntity.getBlockPos().getY() + 0.5,
                                    blockEntity.getBlockPos().getZ() + 0.5,
                                    1,
                                    0.0,
                                    0.0,
                                    0.0,
                                    0.0
                            );
                        }

                        DamageBlockEntity damageBlockEntity = new DamageBlockEntity(blockEntity.getBlockPos(), BlockRegister.Damage.get().defaultBlockState());
                        damageBlockEntity.setItems(blockEntity.getItems());
                        serverPlayer.level().setBlock(blockEntity.getBlockPos(), BlockRegister.Damage.get().defaultBlockState(),Block.UPDATE_ALL);
                        serverPlayer.level().setBlockEntity(damageBlockEntity);
                        if (blockEntity instanceof BaseCoreBlockEntity baseCoreBlockEntity)
                            BasecoreServerHelper.delEntityAndSend(baseCoreBlockEntity);
                    }
                }else {
                    if (blockEntity instanceof BaseCoreBlockEntity baseCoreBlockEntity)
                        BasecoreServerHelper.delEntityAndSend(baseCoreBlockEntity);
                }
            }
        }
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
                            baseCoreBlockEntity.setOwner(serverPlayer.getUUID());
                            BasecoreServerHelper.addEntityAndSend(baseCoreBlockEntity);
                        });
                        //BasecoreServerHelper.addAndSend(event.getPos());
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
    public static void onExp(ExplosionEvent event){
        if (event.getLevel().isClientSide()) return;
        HashMap<Player, Vec3> players = new HashMap<>(event.getExplosion().getHitPlayers());
        players.forEach((player, vec3) -> {
            BaseCoreBlockEntity baseCoreBlockEntity = BasecoreServerHelper.getBasecore((ServerPlayer) player);
            if (baseCoreBlockEntity != null && baseCoreBlockEntity.getModuleLvl(ItemRegister.ExpDefModule.get()) > 0 && baseCoreBlockEntity.canUse(player.getUUID())) event.getExplosion().getHitPlayers().remove(player);
        });
        List<BlockPos> blockPoss = new ArrayList<>(event.getExplosion().getToBlow());
        blockPoss.forEach(blockPos -> {
            BaseCoreBlockEntity baseCoreBlockEntity = BasecoreServerHelper.getBasecore(event.getLevel(),blockPos.getCenter());
            if (baseCoreBlockEntity != null && baseCoreBlockEntity.getModuleLvl(ItemRegister.ExpDefModule.get()) > 0){
                event.getExplosion().getToBlow().remove(blockPos);
            }
            /*
            else {
                BasecoreServerHelper.delEntityAndSend(baseCoreBlockEntity);
            }

             */
        });
    }
    /*
    @SubscribeEvent
    public static void onNet(NetworkEvent event){
        event.getSource().get().enqueueWork(()->{
            ClientLevel clientLevel = Minecraft.getInstance().level;
            if (clientLevel != null){
                BasecoreBlockNet.Data.forEach((blockPos, compoundTag) -> {
                    if (clientLevel.hasChunkAt(blockPos)) {
                        System.out.println("-> "+blockPos.toString());
                        System.out.println("-> "+compoundTag.toString());
                        BlockEntity blockEntity = clientLevel.getChunkAt(blockPos).getBlockEntity(blockPos);
                        clientLevel.getChunkAt(blockPos).getBlockEntities().forEach((blockPos1, blockEntity1) -> System.out.println("-> "+blockPos1.toString() +"  ->"+blockEntity1.getBlockPos().toString()));

                        if (blockEntity instanceof BaseCoreBlockEntity baseCoreBlockEntity){
                            System.out.println("-> 12");
                            baseCoreBlockEntity.handlePacket(compoundTag);
                        }
                    }
                });
            }
        });
    }

     */
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
}
