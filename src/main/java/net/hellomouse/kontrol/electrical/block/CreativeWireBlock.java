package net.hellomouse.kontrol.electrical.block;

import net.hellomouse.kontrol.electrical.block.entity.SuperconductingWireBlockEntity;
import net.hellomouse.kontrol.registry.util.ColorData;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;

public class CreativeWireBlock extends AbstractWireBlock {
    public CreativeWireBlock(AbstractBlock.Settings settings, ColorData.COLOR_STRING color) {
        super(settings, 2.5f, color);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new SuperconductingWireBlockEntity().color(color);
    }
}
