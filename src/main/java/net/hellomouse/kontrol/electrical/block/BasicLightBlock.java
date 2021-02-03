package net.hellomouse.kontrol.electrical.block;

import net.hellomouse.kontrol.electrical.block.entity.ResistorBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;


@SuppressWarnings({"deprecation"})
public class BasicLightBlock extends AbstractPolarizedElectricalBlock {
    public static final IntProperty BRIGHTNESS = Properties.POWER;
    public static final int LIGHT_COLOR = 0xffffff;

    public BasicLightBlock(AbstractBlock.Settings settings) {
        super(settings, true);
        setDefaultState(getStateManager()
                .getDefaultState()
                .with(BRIGHTNESS, 0));
    }

    @Override
    // TODO: use CircuitValues
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

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager){
        super.appendProperties(stateManager);
        stateManager.add(BRIGHTNESS);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        return state == null ? null : state.with(BRIGHTNESS, 0);
    }
}
