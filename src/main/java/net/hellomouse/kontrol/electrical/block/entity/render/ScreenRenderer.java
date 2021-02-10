package net.hellomouse.kontrol.electrical.block.entity.render;

import com.google.common.collect.Maps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.MaterialColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ScreenRenderer implements AutoCloseable {
    private static final Identifier MAP_ICONS_TEXTURE = new Identifier("textures/map/map_icons.png");
    private static final RenderLayer field_21688;
    private final TextureManager textureManager;
    private final Map<String, net.hellomouse.kontrol.electrical.block.entity.render.ScreenRenderer.MapTexture> mapTextures = Maps.newHashMap();

    public ScreenRenderer(TextureManager textureManager) {
        this.textureManager = textureManager;
    }

    public void updateTexture(MapState mapState) {
        this.getMapTexture(mapState).updateTexture();
    }

    public void draw(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, MapState mapState, boolean bl, int i) {
        this.getMapTexture(mapState).draw(matrixStack, vertexConsumerProvider, bl, i);
    }

    private net.hellomouse.kontrol.electrical.block.entity.render.ScreenRenderer.MapTexture getMapTexture(MapState mapState) {
        net.hellomouse.kontrol.electrical.block.entity.render.ScreenRenderer.MapTexture mapTexture = null;
        if (mapState == null) {
            mapTexture = new net.hellomouse.kontrol.electrical.block.entity.render.ScreenRenderer.MapTexture();
            this.mapTextures.put("0", mapTexture);
            for (int i = 0; i < 128 * 128; i++)
                mapTexture.colors[i] = 0xff000000 + ((i) * (0xffffff / 0x4000)  % (0xffffff));
            mapTexture.updateTexture();
        }

        return mapTexture;
    }

    @Nullable
    public net.hellomouse.kontrol.electrical.block.entity.render.ScreenRenderer.MapTexture getTexture(String string) {
        return (net.hellomouse.kontrol.electrical.block.entity.render.ScreenRenderer.MapTexture)this.mapTextures.get(string);
    }

    public void clearStateTextures() {
        Iterator var1 = this.mapTextures.values().iterator();

        while(var1.hasNext()) {
            net.hellomouse.kontrol.electrical.block.entity.render.ScreenRenderer.MapTexture mapTexture = (net.hellomouse.kontrol.electrical.block.entity.render.ScreenRenderer.MapTexture)var1.next();
            mapTexture.close();
        }

        this.mapTextures.clear();
    }


    public void close() {
        this.clearStateTextures();
    }

    static {
        field_21688 = RenderLayer.getText(MAP_ICONS_TEXTURE);
    }

    @Environment(EnvType.CLIENT)
    class MapTexture implements AutoCloseable {
        public int[] colors = new int[128 * 128];
        private final NativeImageBackedTexture texture;
        private final RenderLayer renderlayer;

        private MapTexture() {
            this.texture = new NativeImageBackedTexture(128, 128, true);
            Identifier identifier = net.hellomouse.kontrol.electrical.block.entity.render.ScreenRenderer.this.textureManager.registerDynamicTexture("test/test", this.texture); // TODO: Math.random was mapState.getID
            this.renderlayer = RenderLayer.getText(identifier);
        }

        private void updateTexture() {
            for(int i = 0; i < 128; ++i) {
                for(int j = 0; j < 128; ++j) {
                    int k = j + i * 128;
                    int l = this.colors[k];
                    this.texture.getImage().setPixelColor(j, i, l);
                }
            }

            this.texture.upload();
        }

        private void draw(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, boolean bl, int i) {
            float f = 0.0F;
            Matrix4f matrix4f = matrixStack.peek().getModel();
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(this.renderlayer);
            vertexConsumer.vertex(matrix4f, 0.0F, 128.0F, -0.01F).color(255, 255, 255, 255).texture(0.0F, 1.0F).light(i).next();
            vertexConsumer.vertex(matrix4f, 128.0F, 128.0F, -0.01F).color(255, 255, 255, 255).texture(1.0F, 1.0F).light(i).next();
            vertexConsumer.vertex(matrix4f, 128.0F, 0.0F, -0.01F).color(255, 255, 255, 255).texture(1.0F, 0.0F).light(i).next();
            vertexConsumer.vertex(matrix4f, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).texture(0.0F, 0.0F).light(i).next();
            int l = 0;
           // Iterator var11 = this.mapState.icons.values().iterator();
//
//            while(true) {
//                MapIcon mapIcon;
//                do {
//                    if (!var11.hasNext()) {
//                        return;
//                    }
//
//                    mapIcon = (MapIcon)var11.next();
//                } while(bl && !mapIcon.isAlwaysRendered());
//
//                matrixStack.push();
//                matrixStack.translate((double)(0.0F + (float)mapIcon.getX() / 2.0F + 64.0F), (double)(0.0F + (float)mapIcon.getZ() / 2.0F + 64.0F), -0.019999999552965164D);
//                matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion((float)(mapIcon.getRotation() * 360) / 16.0F));
//                matrixStack.scale(4.0F, 4.0F, 3.0F);
//                matrixStack.translate(-0.125D, 0.125D, 0.0D);
//                byte b = mapIcon.getTypeId();
//                float g = (float)(b % 16 + 0) / 16.0F;
//                float h = (float)(b / 16 + 0) / 16.0F;
//                float m = (float)(b % 16 + 1) / 16.0F;
//                float n = (float)(b / 16 + 1) / 16.0F;
//                Matrix4f matrix4f2 = matrixStack.peek().getModel();
//                float o = -0.001F;
//                VertexConsumer vertexConsumer2 = vertexConsumerProvider.getBuffer(net.hellomouse.kontrol.electrical.block.entity.render.ScreenRenderer.field_21688);
//                vertexConsumer2.vertex(matrix4f2, -1.0F, 1.0F, (float)l * -0.001F).color(255, 255, 255, 255).texture(g, h).light(i).next();
//                vertexConsumer2.vertex(matrix4f2, 1.0F, 1.0F, (float)l * -0.001F).color(255, 255, 255, 255).texture(m, h).light(i).next();
//                vertexConsumer2.vertex(matrix4f2, 1.0F, -1.0F, (float)l * -0.001F).color(255, 255, 255, 255).texture(m, n).light(i).next();
//                vertexConsumer2.vertex(matrix4f2, -1.0F, -1.0F, (float)l * -0.001F).color(255, 255, 255, 255).texture(g, n).light(i).next();
//                matrixStack.pop();
//                if (mapIcon.getText() != null) {
//                    TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
//                    Text text = mapIcon.getText();
//                    float p = (float)textRenderer.getWidth(text);
//                    float var10000 = 25.0F / p;
//                    textRenderer.getClass();
//                    float q = MathHelper.clamp(var10000, 0.0F, 6.0F / 9.0F);
//                    matrixStack.push();
//                    matrixStack.translate((double)(0.0F + (float)mapIcon.getX() / 2.0F + 64.0F - p * q / 2.0F), (double)(0.0F + (float)mapIcon.getZ() / 2.0F + 64.0F + 4.0F), -0.02500000037252903D);
//                    matrixStack.scale(q, q, 1.0F);
//                    matrixStack.translate(0.0D, 0.0D, -0.10000000149011612D);
//                    textRenderer.draw(text, 0.0F, 0.0F, -1, false, matrixStack.peek().getModel(), vertexConsumerProvider, false, -2147483648, i);
//                    matrixStack.pop();
//                }

                //++l;
           // }
        }

        public void close() {
            this.texture.close();
        }
    }
}
