package net.hellomouse.kontrol.logic.circuit.components;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;

public interface PolarizedComponent {
    public Direction positiveTerminal(BlockState state);
    public Direction negativeTerminal(BlockState state);
}
