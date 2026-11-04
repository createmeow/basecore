package dev.anye.mc.basecore.block.entity.basecore;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class BasecoreBlockEntityRender implements BlockEntityRenderer<BaseCoreBlockEntity> {
    public BasecoreBlockEntityRender(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BaseCoreBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        Level level = pBlockEntity.getLevel();
        if (level == null) return;
        BlockPos pPos = pBlockEntity.getBlockPos();

        int range = pBlockEntity.getRange();
        if (range <= 0) return;

        boolean interference = pBlockEntity.getEntityData().getInterferenceTime() > 0;
        // 苏式基地计算机的范围框固定为黄色；普通基地核心在受干扰时变红，否则为绿色
        int color = pBlockEntity instanceof dev.anye.mc.basecore.block.soviet_computer.SovietComputerBlockEntity
                ? 0xffffcc00
                : (interference ? 0xffff0000 : 0xff00ff00);

        VertexConsumer consumer = pBufferSource.getBuffer(RenderType.LINES);
        PoseStack.Pose poseEntry = pPoseStack.last();
        Matrix4f pose = poseEntry.pose();

        AABB aabb = new AABB(pBlockEntity.getBaseCenter()).inflate(range);
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
        // BlockEntityRenderer 的位姿原点位于方块最小角（不是方块中心），
        // 这里必须减去角点坐标，否则整个范围框会整体偏移半格。
        Vec3 origin = new Vec3(pPos.getX(), pPos.getY(), pPos.getZ());
        for (int idx = 0; idx < indices.length; idx += 2) {
            Vec3 v1 = corners[indices[idx]];
            Vec3 v2 = corners[indices[idx+1]];
            consumer.addVertex(pose, (float)(v1.x - origin.x), (float)(v1.y - origin.y), (float)(v1.z - origin.z))
                .setColor(color).setNormal(poseEntry, 0, 1, 0);
            consumer.addVertex(pose, (float)(v2.x - origin.x), (float)(v2.y - origin.y), (float)(v2.z - origin.z))
                .setColor(color).setNormal(poseEntry, 0, 1, 0);
        }
    }
}