package net.hellomouse.kontrol.electrical.block;

import net.hellomouse.kontrol.electrical.misc.TemplateVoxelShapes;
import net.hellomouse.kontrol.util.VoxelShapeUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;


@SuppressWarnings({"deprecation"})
public class BasicSwitchBlock extends AbstractSwitchBlock {
    public static final VoxelShape CUBOID_BASE;

    static {
        CUBOID_BASE = VoxelShapes.union(
                TemplateVoxelShapes.BASIC_PLATE_SHAPE,
                createCuboidShape(5, 1, 2, 11, 7, 14));
    }

    public BasicSwitchBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos blockPos, ShapeContext context) {
        Direction facing = blockState.get(AbstractPolarizedElectricalBlock.FACING);
        return VoxelShapeUtil.rotateShape(facing, CUBOID_BASE);
    }
}
