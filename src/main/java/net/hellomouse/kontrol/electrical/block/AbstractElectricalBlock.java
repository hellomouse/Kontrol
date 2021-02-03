package net.hellomouse.kontrol.electrical.block;

import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.util.math.Direction;


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


    // --- Custom to Electrical Block --- \\

    abstract boolean canAttach(BlockState state, Direction dir, Block other);


    // --- Connection logic --- \\
    public boolean canConnect(BlockState state, Direction dir, BlockState blockStateOther) {
        return canAttach(state, dir, blockStateOther.getBlock()) &&
                !blockStateOther.isAir() &&
                globalConnectableCheck(blockStateOther.getBlock()) &&
                ((AbstractElectricalBlock)(blockStateOther.getBlock())).canAttach(blockStateOther, dir.getOpposite(), this);
    }

    private boolean globalConnectableCheck(Block other) {
        return other instanceof AbstractElectricalBlock;
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
