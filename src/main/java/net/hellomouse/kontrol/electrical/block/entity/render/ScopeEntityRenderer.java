package net.hellomouse.kontrol.electrical.block.entity.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hellomouse.kontrol.electrical.block.entity.ScopeBlockEntity;
import net.hellomouse.kontrol.electrical.client.render.ScopeRenderer;
import net.hellomouse.kontrol.electrical.misc.ScopeState;
import net.hellomouse.kontrol.util.FormatUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;


@Environment(EnvType.CLIENT)
public class ScopeEntityRenderer extends BlockEntityRenderer<ScopeBlockEntity> {
    private final ScopeRenderer renderer = new ScopeRenderer(MinecraftClient.getInstance().getTextureManager());

    public ScopeEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(ScopeBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ScopeState state = blockEntity.getScopeState();

        if (state != null && state.getMaxReadings() != 0) {
            matrices.push();

            // TODO: scale and translate depending on internal size
            // TODO: customizable translations for blockEntity

            matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180.0f));
            matrices.scale(0.0078125f, 0.0078125f, 0.0078125f); // Scale by 1/128, the size of the texture
            matrices.translate(-128, -128, -0.1);

            Direction facing = blockEntity.getWorld().getBlockState(blockEntity.getPos()).get(Properties.HORIZONTAL_FACING);
            switch(facing) {
                case SOUTH: {
                    matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90.0f));
                    matrices.translate(-128, 0, -0.1);
                    break;
                }
                case NORTH: {
                    matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(270.0f));
                    matrices.translate(0, 0, -128.1);
                    break;
                }
                case WEST: {
                    matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0f));
                    matrices.translate(-128, 0, -128.2);
                    break;
                }
            }

            if (state.requiresUpdate()) {
                renderer.updateTexture(state);
                state.finishedUpdating();
            }
            renderer.draw(matrices, vertexConsumers, state, light);


            TextRenderer textRenderer = this.dispatcher.getTextRenderer();
            matrices.translate(8, 8, -1);
            matrices.scale(0.5f, 0.5f, 0.5f);

            int q = 0xffffffff;
            // Each px = 1 tick / state.getTimeScale()

            float unitsPerDiv = ScopeState.DEFAULT_SCALE_HEIGHT / state.getYScale() / state.graphics.yDivisions;
            float ticksPerDiv = (float)state.graphics.displayWidthInternal / state.graphics.xDivisions * 0.05f / state.getTimeScale();

            Text f = new LiteralText(FormatUtil.SIFormat(unitsPerDiv, 2, state.getUnit()) + "/  | "
                 + String.format("%.2f", ticksPerDiv) + " s/");
            // TODO: time in SI format too

            textRenderer.draw(f, 0.0f, 0.0f, q, false, matrices.peek().getModel(), vertexConsumers, false, 0xaa000000, light - 2);

            matrices.pop();
        }
    }
}
