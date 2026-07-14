package dev.anye.mc.basecore.block.entity.basecore;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class BasecoreBlockEntityRender implements BlockEntityRenderer<BaseCoreBlockEntity> {
    public BasecoreBlockEntityRender(BlockEntityRendererProvider.Context context){
    }
    @Override
    public void render(@NotNull BaseCoreBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 0.5, 0.5);
        int range = pBlockEntity.getRange();
        float extent = (float) range;
        VertexConsumer consumer = pBuffer.getBuffer(RenderType.lines());
        Matrix4f matrix = pPoseStack.last().pose();
        Matrix3f normalMatrix = pPoseStack.last().normal();
        int r = 0;
        int g = 255;
        int b = 0;
        int a = 255;
        this.drawLineSegment(consumer, matrix,normalMatrix,
                -extent, -extent, -extent,  extent, -extent, -extent,  r, g, b, a);
        this.drawLineSegment(consumer, matrix,normalMatrix,
                -extent,  extent, -extent,  extent,  extent, -extent,  r, g, b, a);
        this.drawLineSegment(consumer, matrix,normalMatrix,
                -extent, -extent,  extent,  extent, -extent,  extent,  r, g, b, a);
        this.drawLineSegment(consumer, matrix,normalMatrix,
                -extent,  extent,  extent,  extent,  extent,  extent,  r, g, b, a);

        this.drawLineSegment(consumer, matrix,normalMatrix,
                -extent, -extent, -extent, -extent,  extent, -extent,  r, g, b, a);
        this.drawLineSegment(consumer, matrix,normalMatrix,
                extent, -extent, -extent,  extent,  extent, -extent,  r, g, b, a);
        this.drawLineSegment(consumer, matrix,normalMatrix,
                -extent, -extent,  extent, -extent,  extent,  extent,  r, g, b, a);
        this.drawLineSegment(consumer, matrix,normalMatrix,
                extent, -extent,  extent,  extent,  extent,  extent,  r, g, b, a);

        this.drawLineSegment(consumer, matrix,normalMatrix,
                -extent, -extent, -extent, -extent, -extent,  extent,  r, g, b, a);
        this.drawLineSegment(consumer, matrix,normalMatrix,
                extent, -extent, -extent,  extent, -extent,  extent,  r, g, b, a);
        this.drawLineSegment(consumer, matrix,normalMatrix,
                -extent,  extent, -extent, -extent,  extent,  extent,  r, g, b, a);
        this.drawLineSegment(consumer, matrix,normalMatrix,
                extent,  extent, -extent,  extent,  extent,  extent,  r, g, b, a);

        pPoseStack.popPose();
    }
    private void drawLineSegment(VertexConsumer consumer, Matrix4f matrix,Matrix3f normalMatrix,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2,
                                 int r, int g, int b, int a
    ) {
        float nx = 0.0f;
        float ny = 1.0f;
        float nz = 0.0f;

        consumer.vertex(matrix, x1, y1, z1)
                .color(r, g, b, a)
                .normal(normalMatrix, nx, ny, nz)
                .endVertex();

        consumer.vertex(matrix, x2, y2, z2)
                .color(r, g, b, a)
                .normal(normalMatrix, nx, ny, nz)
                .endVertex();
    }
}
