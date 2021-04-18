package net.hellomouse.kontrol.electrical.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.hellomouse.kontrol.electrical.microcontroller.MUCStatic;
import net.hellomouse.kontrol.registry.block.MUCBlockRegistry;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;

/**
 * MUC Maker screen
 * @author Bowserinator
 */
public class CreativeMUCMakerScreen extends HandledScreen<ScreenHandler> {
    private static final TranslatableText TEXT_ROTATION  = new TranslatableText("muc_maker.rotation");

    private ButtonWidget buttonDone;
    private ButtonWidget buttonCancel;
    private ButtonWidget buttonRotate0;
    private ButtonWidget buttonRotate90;
    private ButtonWidget buttonRotate180;
    private ButtonWidget buttonRotate270;

    private ButtonWidget buttonPrev;
    private ButtonWidget buttonNext;

    private BlockRotation rotation = BlockRotation.NONE;
    private int currentMUC = 0;
    private final BlockPos pos;

    public CreativeMUCMakerScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        if (handler instanceof CreativeMUCMakerScreenHandler) {
            CreativeMUCMakerScreenHandler screenHandler = ((CreativeMUCMakerScreenHandler)handler);
            pos = screenHandler.getPos();
            currentMUC = screenHandler.getCurrentMUC();
            rotation = BlockRotation.values()[screenHandler.getRotationIndex()];
        }
        else {
            pos = null;
            this.close();
        }
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        if (client == null) return;

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        MUCStatic.MUCData currentMUCPair = MUCStatic.CHOICES.get(currentMUC);
        client.getTextureManager().bindTexture(currentMUCPair.texture);

        drawTexture(matrices,this.width / 2 - 4 - 139, 50, this.getZOffset(), 0, 0, 128, 128, 128, 128);
        drawCenteredText(matrices, this.textRenderer, currentMUCPair.name, this.width / 2 - 80, 40, 0xFFFFFF);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.drawBackground(matrices, delta, mouseX, mouseY);

        final int subColor = 0xA0A0A0;

        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);

        this.buttonCancel.render(matrices, mouseX, mouseY, delta);
        this.buttonDone.render(matrices, mouseX, mouseY, delta);

        drawTextWithShadow(matrices, this.textRenderer, TEXT_ROTATION, this.width / 2 + 4, 168, subColor);
        this.buttonRotate0.render(matrices, mouseX, mouseY, delta);
        this.buttonRotate90.render(matrices, mouseX, mouseY, delta);
        this.buttonRotate180.render(matrices, mouseX, mouseY, delta);
        this.buttonRotate270.render(matrices, mouseX, mouseY, delta);

        this.buttonPrev.render(matrices, mouseX, mouseY, delta);
        this.buttonNext.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;

        this.buttonDone = this.addButton(new ButtonWidget(this.width / 2 + 4, 210, 150, 20, ScreenTexts.DONE, (buttonWidget) -> this.done()));
        this.buttonCancel = this.addButton(new ButtonWidget(this.width / 2 - 4 - 150, 210, 150, 20, ScreenTexts.CANCEL, (buttonWidget) -> this.close()));

        // Microcontroller selection buttons
        this.buttonPrev = this.addButton(new ButtonWidget(this.width / 2 - 150 - 4, 180, 70, 20, new TranslatableText("muc_maker.prev"), (buttonWidget) ->
                currentMUC = (currentMUC + MUCStatic.CHOICES.size() - 1) % MUCStatic.CHOICES.size())); // Don't -1 because java mod is stupid and will return negative values
        this.buttonNext = this.addButton(new ButtonWidget(this.width / 2 - 70 - 4, 180, 70, 20, new TranslatableText("muc_maker.next"), (buttonWidget) ->
                currentMUC = (currentMUC + 1) % MUCStatic.CHOICES.size()));


        this.buttonRotate0 = this.addButton(new ButtonWidget(this.width / 2 + 4, 180, 40, 20, new LiteralText("0"), (buttonWidget) -> {
            this.rotation = BlockRotation.NONE;
            this.updateRotationButton();
        }));
        this.buttonRotate90 = this.addButton(new ButtonWidget(this.width / 2 + 6 + 40, 180, 40, 20, new LiteralText("90"), (buttonWidget) -> {
            this.rotation = BlockRotation.CLOCKWISE_90;
            this.updateRotationButton();
        }));
        this.buttonRotate180 = this.addButton(new ButtonWidget(this.width / 2 + 8 + 80, 180, 40, 20, new LiteralText("180"), (buttonWidget) -> {
            this.rotation = BlockRotation.CLOCKWISE_180;
            this.updateRotationButton();
        }));
        this.buttonRotate270 = this.addButton(new ButtonWidget(this.width / 2 + 10 + 120, 180, 40, 20, new LiteralText("270"), (buttonWidget) -> {
            this.rotation = BlockRotation.COUNTERCLOCKWISE_90;
            this.updateRotationButton();
        }));

        // Only 1 choice, no prev / next needed
        if (MUCStatic.CHOICES.size() <= 1) {
            this.buttonNext.active = false;
            this.buttonPrev.active = false;
        }

        updateRotationButton();
    }

    /** Close screen & send packet with new information */
    @SuppressWarnings("deprecation")
    private void done() {
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());

        passedData.writeBlockPos(pos);
        passedData.writeInt(rotation.ordinal());
        passedData.writeInt(currentMUC);

        ClientSidePacketRegistry.INSTANCE.sendToServer(MUCBlockRegistry.MUC_MAKER_PACKET_ID, passedData);
        this.close();
    }

    /** Close the screen */
    private void close() {
        if (this.client == null) return;
        this.client.openScreen(null);
    }

    /** Set active rotation button to current rotation state */
    private void updateRotationButton() {
        this.buttonRotate0.active = true;
        this.buttonRotate90.active = true;
        this.buttonRotate180.active = true;
        this.buttonRotate270.active = true;

        switch(rotation) {
            case NONE:
                this.buttonRotate0.active = false;
                break;
            case CLOCKWISE_180:
                this.buttonRotate180.active = false;
                break;
            case COUNTERCLOCKWISE_90:
                this.buttonRotate270.active = false;
                break;
            case CLOCKWISE_90:
                this.buttonRotate90.active = false;
        }
    }
}
