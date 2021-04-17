package net.hellomouse.kontrol.electrical.block.microcontroller;

import net.hellomouse.kontrol.electrical.block.AbstractElectricalBlock;
import net.hellomouse.kontrol.electrical.block.microcontroller.entity.MUCRedstonePortBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public class MUCRedstonePortBlock extends AbstractElectricalBlock {
    public static final BooleanProperty POWERING = BooleanProperty.of("powering");
    public static final DirectionProperty FACING = Properties.FACING;

    public MUCRedstonePortBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(POWERING, false).with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager){
        super.appendProperties(stateManager);
        stateManager.add(POWERING).add(FACING);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new MUCRedstonePortBlockEntity().voltageToRedstoneFunction(voltage -> voltage > 0.7);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!moved && !state.isOf(newState.getBlock())) {
            if (state.get(POWERING))
                this.updateNeighbors(world, pos);
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWERING) ? 15 : 0;
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    private void updateNeighbors(World world, BlockPos pos) {
        world.updateNeighborsAlways(pos, this);
    }
}
