package net.hellomouse.kontrol.electrical.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hellomouse.kontrol.electrical.block.entity.CapacitorBlockEntity;
import net.hellomouse.kontrol.electrical.block.entity.ResistorBlockEntity;
import net.hellomouse.kontrol.util.FormatUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class CapacitorEntityRenderer extends BlockEntityRenderer<CapacitorBlockEntity> {
    public static final SpriteIdentifier LABEL_TEXTURE;

    static {
        LABEL_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("entity/enchanting_table_book"));
    }

    public CapacitorEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(CapacitorBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5, 0.0, 0.5);
//
         Direction facing = blockEntity.getWorld().getBlockState(blockEntity.getPos()).get(Properties.HORIZONTAL_FACING);
        if (facing == Direction.NORTH || facing == Direction.SOUTH)
            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90.0f));
//
        matrices.translate(-0.5, 0, -0.5);


        TextRenderer textRenderer = this.dispatcher.getTextRenderer();
        float scale = 0.008f; // 0.010416667F;
        matrices.translate(0.1D, 0.68D, 0.7501);


        matrices.scale(scale, -scale, scale);
        int m = 0xFFFFFF;
        int q = 0xffc7bdeb;

        String orderedText = FormatUtil.SIFormat(blockEntity.getCapacitance(), 0, "F");
        Text f = new LiteralText(orderedText);

        textRenderer.draw(f, 0.0f, 0.15f, q, false, matrices.peek().getModel(), vertexConsumers, false, 0, light - 2);

        matrices.scale(1 / scale, 1 / -scale, 1 / scale);
        matrices.translate(0.4, 0.0, -0.2501);
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0f));
        matrices.translate(-0.4, 0.0, 0.2501);
        matrices.scale(scale, -scale, scale);

        textRenderer.draw(f, 0.0f, 0.15f, q, false, matrices.peek().getModel(), vertexConsumers, false, 0, light - 2);



        // matrices.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion(-10));
        // matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(80.0F));
        // float l = MathHelper.lerp(f, enchantingTableBlockEntity.pageAngle, enchantingTableBlockEntity.nextPageAngle);
        // float m = MathHelper.fractionalPart(l + 0.25F) * 1.6F - 0.3F;
        // float n = MathHelper.fractionalPart(l + 0.75F) * 1.6F - 0.3F;
        // float o = MathHelper.lerp(tickDelta, enchantingTableBlockEntity.pageTurningSpeed, enchantingTableBlockEntity.nextPageTurningSpeed);


        matrices.pop();
    }
}