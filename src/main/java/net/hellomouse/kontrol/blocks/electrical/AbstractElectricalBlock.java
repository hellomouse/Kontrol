package net.hellomouse.kontrol.blocks.electrical;

import net.minecraft.block.*;
import net.minecraft.util.math.Direction;


/**
 * A block that can be connected to with electrical wires (can transfer electricity)
 * Examples include wires, generators, lamps, etc...
 */
public abstract class AbstractElectricalBlock extends BlockWithEntity {
    private int groupID = -1;

    public AbstractElectricalBlock(AbstractBlock.Settings settings) {
        super(settings);
    }


    // ---- Custom to Electrical Block ---- \\

    abstract void computeIO();
    abstract boolean canAttach(BlockState state, Direction dir, Block other);

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public int getGroupID() {
        return groupID;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    // --- Connection logic --- \\
    protected boolean canConnect(BlockState state, Direction dir, BlockState blockStateOther) {
        return canAttach(state, dir, blockStateOther.getBlock()) &&
                !blockStateOther.isAir() &&
                globalConnectableCheck(blockStateOther.getBlock()) &&
                ((AbstractElectricalBlock)(blockStateOther.getBlock())).canAttach(blockStateOther, dir.getOpposite(), this);
    }

    private boolean globalConnectableCheck(Block other) {
        return other instanceof AbstractElectricalBlock;
    }
}
