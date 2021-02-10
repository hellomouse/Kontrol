package net.hellomouse.kontrol.electrical.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hellomouse.kontrol.electrical.misc.ScopeState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

import java.util.HashMap;
import java.util.Map;


/**
 * A renderer for oscilloscope graphs. Creates, renders and updates textures
 * from ScopeState data
 * @author Bowserinator
 */
@Environment(EnvType.CLIENT)
public class ScopeRenderer implements AutoCloseable {
    private final TextureManager textureManager;
    private final Map<String, NativeImage> backgroundCache = new HashMap<>();
    private final Map<String, ScopeTexture> scopeTextures = new HashMap<>();

    public ScopeRenderer(TextureManager textureManager) {
        this.textureManager = textureManager;
    }

    public void updateTexture(ScopeState scopeState) {
        this.getScopeTexture(scopeState).updateTexture();
    }

    public void draw(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, ScopeState scopeState, int light) {
        this.getScopeTexture(scopeState).draw(matrixStack, vertexConsumerProvider, light);
    }

    public void close() {
        for (ScopeTexture texture : scopeTextures.values())
            texture.close();
        scopeTextures.clear();
    }

    /**
     * Get a scope texture for a given scopeState (via state ID). If there does not exist a texture
     * for the state in cache (scopeTextures) a texture will be created and assigned
     * to the state. Textures are separated by state IDs.
     * @param scopeState State
     * @return ScopeTexture
     */
    private ScopeTexture getScopeTexture(ScopeState scopeState) {
        ScopeTexture scopeTexture = scopeTextures.get(scopeState.getId());
        if (scopeTexture == null) {
            scopeTexture = new ScopeTexture(scopeState);
            scopeTexture.updateTexture();
            scopeTextures.put(scopeState.getId(), scopeTexture);
        }
        return scopeTexture;
    }

    /**
     * A texture that can be rendered
     * @author Bowserinator
     */
    @Environment(EnvType.CLIENT)
    class ScopeTexture implements AutoCloseable {
        private final NativeImageBackedTexture texture;
        private final RenderLayer renderlayer;
        private final ScopeState state;

        /**
         * Construct scope texture
         * @param state ScopeState this is bound to, same as the key in the scopeTextures map
         */
        private ScopeTexture(ScopeState state) {
            this.texture = new NativeImageBackedTexture(state.graphics.displayWidth, state.graphics.displayHeight, true);
            Identifier identifier = ScopeRenderer.this.textureManager.registerDynamicTexture(state.getId(), this.texture);
            this.renderlayer = RenderLayer.getText(identifier);
            this.state = state;
        }

        /**
         * Update the texture (re-draws the waveform based on new data
         * from the state)
         */
        private void updateTexture() {
            NativeImage image = texture.getImage();
            if (image == null) return;

            // Load background grid and axis from cache
            NativeImage bgImage = ScopeRenderer.this.backgroundCache.get(state.getId());
            if (bgImage == null) {
                bgImage = createBackgroundImage();
                ScopeRenderer.this.backgroundCache.put(state.getId(), bgImage);
            }
            image.copyFrom(bgImage);

            // We take readings from the end so the rightmost datapoint is always the newest
            // This is helpful if the screen is not large enough to render all the readings
            int x = state.graphics.padding[0] + state.graphics.displayWidthInternal - 2;
            int i = (state.getDataStart() - 1) % state.getMaxReadings();
            if (i < 0) i += state.getMaxReadings();

            // Previous point's y value, used to connect the dots between values
            // -1 means don't try to draw a line
            int prevVal = -1;

            do {
                // Reading's value is px offset from center axis
                int val = state.graphics.displayHeightInternal / 2 - state.getReadings()[i] + state.graphics.padding[2];

                // Val must be in (lowerY, upperY) (not inclusive)
                int lowerY = state.graphics.padding[2];
                int upperY = state.graphics.padding[2] + state.graphics.displayHeightInternal - 1;

                if (val > lowerY && val < upperY)
                    image.setPixelColor(x, val, state.graphics.waveFormColor);

                // Connect vertical line
                if (prevVal > -1) {
                    int startY = Math.min(prevVal, val);
                    if (startY < upperY && startY > lowerY) {
                        int height = Math.min(Math.abs(prevVal - val) + 1, state.graphics.displayHeightInternal - (startY - state.graphics.padding[2]) - 2);
                        image.fillRect(x, startY, 1, height, state.graphics.waveFormColor);
                    }
                }

                x--;
                if (x <= state.graphics.padding[0]) // Left padding
                    break;

                prevVal = val;
                i = (i - 1) % state.getMaxReadings();
                if (i < 0) i += state.getMaxReadings();
            } while (i != state.getDataStart());

            texture.upload();
        }

