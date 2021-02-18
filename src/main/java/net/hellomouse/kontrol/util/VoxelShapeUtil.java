package net.hellomouse.kontrol.util;

import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;


/**
 * Utilities for dealing with VoxelShapes
 * @author Bowserinator
 */
public class VoxelShapeUtil {
    /**
     * Rotate the "NORTH" end of the shape so it faces in Direction "to" instead.
     * In the event a shape has multiple ending orientations that are all valid (ie, rotating so
     * NORTH -> UP can have 4 distinct rotations that all point up), the one that requires the least
     * rotations will be done.
     *
     * @param to Direction original north end of shape now points.
     * @param shape Shape to rotate
     * @return Rotated shape
     */
    public static VoxelShape rotateShape(Direction to, VoxelShape shape) {
        if (to == Direction.NORTH)
            return shape;

        final VoxelShape[] newShape = { VoxelShapes.empty() };

        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            switch(to) {
                case WEST:
                    // Swap X <-> Z
                    newShape[0] = VoxelShapes.union(newShape[0], VoxelShapes.cuboid(minZ, minY, minX, maxZ, maxY, maxX));
                    break;
                case EAST:
                    // Swap X <-> Z, then take 1 - newX
                    newShape[0] = VoxelShapes.union(newShape[0], VoxelShapes.cuboid(1 - minZ, minY, minX, 1 - maxZ, maxY, maxX));
                    break;
                case SOUTH:
                    // Z = 1 - Z
                    newShape[0] = VoxelShapes.union(newShape[0], VoxelShapes.cuboid(minX, minY, 1 - minZ, maxX, maxY, 1 - maxZ));
                    break;
                case UP:
                    // Swap Y, Z, then take 1 - newY
                    newShape[0] = VoxelShapes.union(newShape[0], VoxelShapes.cuboid(minX, 1 - minZ, minY, maxX, 1 - maxZ, maxY));
                    break;
                case DOWN:
                    // Swap Y, Z
                    newShape[0] = VoxelShapes.union(newShape[0], VoxelShapes.cuboid(minX, minZ, minY, maxX, maxZ, maxY));
                    break;
            }
        });

        return newShape[0];
    }

    // TODO: mirrorShape
}
