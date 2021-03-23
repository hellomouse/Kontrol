package net.hellomouse.kontrol.electrical.block;

import net.hellomouse.kontrol.electrical.block.entity.BuzzerBlockEntity;
import net.hellomouse.kontrol.electrical.misc.TemplateVoxelShapes;
import net.hellomouse.kontrol.util.VoxelShapeUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

@SuppressWarnings({"deprecation"})
public class BasicBuzzerBlock extends AbstractPolarizedElectricalBlock {
    public static final VoxelShape CUBOID_BASE;

    static {
        CUBOID_BASE = VoxelShapes.union(
                TemplateVoxelShapes.BASIC_PLATE_SHAPE,
                createCuboidShape(3, 3, 3, 13, 9, 13));
    }

    public BasicBuzzerBlock(AbstractBlock.Settings settings) {
        super(settings, true);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new BuzzerBlockEntity().pitch(1f).voltageThreshold(0.5f).resistance(300);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos blockPos, ShapeContext context) {
        Direction facing = blockState.get(AbstractPolarizedElectricalBlock.FACING);
        return VoxelShapeUtil.rotateShape(facing, CUBOID_BASE);
    }
}
