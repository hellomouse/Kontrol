package net.hellomouse.kontrol.electrical.block;

import net.hellomouse.kontrol.electrical.block.entity.AbstractElectricalBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;


/**
 * A block that can be connected to with electrical wires or is otherwise modelled
 * as basic components of an electric circuit such as resistors, inductors or diodes
 * Examples include wires, generators, lamps, etc...
 */
@SuppressWarnings({"deprecation"})
public abstract class AbstractElectricalBlock extends BlockWithEntity {
    public AbstractElectricalBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction dir, BlockState blockstateOther, WorldAccess world, BlockPos pos, BlockPos otherPos) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof AbstractElectricalBlockEntity)
            ((AbstractElectricalBlockEntity) entity).updateAmbientTemperature();

        state = super.getStateForNeighborUpdate(state, dir, blockstateOther, world, pos, otherPos);
        return state;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
    }


    // --- Mojang --- \\
    @Override
    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.BLOCK; // Technically not necessary, already implicitly blocked
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL; // BlockWithEntity makes this INVISIBLE by default
    }
}
