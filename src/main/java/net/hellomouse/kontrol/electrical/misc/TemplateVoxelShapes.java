package net.hellomouse.kontrol.electrical.misc;

import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import static net.minecraft.block.Block.createCuboidShape;


/**
 * VoxelShape templates shared between electrical blocks
 * @author Bowserinator
 */
public class TemplateVoxelShapes {
    public static VoxelShape BASIC_PLATE_SHAPE;

    static {
        BASIC_PLATE_SHAPE = VoxelShapes.union(
                createCuboidShape(0, 0, 0, 16, 1, 16),  // Base
                createCuboidShape(6, 1, 14, 10, 10, 15), // Side connectors
                createCuboidShape(6, 1, 1, 10, 10, 2));  // Side connectors
    }

    private TemplateVoxelShapes() {}
}
