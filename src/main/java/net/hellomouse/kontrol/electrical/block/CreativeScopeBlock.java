package net.hellomouse.kontrol.electrical.block;

import net.hellomouse.kontrol.electrical.block.entity.ScopeBlockEntity;
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
public class CreativeScopeBlock extends AbstractPolarizedElectricalBlock{
    public CreativeScopeBlock(AbstractBlock.Settings settings) {
        super(settings, true);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        ScopeBlockEntity blockEntity = new ScopeBlockEntity().tier(ScopeBlockEntity.T_CREATIVE);
        // TODO: move ID assignment to the entity
        blockEntity.createScopeState(128);
        return blockEntity;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos blockPos, ShapeContext context) {
        Direction facing = blockState.get(AbstractPolarizedElectricalBlock.FACING);
        boolean NS = facing == Direction.NORTH || facing == Direction.SOUTH;

        VoxelShape base = NS ?
                createCuboidShape(2, 0, 0, 14, 1, 16) :
                createCuboidShape(0, 0, 2, 16, 1, 14);
        VoxelShape top = NS ?
                createCuboidShape(4, 0, 1, 12, 12, 15) :
                createCuboidShape(1, 1, 4, 15, 12, 12);
        return VoxelShapes.union(base, top);
    }


}
