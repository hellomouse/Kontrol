package net.hellomouse.kontrol.electrical.block.interfaces;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;

public interface IPolarizedBlock {
    Direction positiveTerminal(BlockState state);
    Direction negativeTerminal(BlockState state);
}
