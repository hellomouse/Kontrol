package net.hellomouse.kontrol.electrical.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;

public class BasicWireBlock extends AbstractWireBlock {
    public BasicWireBlock(AbstractBlock.Settings settings) {
        super(settings, 1.5f); // Slightly larger hitbox for easier placing
    }

    @Override
    boolean canAttach(BlockState state, Direction dir, Block other) {
        if (other instanceof BasicWireBlock) // Connect to basic wires of same color
            return other == this;
        return true;
    }
}
