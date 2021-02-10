package net.hellomouse.kontrol.electrical.block;

import net.hellomouse.kontrol.registry.util.ColorData;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;

public class BasicWireBlock extends AbstractWireBlock {
    public BasicWireBlock(AbstractBlock.Settings settings, ColorData.COLOR_STRING color) {
        super(settings, 1.5f, color); // Slightly larger hitbox for easier placing
    }
}