        /**
         * Get a NativeImage with the background elements (grid, border, etc...)
         * pre-drawn. Used to generate bg texture if not found in cache
         * @return NativeImage
         */
        private NativeImage createBackgroundImage() {
            NativeImage image = new NativeImage(state.graphics.displayWidth, state.graphics.displayHeight, true);
            drawBackground(image);
            return image;
        }

        /**
         * Renders background elements like grid, the X/Y axis, etc... onto a native image
         * @param image Native image to draw on, should be of size
         *              state.displayWidth, state.displayHeight
         */
        private void drawBackground(NativeImage image) {
            if (image == null) return;

            // Boundaries for box where grid is drawn
            // Includes left and top and up to but not including bottom and right
            // For example, if yBottom = 5 and xRight = 7, the bottom and right borders
            // would be drawn at y=4 and x=6 respectively.
            int xLeft   = state.graphics.padding[0];
            int xRight  = state.graphics.padding[0] + state.graphics.displayWidthInternal;
            int yTop    = state.graphics.padding[2];
            int yBottom = state.graphics.padding[2] + state.graphics.displayHeightInternal;

            // Fill background
            image.fillRect(0, 0, state.graphics.displayWidth, state.graphics.displayHeight, state.graphics.backgroundColor);

            // Background grid
            for (int multiplier = 1; multiplier < state.graphics.yDivisions; multiplier++)
                image.fillRect(xLeft, yTop + Math.round((float) state.graphics.displayHeightInternal / state.graphics.yDivisions * multiplier),
                        state.graphics.displayWidthInternal, 1, state.graphics.gridColor);

            for (int multiplier = 1; multiplier < state.graphics.xDivisions; multiplier++)
                image.fillRect(xLeft + Math.round((float) state.graphics.displayWidthInternal / state.graphics.xDivisions * multiplier), yTop,
                        1, state.graphics.displayHeightInternal, state.graphics.gridColor);

            // Time axis
            final int timeY = yTop + state.graphics.displayHeightInternal / 2;
            for (int x = xLeft; x < xRight; x += 2) {

                image.setPixelColor(x, timeY, state.graphics.axisColor);
            }

            // Data axis
            final int dataX = xLeft + state.graphics.displayWidthInternal / 2;

            for (int y = yTop; y < yBottom; y += 2) {
                image.setPixelColor(dataX, y, state.graphics.axisColor);
            }

            // Surrounding border
            for (int x = xLeft; x < xRight; x++) {
                image.setPixelColor(x, yTop, state.graphics.borderColor);
                image.setPixelColor(x, yBottom - 1, state.graphics.borderColor);
            }
            for (int y = yTop; y < yBottom; y++) {
                image.setPixelColor(xLeft, y, state.graphics.borderColor);
                image.setPixelColor(xRight - 1, y, state.graphics.borderColor);
            }
        }

        /**
         * Draw the texture
         * @param matrixStack matrices
         * @param vertexConsumerProvider vertexConsumerProvider
         * @param light Light level
         */
        private void draw(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
            Matrix4f matrix4f = matrixStack.peek().getModel();
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(this.renderlayer);
            vertexConsumer.vertex(matrix4f, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).texture(0.0F, 0.0F).light(light).next();
            vertexConsumer.vertex(matrix4f, 0.0F, state.graphics.displayHeight, -0.01F).color(255, 255, 255, 255).texture(0.0F, 1.0F).light(light).next();
            vertexConsumer.vertex(matrix4f, state.graphics.displayWidth, state.graphics.displayHeight, -0.01F).color(255, 255, 255, 255).texture(1.0F, 1.0F).light(light).next();
            vertexConsumer.vertex(matrix4f, state.graphics.displayWidth, 0.0F, -0.01F).color(255, 255, 255, 255).texture(1.0F, 0.0F).light(light).next();
        }

        /** Close the texture */
        public void close() {
            this.texture.close();
        }
    }
}