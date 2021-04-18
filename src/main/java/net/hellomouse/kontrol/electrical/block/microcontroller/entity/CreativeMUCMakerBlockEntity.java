package net.hellomouse.kontrol.electrical.block.microcontroller.entity;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.hellomouse.kontrol.electrical.microcontroller.MUCStatic;
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
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class CreativeMUCMakerBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, BlockEntityClientSerializable {
    private int currentMUC = 0;
    private int rotationIndex = 0;
    private ArrayList<String[]> blueprint = null;

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

        if (blueprint == null)
            return new int[]{0, 0, 0, 1, 1, 1};

        int xSize = blueprint.size();
        int zSize = blueprint.get(0).length;

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
        if (blueprint == null) return;

        int[] coords = getBoundingCoordinates();
        int x1 = coords[0], z1 = coords[2], x2 = coords[3], z2 = coords[5];
        int xSize = blueprint.size();
        int zSize = blueprint.get(0).length;
        if (x1 > x2) {
            int temp = x2;
            x2 = x1;
            x1 = temp;
        }
        if (z1 > z2) {
            int temp = z2;
            z2 = z1;
            z1 = temp;
        }

        for (int x = x1; x < x2; x++) {
            for (int z = z1; z < z2; z++) {
                // Rotation 0:   +x +z
                int blueprintRow = x;
                int blueprintCol = z - 1;
                BlockRotation rotation = getRotation();

                // Rotation 270: +z +x
                if (rotation == BlockRotation.COUNTERCLOCKWISE_90) {
                    blueprintRow = xSize + z - 1;
                    blueprintCol = zSize - x;
                }
                // Rotation 90:  -z -x
                else if (rotation == BlockRotation.CLOCKWISE_90) {
                    blueprintRow = xSize - z - 1;
                    blueprintCol = zSize + x;
                }
                // Rotation 180: -x -z
                else if (rotation == BlockRotation.CLOCKWISE_180) {
                    blueprintRow = xSize + x - 1;
                    blueprintCol = zSize + z;
                }

                System.out.println(x + ", " + z + "  = " + blueprintRow + ", " + blueprintCol);

                String blueprintBlock = blueprint.get(blueprintRow)[blueprintCol];
                BlockPos blockPos = new BlockPos(pos.getX() + x, pos.getY(), pos.getZ() + z);

                if (blueprintBlock.equals(".."))
                    continue;
                else if (blueprintBlock.equals("CO"))
                    world.setBlockState(blockPos, MUCBlockRegistry.MUC_PORT_CONNECTOR_BLOCK.getDefaultState(), 3);
                else {
                    try {
                        int value = Integer.parseInt(blueprintBlock);
                        world.setBlockState(blockPos, MUCBlockRegistry.MUC_PORT_BLOCK.getDefaultState(), 3);
                        BlockEntity blockEntity = world.getBlockEntity(blockPos);
                        if (blockEntity instanceof MUCPortBlockEntity)
                            ((MUCPortBlockEntity)blockEntity).setPortId(value);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
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
        blueprint = MUCStatic.MUCBlueprints.get(MUCStatic.CHOICES.get(currentMUC).id);
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
        currentMUC = tag.getInt("currentMUC");
        blueprint = MUCStatic.MUCBlueprints.get(MUCStatic.CHOICES.get(currentMUC).id);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        tag.putInt("rotationIndex", rotationIndex);
        tag.putInt("currentMUC", currentMUC);
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
        blueprint = MUCStatic.MUCBlueprints.get(MUCStatic.CHOICES.get(currentMUC).id);

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
