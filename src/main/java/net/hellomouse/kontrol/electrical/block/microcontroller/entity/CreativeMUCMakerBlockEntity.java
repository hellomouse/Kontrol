package net.hellomouse.kontrol.electrical.block.microcontroller.entity;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.hellomouse.kontrol.electrical.microcontroller.C8051.MUCList;
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
import net.minecraft.util.Pair;

public class CreativeMUCMakerBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, BlockEntityClientSerializable {
    private int rotationIndex = 0;
    private int sideLength = 10;
    private int portLower = 0;
    private int portUpper = 16;
    private int currentMUC = 0;

    public CreativeMUCMakerBlockEntity() {
        super(MUCBlockRegistry.MUC_MAKER_BLOCK_ENTITY);
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
        packetByteBuf.writeInt(sideLength);
        packetByteBuf.writeInt(portLower);
        packetByteBuf.writeInt(portUpper);
        packetByteBuf.writeInt(currentMUC);
    }

    public void writePacketData(int rotationIndex, int sideLength, int portLower, int portUpper, int currentMUC) {
        // Ignore any invalid inputs
        if (rotationIndex < 0 || rotationIndex >= BlockRotation.values().length) rotationIndex = this.rotationIndex;
        if (sideLength <= 0) this.sideLength = sideLength;
        if (portLower < 0) portLower = this.portLower;

        if (portLower > portUpper) {
            portLower = this.portLower;
            portUpper = this.portUpper;
        }
        if (sideLength > MUCList.MAX_SIDE_LENGTH) sideLength = this.sideLength;
        if (currentMUC < 0 || currentMUC >= MUCList.CHOICES.size()) currentMUC = 0;
        if (portUpper >= MUCList.CHOICES.get(currentMUC).maxPorts) portUpper = this.portUpper;

        this.rotationIndex = rotationIndex;
        this.sideLength = sideLength;
        this.portLower = portLower;
        this.portUpper = portUpper;
        this.currentMUC = currentMUC;
        this.markDirty();
        this.sync();
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        rotationIndex = tag.getInt("rotationIndex");
        sideLength = tag.getInt("sideLength");
        portLower = tag.getInt("portLower");
        portUpper = tag.getInt("portUpper");
        currentMUC = tag.getInt("currentMUC");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.putInt("rotationIndex", rotationIndex);
        tag.putInt("sideLength", sideLength);
        tag.putInt("portLower", portLower);
        tag.putInt("portUpper", portUpper);
        tag.putInt("currentMUC", currentMUC);
        return tag;
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        rotationIndex = tag.getInt("rotationIndex");
        sideLength = tag.getInt("sideLength");
        portLower = tag.getInt("portLower");
        portUpper = tag.getInt("portUpper");
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        tag.putInt("rotationIndex", rotationIndex);
        tag.putInt("sideLength", sideLength);
        tag.putInt("portLower", portLower);
        tag.putInt("portUpper", portUpper);
        return tag;
    }

    /**
     * Get the x and z side lengths of the bounding box
     * computed from port range and side length
     * @return Pair of x side length, z side length
     */
    public Pair<Integer, Integer> getBoundingBoxSize() {
        return new Pair<>(sideLength, (int)Math.ceil((float)(portUpper - portLower) / sideLength));
    }

    /**
     * Get current rotation
     * @return Rotation
     */
    public BlockRotation getRotation() {
        return BlockRotation.values()[rotationIndex];
    }
}
