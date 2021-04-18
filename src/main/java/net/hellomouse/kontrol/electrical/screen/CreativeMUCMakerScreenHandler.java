package net.hellomouse.kontrol.electrical.screen;

import net.hellomouse.kontrol.electrical.microcontroller.MUCStatic;
import net.hellomouse.kontrol.registry.block.MUCBlockRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;

public class CreativeMUCMakerScreenHandler extends ScreenHandler {
    private BlockPos pos;
    private int rotationIndex = MUCStatic.rotationIndex;
    private int currentMUC = MUCStatic.currentMUC;

    public CreativeMUCMakerScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(MUCBlockRegistry.MUC_MAKER_SCREEN_HANDLER, syncId);
        pos = BlockPos.ORIGIN;
    }

    public CreativeMUCMakerScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory);

        pos = buf.readBlockPos();
        rotationIndex = buf.readInt();
        currentMUC = buf.readInt();
    }

    public BlockPos getPos() { return pos; }
    public int getRotationIndex() { return rotationIndex; }
    public int getCurrentMUC() { return currentMUC; }

    @Override
    public boolean canUse(PlayerEntity player) {
        return player.isCreativeLevelTwoOp();
    }
}
