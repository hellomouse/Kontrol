package net.hellomouse.kontrol.electrical.screen;

import net.hellomouse.kontrol.electrical.microcontroller.MUCStatic;
import net.hellomouse.kontrol.registry.block.MUCBlockRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;

public class CreativeMUCPortMakerScreenHandler extends ScreenHandler {
    private BlockPos pos;
    private int rotationIndex = MUCStatic.rotationIndex;
    private int sideLength = MUCStatic.sideLength;
    private int portLower = MUCStatic.portLower;
    private int portUpper = MUCStatic.portUpper;
    private int currentMUC = MUCStatic.currentMUC;

    public CreativeMUCPortMakerScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(MUCBlockRegistry.MUC_PORT_MAKER_SCREEN_HANDLER, syncId);
        pos = BlockPos.ORIGIN;
    }

    public CreativeMUCPortMakerScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory);

        pos = buf.readBlockPos();
        rotationIndex = buf.readInt();
        sideLength = buf.readInt();
        portLower = buf.readInt();
        portUpper = buf.readInt();
        currentMUC = buf.readInt();
    }

    public BlockPos getPos() { return pos; }
    public int getRotationIndex() { return rotationIndex; }
    public int getSideLength() { return sideLength; }
    public int getPortLower() { return portLower; }
    public int getPortUpper() { return portUpper; }
    public int getCurrentMUC() { return currentMUC; }

    @Override
    public boolean canUse(PlayerEntity player) {
        return player.isCreativeLevelTwoOp();
    }
}
