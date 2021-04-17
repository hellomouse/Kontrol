package net.hellomouse.kontrol.electrical.block.microcontroller.entity;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.hellomouse.kontrol.electrical.microcontroller.C8051.MUCStatic;
import net.hellomouse.kontrol.electrical.screen.CreativeMUCMakerScreenHandler;
import net.hellomouse.kontrol.registry.block.MUCBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.BlockRotation;

public class CreativeMUCMakerBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, BlockEntityClientSerializable {
    private int currentMUC = 0;
    private int rotationIndex = 0;

    public CreativeMUCMakerBlockEntity() {
        super(MUCBlockRegistry.MUC_MAKER_BLOCK_ENTITY);
    }

    /**
     * Get the coordinates for the corners of the bounding box
     * where the ports will be constructed <b>as a relative offset</b>
     * to the current microcontroller
     * @return Coordinates {x1, y1, z1, x2, y2, z2}
     */
    public int[] getBoundingCoordinates() {
        int x1 = 0, y1 = 0, z1 = 0;
        int x2, y2 = 1, z2;

        int xSize = 10;
        int zSize = 20;

        BlockRotation rotation = getRotation();

        // Switch statement won't run with BlockRotation enum
        // if you can figure it out pls fix
        if (rotation == BlockRotation.NONE) {
            z1 = 1;
            x2 = xSize;
            z2 = 1 + zSize;
        }
        else if (rotation == BlockRotation.CLOCKWISE_90) {
            x2 = -zSize;
            z2 = xSize;
        }
        else if (rotation == BlockRotation.CLOCKWISE_180) {
            x1 = 1;
            x2 = 1 - xSize;
            z2 = -zSize;
        }
        else {
            x1 = z1 = 1;
            x2 = zSize + 1;
            z2 = 1 - xSize;
        }

        return new int[]{ x1, y1, z1, x2, y2, z2 };
    }

    /** Generate all the ports, call when powered */
    public void create() {
        if (world == null) return;

        int[] coords = getBoundingCoordinates();
        int x1 = coords[0], y1 = coords[1], z1 = coords[2], x2 = coords[3], y2 = coords[4], z2 = coords[5];

    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new CreativeMUCMakerScreenHandler(syncId, playerInventory);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
        packetByteBuf.writeBlockPos(pos);
        packetByteBuf.writeInt(rotationIndex);
        packetByteBuf.writeInt(currentMUC);
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        rotationIndex = tag.getInt("rotationIndex");
        currentMUC = tag.getInt("currentMUC");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.putInt("rotationIndex", rotationIndex);
        tag.putInt("currentMUC", currentMUC);
        return tag;
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        rotationIndex = tag.getInt("rotationIndex");
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        tag.putInt("rotationIndex", rotationIndex);
        return tag;
    }

    /**
     * Update the MUC Maker with data from a C2S packet. If any value given is invalid it will be ignored and
     * the current value will take its place (for that specific value)
     *
     * @param rotationIndex Mew rotation packet
     * @param currentMUC Current MUC index
     */
    public void writePacketData(int rotationIndex, int currentMUC) {
        // Ignore any invalid inputs
        if (rotationIndex < 0 || rotationIndex >= BlockRotation.values().length) rotationIndex = this.rotationIndex;
        if (currentMUC < 0 || currentMUC >= MUCStatic.CHOICES.size()) currentMUC = 0;

        this.rotationIndex = rotationIndex;
        this.currentMUC = currentMUC;
        this.markDirty();
        this.sync();
    }

    /**
     * Get current rotation
     * @return Rotation
     */
    public BlockRotation getRotation() {
        return BlockRotation.values()[rotationIndex];
    }
}
