package net.hellomouse.kontrol.electrical.block.microcontroller;

import net.hellomouse.kontrol.electrical.microcontroller.C8051.C8051Network;
import net.hellomouse.kontrol.electrical.microcontroller.MUCNetworkManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@SuppressWarnings({"deprecation"})
public abstract class AbstractMUCCoreBlock extends Block {
    private final String MUCID;

    public static final VoxelShape MUC_CORE_SHAPE = createCuboidShape(0, 0, 0, 16, 12, 16);

    public AbstractMUCCoreBlock(Settings settings, String MUCID) {
        super(settings);
        this.MUCID = MUCID;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos blockPos, ShapeContext context) {
        return MUC_CORE_SHAPE;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction dir, BlockState blockstateOther, WorldAccess world, BlockPos pos, BlockPos otherPos) {
        state = super.getStateForNeighborUpdate(state, dir, blockstateOther, world, pos, otherPos);

        return state;
    }

    // TODO: on delete

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        MUCNetworkManager.create(pos, world, C8051Network::new);
    }
}
