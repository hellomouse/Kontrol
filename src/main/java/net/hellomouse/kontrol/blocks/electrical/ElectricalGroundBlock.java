package net.hellomouse.kontrol.blocks.electrical;

import net.minecraft.block.AbstractBlock;

public class ElectricalGroundBlock extends AbstractWireBlock {
    public ElectricalGroundBlock(AbstractBlock.Settings settings) {
        super(settings, 1.5f, 2.5f);
    }

    // ---- Circuit computation ---- \\
    @Override
    void computeIO() {

    }
}
