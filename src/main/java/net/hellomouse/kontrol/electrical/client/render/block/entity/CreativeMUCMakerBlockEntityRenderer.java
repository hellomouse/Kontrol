package net.hellomouse.kontrol.electrical.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hellomouse.kontrol.electrical.block.microcontroller.entity.CreativeMUCMakerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Pair;

@Environment(EnvType.CLIENT)
public class CreativeMUCMakerBlockEntityRenderer extends BlockEntityRenderer<CreativeMUCMakerBlockEntity> {
    public CreativeMUCMakerBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(CreativeMUCMakerBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (MinecraftClient.getInstance().player.isCreativeLevelTwoOp() || MinecraftClient.getInstance().player.isSpectator()) {
            Pair<Integer, Integer> boxSize = blockEntity.getBoundingBoxSize();
            int xSize = boxSize.getLeft();
            int zSize = boxSize.getRight();

            int x1 = 0, y1 = 0, z1 = 0;
            int x2 = 0, y2 = 1, z2 = 0;

            BlockRotation rotation = blockEntity.getRotation();

            // Switch statement won't run with BlockRotation enum
            // if you can figure it out pls fix
            if (rotation == BlockRotation.NONE) {
                z1 = 1;
                x2 = xSize;
                z2 = 1 + zSize;
            }
            else if (rotation == BlockRotation.CLOCKWISE_90) {
                x2 = -zSize;
                z2 = xSize;
            }
            else if (rotation == BlockRotation.CLOCKWISE_180) {
                x1 = 1;
                x2 = 1 - xSize;
                z2 = -zSize;
            }
            else {
                x1 = z1 = 1;
                x2 = zSize + 1;
                z2 = 1 - xSize;
            }

            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
            WorldRenderer.drawBox(matrices, vertexConsumer, x1, y1, z1, x2, y2, z2, 0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
        }
    }

    @Override
    public boolean rendersOutsideBoundingBox(CreativeMUCMakerBlockEntity blockEntity) { return true; }
}
