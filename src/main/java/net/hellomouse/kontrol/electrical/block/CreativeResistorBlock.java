package net.hellomouse.kontrol.electrical.block;

import net.hellomouse.kontrol.electrical.block.entity.ResistorBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;


@SuppressWarnings({"deprecation"})
public class CreativeResistorBlock extends AbstractPolarizedElectricalBlock {
    public CreativeResistorBlock(AbstractBlock.Settings settings) {
        super(settings, false);
    }

    @Override
    // TODO: customizable on click?
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new ResistorBlockEntity().resistance(1000.0);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos blockPos, ShapeContext context) {
        Direction facing = blockState.get(Properties.HORIZONTAL_FACING);
        return (facing == Direction.NORTH || facing == Direction.SOUTH) ?
                createCuboidShape(2, 0, 0, 14, 16, 16) :
                createCuboidShape(0, 0, 2, 16, 16, 14);
    }
}
