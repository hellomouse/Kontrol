package net.hellomouse.kontrol.electrical.block;

import net.hellomouse.kontrol.electrical.block.entity.ScopeBlockEntity;
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
public class CreativeScopeBlock extends AbstractPolarizedElectricalBlock{
    private static final VoxelShape SHAPE;
    static {
        SHAPE = VoxelShapes.union(
            createCuboidShape(0, 0, 1, 16, 16, 2),
            createCuboidShape(0, 0, 2, 1, 13, 14),
            createCuboidShape(15, 0, 2, 16, 13, 14),
            createCuboidShape(1, 3, 4, 15, 11, 12)
        );
    }

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
        Direction facing = blockState.get(AbstractPolarizedElectricalBlock.FACING).rotateYCounterclockwise();
        return VoxelShapeUtil.rotateShape(facing, SHAPE);
    }


}
