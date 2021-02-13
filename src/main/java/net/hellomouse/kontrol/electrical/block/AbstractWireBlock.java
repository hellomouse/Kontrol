package net.hellomouse.kontrol.electrical.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hellomouse.kontrol.electrical.block.entity.AbstractElectricalBlockEntity;
import net.hellomouse.kontrol.electrical.block.entity.WireBlockEntity;
import net.hellomouse.kontrol.registry.util.ColorData;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.ArrayList;

@SuppressWarnings({"deprecation"})
public abstract class AbstractWireBlock extends AbstractElectricalBlock implements Waterloggable {
    public static final BooleanProperty ATTACH_UP    = BooleanProperty.of("attach_up");
    public static final BooleanProperty ATTACH_DOWN  = BooleanProperty.of("attach_down");
    public static final BooleanProperty ATTACH_NORTH = BooleanProperty.of("attach_north");
    public static final BooleanProperty ATTACH_SOUTH = BooleanProperty.of("attach_south");
    public static final BooleanProperty ATTACH_EAST  = BooleanProperty.of("attach_east");
    public static final BooleanProperty ATTACH_WEST  = BooleanProperty.of("attach_west");

    private VoxelShape
        UP_SHAPE, DOWN_SHAPE, NORTH_SHAPE, SOUTH_SHAPE, EAST_SHAPE, WEST_SHAPE, NONE_SHAPE;

    protected final ColorData.COLOR_STRING color;

    public AbstractWireBlock(AbstractBlock.Settings settings, float wireSize, float noneSize, ColorData.COLOR_STRING color) {
        super(settings);
        generateVoxelShapes(wireSize, noneSize);
        setDefaultState(getStateManager().getDefaultState()
                .with(Properties.WATERLOGGED, false)
                .with(ATTACH_UP, false)
                .with(ATTACH_DOWN, false)
                .with(ATTACH_NORTH, false)
                .with(ATTACH_SOUTH, false)
                .with(ATTACH_EAST, false)
                .with(ATTACH_WEST, false));
        this.color = color;
    }

    public AbstractWireBlock(AbstractBlock.Settings settings, float wireSize, ColorData.COLOR_STRING color) {
        this(settings, wireSize, wireSize, color);
    }

    public AbstractWireBlock(AbstractBlock.Settings settings, float wireSize, float noneSize) {
        this(settings, wireSize, noneSize, null);
    }

    public AbstractWireBlock(AbstractBlock.Settings settings, float wireSize) {
        this(settings, wireSize,null);
    }


    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new WireBlockEntity().color(color);
    }

    /**
     * Generates the VoxelShapes for the wire, can be
     * overridden to change wire thickness
     * @param size Thickness (px) of the wire voxel
     * @param middleSize Thickness (px) of middle part
     */
    private void generateVoxelShapes(float size, float middleSize) {
        if (UP_SHAPE != null)
            return; // Already generated

        UP_SHAPE    = createCuboidShape(8 - size, 8 - size, 8 - size, 8 + size, 16, 8 + size);
        DOWN_SHAPE  = createCuboidShape(8 - size, 0, 8 - size, 8 + size, 8 + size, 8 + size);
        NORTH_SHAPE = createCuboidShape(8 - size, 8 - size, 0, 8 + size, 8 + size, 8 + size);
        SOUTH_SHAPE = createCuboidShape(8 - size, 8 - size, 8 - size, 8 + size, 8 + size, 16);
        EAST_SHAPE  = createCuboidShape(8 - size, 8 - size, 8 - size, 16, 8 + size, 8 + size);
        WEST_SHAPE  = createCuboidShape(0, 8 - size, 8 - size, 8 + size, 8 + size, 8 + size);
        NONE_SHAPE  = createCuboidShape(8 - middleSize, 8 - middleSize, 8 - middleSize, 8 + middleSize, 8 + middleSize, 8 + middleSize);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos blockPos, ShapeContext context) {
        ArrayList<VoxelShape> shapes = new ArrayList<>();

        if (blockState.get(ATTACH_UP))
            shapes.add(UP_SHAPE);
        if (blockState.get(ATTACH_DOWN))
            shapes.add(DOWN_SHAPE);
        if (blockState.get(ATTACH_NORTH))
            shapes.add(NORTH_SHAPE);
        if (blockState.get(ATTACH_SOUTH))
            shapes.add(SOUTH_SHAPE);
        if (blockState.get(ATTACH_EAST))
            shapes.add(EAST_SHAPE);
        if (blockState.get(ATTACH_WEST))
            shapes.add(WEST_SHAPE);
        if (shapes.isEmpty())
            return NONE_SHAPE;
        return VoxelShapes.union(NONE_SHAPE, shapes.toArray(new VoxelShape[0]));
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView blockView, BlockPos pos) {
        return 1.0f;
    }


    // ---- Waterlogging and connection ---- \\

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager){
        super.appendProperties(stateManager);
        stateManager
            .add(Properties.WATERLOGGED)
            .add(ATTACH_UP)
            .add(ATTACH_DOWN)
            .add(ATTACH_NORTH)
            .add(ATTACH_SOUTH)
            .add(ATTACH_EAST)
            .add(ATTACH_WEST);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction dir, BlockState blockstateOther, WorldAccess world, BlockPos pos, BlockPos otherPos) {
        state = super.getStateForNeighborUpdate(state, dir, blockstateOther, world, pos, otherPos);

        if (state.get(Properties.WATERLOGGED))
            world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));

        BlockEntity entity = world.getBlockEntity(pos);

        if (entity instanceof AbstractElectricalBlockEntity) {
            AbstractElectricalBlockEntity electricalBlockEntity = (AbstractElectricalBlockEntity)entity;
            return state.with(getPropertyFromDirection(dir), electricalBlockEntity.canConnectTo(dir, world.getBlockEntity(pos.offset(dir))));
        }

        return state;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

         for (Direction dir : Direction.values()) {
             BlockEntity entity = world.getBlockEntity(pos);

             if (entity instanceof AbstractElectricalBlockEntity) {
                 AbstractElectricalBlockEntity electricalBlockEntity = (AbstractElectricalBlockEntity) entity;
                 state = state.with(getPropertyFromDirection(dir), electricalBlockEntity.canConnectTo(dir, world.getBlockEntity(pos.offset(dir))));
             }
         }
         world.setBlockState(pos, state);


        // TODO: blockEntity.circuit.markInvaliD();
    }

    // ---- Connection Logic ---- \\
    public BooleanProperty getPropertyFromDirection(Direction dir) {
        switch (dir) {
            case UP:    return ATTACH_UP;
            case DOWN:  return ATTACH_DOWN;
            case WEST:  return ATTACH_WEST;
            case EAST:  return ATTACH_EAST;
            case SOUTH: return ATTACH_SOUTH;
            case NORTH: return ATTACH_NORTH;
        }
        throw new IllegalStateException("Invalid direction " + dir);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        BlockState state = super.getPlacementState(ctx);
        return state == null ? state : state.with(Properties.WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }
}
