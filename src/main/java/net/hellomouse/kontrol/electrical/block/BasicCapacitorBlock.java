package net.hellomouse.kontrol.electrical.block;

import net.hellomouse.kontrol.electrical.block.entity.CapacitorBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;


public class BasicCapacitorBlock extends AbstractPolarizedElectricalBlock {
    public BasicCapacitorBlock(AbstractBlock.Settings settings) {
        super(settings, true);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new CapacitorBlockEntity();
    }
}
