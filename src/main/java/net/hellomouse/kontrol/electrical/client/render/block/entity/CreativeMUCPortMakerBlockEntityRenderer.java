package net.hellomouse.kontrol.electrical.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hellomouse.kontrol.electrical.block.microcontroller.entity.CreativeMUCPortMakerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class CreativeMUCPortMakerBlockEntityRenderer extends BlockEntityRenderer<CreativeMUCPortMakerBlockEntity> {
    public CreativeMUCPortMakerBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(CreativeMUCPortMakerBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (MinecraftClient.getInstance().player.isCreativeLevelTwoOp() || MinecraftClient.getInstance().player.isSpectator()) {
            int[] coords = blockEntity.getBoundingCoordinates();
            int x1 = coords[0], y1 = coords[1], z1 = coords[2], x2 = coords[3], y2 = coords[4], z2 = coords[5];

            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
            WorldRenderer.drawBox(matrices, vertexConsumer, x1, y1, z1, x2, y2, z2, 0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
        }
    }

    @Override
    public boolean rendersOutsideBoundingBox(CreativeMUCPortMakerBlockEntity blockEntity) { return true; }
}
