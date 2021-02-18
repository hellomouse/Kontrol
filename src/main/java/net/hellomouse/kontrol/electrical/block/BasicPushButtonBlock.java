package net.hellomouse.kontrol.electrical.block;

import net.hellomouse.kontrol.util.VoxelShapeUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;


@SuppressWarnings({"deprecation"})
public class BasicPushButtonBlock extends AbstractPushButtonBlock {
    public static final VoxelShape CUBOID_BASE, UNPRESSED_SHAPE, PRESSED_SHAPE;

    static {
        CUBOID_BASE = VoxelShapes.union(
                createCuboidShape(0, 0, 0, 16, 1, 16),  // Base
                createCuboidShape(2, 1, 2, 14, 4, 14),  // Button housing
                createCuboidShape(6, 1, 15, 10, 9, 16), // Side connectors
                createCuboidShape(6, 1, 0, 10, 9, 1));  // Side connectors
        UNPRESSED_SHAPE = createCuboidShape(4, 4, 4, 12, 7, 12);
        PRESSED_SHAPE = createCuboidShape(4, 4, 4, 12, 5, 12);
    }

    public BasicPushButtonBlock(AbstractBlock.Settings settings) { super(settings); }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos blockPos, ShapeContext context) {
        Direction facing = blockState.get(Properties.HORIZONTAL_FACING);
        boolean pressed = blockState.get(AbstractPushButtonBlock.PRESSED);
        return VoxelShapeUtil.rotateShape(facing, VoxelShapes.union(CUBOID_BASE, pressed ? PRESSED_SHAPE : UNPRESSED_SHAPE));
    }
}
