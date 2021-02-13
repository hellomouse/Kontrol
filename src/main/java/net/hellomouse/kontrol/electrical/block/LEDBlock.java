package net.hellomouse.kontrol.electrical.block;

import net.hellomouse.kontrol.electrical.block.entity.ResistorBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class LEDBlock extends AbstractElectricalBlock {
    public static final IntProperty BRIGHTNESS = Properties.POWER;

    public LEDBlock(AbstractBlock.Settings settings) {
        super(settings);
        setDefaultState(getStateManager()
                .getDefaultState()
                .with(Properties.WATERLOGGED, false)
                .with(BRIGHTNESS, 0));
    }


    public Direction positiveTerminal(BlockState state) {
        return state.get(Properties.HORIZONTAL_FACING);
    }

    public Direction negativeTerminal(BlockState state) {
        return positiveTerminal(state).getOpposite();
    }


    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager){
        super.appendProperties(stateManager);
        stateManager
                .add(Properties.HORIZONTAL_FACING)
                .add(Properties.WATERLOGGED)
                .add(BRIGHTNESS);
    }

    @Override
    // implement polarizd component
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new ResistorBlockEntity().resistance(1000.0);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        BlockState state = super.getPlacementState(ctx);
        return state == null ? null : state
                .with(Properties.WATERLOGGED, fluidState.getFluid() == Fluids.WATER)
                .with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing())
                .with(BRIGHTNESS, 0);
    }
}
