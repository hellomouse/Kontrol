package net.hellomouse.kontrol.electrical.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;


/**
 * A polarized electrical block (has a positive and negative terminal). By default:
 * - Block will have FACING
 * - The positive terminal is HORIZONTAL_FACING direction
 * - The negative terminal is opposite of positive terminal
 * - Waterlogging logic is included by default
 *
 * @author Bowserinator
 */
@SuppressWarnings({"deprecation"})
public abstract class AbstractPolarizedElectricalBlock extends AbstractElectricalBlock {
    public static DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public final boolean waterloggable;
    public final boolean rotateWhenPlacing;

    // TODO: dont water log by default?

    /**
     * Construct an AbstractPolarizedElectricalBlock
     * @param settings AbstractBlock Settings
     * @param rotateWhenPlacing If true will make it face 90 deg rotated from the direction the player faces when placed
     */
    public AbstractPolarizedElectricalBlock(AbstractBlock.Settings settings, boolean rotateWhenPlacing) {
        super(settings);
        this.waterloggable = true; // TODO: set after super call, maybe as a hack make 2 copies of class that extend this, and return? :shrug:
        // TODO: voxel shape calculates water spread hmm add a note
        this.rotateWhenPlacing = rotateWhenPlacing;

        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(Properties.WATERLOGGED, false));
       // if (waterloggable)
       //     state = state.with(Properties.WATERLOGGED, false);
        // setDefaultState(state);
    }


    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction dir, BlockState blockStateOther, WorldAccess world, BlockPos pos, BlockPos otherPos) {
        state = super.getStateForNeighborUpdate(state, dir, blockStateOther, world, pos, otherPos);
        if (state.get(Properties.WATERLOGGED))
            world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        return state;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager){
        super.appendProperties(stateManager);
        stateManager.add(FACING);
        if (true || waterloggable)
            stateManager.add(Properties.WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return waterloggable && state.get(Properties.WATERLOGGED) ?
                Fluids.WATER.getStill(false) :
                super.getFluidState(state);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        System.out.println("Rotate: " + rotateWhenPlacing);
        if (state == null)
            return null;

        state = state.with(FACING, rotateWhenPlacing ?
                ctx.getPlayerFacing().rotateYCounterclockwise() : ctx.getPlayerFacing());

        if (waterloggable) {
            FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
            return state.with(Properties.WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
        }
        return state;
    }
}
