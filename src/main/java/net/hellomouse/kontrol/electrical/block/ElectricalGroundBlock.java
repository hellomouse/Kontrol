package net.hellomouse.kontrol.electrical.block;

import net.hellomouse.kontrol.electrical.block.entity.ElectricalGroundEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;

public class ElectricalGroundBlock extends AbstractWireBlock {
    public ElectricalGroundBlock(AbstractBlock.Settings settings) {
        super(settings, 1.5f, 2.5f);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new ElectricalGroundEntity();
    }
}
