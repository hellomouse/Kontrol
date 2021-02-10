package net.hellomouse.kontrol.electrical.block.entity.render;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ResistorEntityModel extends Model {
    private final ModelPart labelBack = (new ModelPart(8, 4, 0, 0)).addCuboid(0.0f, 0.0f, 0.0f, 8.0f, 4.0f, 0.005f);
    private final List<ModelPart> parts;

    public ResistorEntityModel() {
        super(RenderLayer::getEntitySolid);
        this.parts = ImmutableList.of(this.labelBack);
    }

    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
       //  this.method_24184(matrices, vertices, light, overlay, red, green, blue, alpha);

        Matrix4f matrix4f = matrices.peek().getModel();
        int n = 1;
        int o = 0;
        int p = 0;
        vertices.vertex(matrix4f, 0.0f, 0.0f, 0.0f).color(n, o, p, 1.0F).next();
        vertices.vertex(matrix4f, 0.0f, 1.0f, 0.0f).color(n, o, p, 1.0F).next();
        vertices.vertex(matrix4f, 0.0f, 1.0f, 1.0f).color(n, o, p, 1.0F).next();
        vertices.vertex(matrix4f, 0.0f, 0.0f, 1.0f).color(n, o, p, 1.0F).next();
    }

    public void method_24184(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.parts.forEach((modelPart) -> {
            modelPart.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        });
    }
}
