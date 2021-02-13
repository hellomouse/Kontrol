package net.hellomouse.kontrol.electrical.block;

import net.hellomouse.kontrol.electrical.block.entity.SwitchBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;


@SuppressWarnings({"deprecation"})
public class CreativeSwitchBlock extends AbstractPolarizedElectricalBlock {

    // TODO: open blockstate too

    public CreativeSwitchBlock(AbstractBlock.Settings settings) {
        super(settings, true);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new SwitchBlockEntity();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos blockPos, ShapeContext context) {
        Direction facing = blockState.get(Properties.HORIZONTAL_FACING);
        return (facing == Direction.NORTH || facing == Direction.SOUTH) ?
                createCuboidShape(2, 0, 0, 14, 16, 16) :
                createCuboidShape(0, 0, 2, 16, 16, 14);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.abilities.allowModifyWorld)
            return ActionResult.PASS;
        else {
            SwitchBlockEntity entity = (SwitchBlockEntity) world.getBlockEntity(pos);
            entity.toggle();
            System.out.println("Used?" + entity.isOpen());

            return ActionResult.success(world.isClient);
        }
       //  return super.onUse(state, world, pos, player, hand, hit);
    }
}
