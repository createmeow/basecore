package dev.anye.mc.basecore.block.nothing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.anye.mc.basecore.BaseCore;
import dev.anye.mc.basecore.item.ItemRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = BaseCore.MOD_ID, value = Dist.CLIENT)
public class NothingBlockRender {

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Player player = mc.player;
        if (player == null) return;
        if (!isHoldingBasecoreItem(player)) return;

        Vec3 cam = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        PoseStack.Pose poseEntry = poseStack.last();
        Matrix4f pose = poseEntry.pose();

        MultiBufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.LINES);

        ChunkPos playerChunk = new ChunkPos(player.blockPosition());
        int viewDist = mc.options.renderDistance().get();

        for (int cx = playerChunk.x - viewDist; cx <= playerChunk.x + viewDist; cx++) {
            for (int cz = playerChunk.z - viewDist; cz <= playerChunk.z + viewDist; cz++) {
                var chunk = mc.level.getChunkSource().getChunk(cx, cz, false);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be.getBlockState().getBlock() instanceof NothingBlock) {
                        double x = be.getBlockPos().getX();
                        double y = be.getBlockPos().getY();
                        double z = be.getBlockPos().getZ();
                        Vec3 center = be.getBlockPos().getCenter();

                        AABB aabb = new AABB(be.getBlockPos());
                        Vec3[] corners = {
                            new Vec3(aabb.minX, aabb.minY, aabb.minZ),
                            new Vec3(aabb.maxX, aabb.minY, aabb.minZ),
                            new Vec3(aabb.maxX, aabb.minY, aabb.maxZ),
                            new Vec3(aabb.minX, aabb.minY, aabb.maxZ),
                            new Vec3(aabb.minX, aabb.maxY, aabb.minZ),
                            new Vec3(aabb.maxX, aabb.maxY, aabb.minZ),
                            new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ),
                            new Vec3(aabb.minX, aabb.maxY, aabb.maxZ),
                        };
                        int[] indices = {0,1, 1,2, 2,3, 3,0, 4,5, 5,6, 6,7, 7,4, 0,4, 1,5, 2,6, 3,7};
                        int color = 0x6600FF00;
                        for (int idx = 0; idx < indices.length; idx += 2) {
                            Vec3 v1 = corners[indices[idx]];
                            Vec3 v2 = corners[indices[idx+1]];
                            consumer.addVertex(pose, (float)(v1.x - center.x), (float)(v1.y - center.y), (float)(v1.z - center.z))
                                    .setColor(color).setNormal(poseEntry, 0, 1, 0);
                            consumer.addVertex(pose, (float)(v2.x - center.x), (float)(v2.y - center.y), (float)(v2.z - center.z))
                                    .setColor(color).setNormal(poseEntry, 0, 1, 0);
                        }
                    }
                }
            }
        }
    }

    private static boolean isHoldingBasecoreItem(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return mainHand.is(ItemRegister.Basecore.get()) || offHand.is(ItemRegister.Basecore.get());
    }
}