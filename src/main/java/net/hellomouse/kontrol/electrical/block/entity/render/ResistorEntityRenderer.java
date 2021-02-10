package net.hellomouse.kontrol.electrical.block.entity.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.hellomouse.kontrol.electrical.block.AbstractPolarizedElectricalBlock;
import net.hellomouse.kontrol.electrical.block.entity.ResistorBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ResistorEntityRenderer extends BlockEntityRenderer<ResistorBlockEntity> {
    public static final SpriteIdentifier LABEL_TEXTURE;
    private final ResistorEntityModel model = new ResistorEntityModel();

    private ScreenRenderer screen = new ScreenRenderer(MinecraftClient.getInstance().getTextureManager());

    static {
        LABEL_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("entity/enchanting_table_book"));
    }

    public ResistorEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);


    }

    @Override
    public void render(ResistorBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
//        matrices.translate(0.5, 0.32, 0.5);
//
//        Direction facing = blockEntity.getWorld().getBlockState(blockEntity.getPos()).get(Properties.HORIZONTAL_FACING);
//        if (facing == Direction.NORTH || facing == Direction.SOUTH)
//            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90.0f));
//
//        matrices.translate(-0.15, 0, -0.32);
//
//
//        TextRenderer textRenderer = this.dispatcher.getTextRenderer();
//        float l = 0.010416667F;
//        matrices.translate(0.0D, 0.3333333432674408D, 0.046666666865348816D);
//        matrices.scale(0.010416667F, -0.010416667F, 0.010416667F);
//        int m = 0xFFFFFF;
//        double d = 0.4D;
//        int n = (int)((double) NativeImage.getRed(m) * 0.4D);
//        int o = (int)((double)NativeImage.getGreen(m) * 0.4D);
//        int p = (int)((double)NativeImage.getBlue(m) * 0.4D);
//        int q = NativeImage.getAbgrColor(0, p, o, n);
//
//        q = 0xFF000000;
//
//        for(int s = 0; s < 4; ++s) {
//            Text orderedText = new LiteralText("hello!!");
//            if (orderedText != null) {
//                float t = (float)(-textRenderer.getWidth(orderedText) / 2);
//                textRenderer.draw(orderedText, t, (float)(s * 10 - 20), q, false, matrices.peek().getModel(), vertexConsumers, false, 0x31FF0000, light);
//            }
//        }
//

        int j = 4;
        //matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion((float)j * 360.0F / 8.0F));

        //matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180.0F));
        float h = 0.0078125F;
       // matrices.scale(0.0078125F, 0.0078125F, 0.0078125F);
       // matrices.translate(-64.0D, -64.0D, 0.0D);
        // MapState mapState = FilledMapItem.getOrCreateMapState(itemStack, itemFrameEntity.world);
       // matrices.translate(0.0D, 0.0D, -1.0D);

        matrices.scale(0.0078125F, 0.0078125F, 0.0078125F);
        // screen.draw(matrices, vertexConsumers, null, true, light);




        // matrices.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion(-10));
       // matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(80.0F));
        // float l = MathHelper.lerp(f, enchantingTableBlockEntity.pageAngle, enchantingTableBlockEntity.nextPageAngle);
        // float m = MathHelper.fractionalPart(l + 0.25F) * 1.6F - 0.3F;
        // float n = MathHelper.fractionalPart(l + 0.75F) * 1.6F - 0.3F;
        // float o = MathHelper.lerp(tickDelta, enchantingTableBlockEntity.pageTurningSpeed, enchantingTableBlockEntity.nextPageTurningSpeed);

        //VertexConsumer vertexConsumer = LABEL_TEXTURE.getVertexConsumer(vertexConsumers, RenderLayer::getText);
        //this.model.method_24184(matrices, vertexConsumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);

        matrices.pop();

    }
}