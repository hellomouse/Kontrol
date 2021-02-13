package net.hellomouse.kontrol.electrical.block.entity;

import net.hellomouse.kontrol.electrical.block.AbstractPolarizedElectricalBlock;
import net.hellomouse.kontrol.electrical.items.multimeters.MultimeterReading;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

/**
 * TODO: mention must be made by AbstractPolarizedElectricalBock
 * Mention polarity convention
 */
public abstract class AbstractPolarizedElectricalBlockEntity extends AbstractElectricalBlockEntity {
    protected Direction positiveTerminal;
    protected Direction negativeTerminal;
    protected boolean rotate;

    public AbstractPolarizedElectricalBlockEntity(BlockEntityType<?> entityType) {
        super(entityType);
    }

    public void setRotate(boolean rotate) {
        this.rotate = rotate;
    }

    public MultimeterReading getReading() {
        if (nodalVoltages.size() < 2)
            return super.getReading().error();

        // Doesn't use internal component because can have internal resistors
        double voltage = nodalVoltages.get(0) - nodalVoltages.get(1);
        double current = internalCircuit.getComponents().get(0).getCurrent();
        return super.getReading()
                .voltage(voltage)
                .current(current)
                .power(Math.abs(voltage * current))
                .polarity(normalizedNodeToDir.get(normalizedOutgoingNodes.get(0)),
                          normalizedNodeToDir.get(normalizedOutgoingNodes.get(1)));
    }

    @Override
    public boolean canAttach(Direction dir, BlockEntity otherEntity) {
        return dir == getPositiveTerminal() || dir == getNegativeTerminal();
    }

    public Direction getPositiveTerminal() {
        if (positiveTerminal == null) {
            if (world == null || world.getBlockState(pos) == null)
                throw new IllegalStateException("Invalid block entity: no block state found");

            BlockState state = world.getBlockState(pos);
            if (!(state.getBlock() instanceof AbstractPolarizedElectricalBlock))
                throw new IllegalStateException("Invalid block entity: block state's block does not extend AbstractPolarizedElectricalBlock, rather it is " + state.getBlock());

            Direction dir = state.get(Properties.HORIZONTAL_FACING);
            positiveTerminal = dir;
        }
        return positiveTerminal;
    }

    public Direction getNegativeTerminal() {
        negativeTerminal = getPositiveTerminal().getOpposite();
        return negativeTerminal;
    }

    @Override
    protected void sortOutgoingNodesByPolarity() {
        int node1 = normalizedOutgoingNodes.get(0);
        int node2 = normalizedOutgoingNodes.get(1);

        if (normalizedNodeToDir.get(node1) != positiveTerminal) {
            normalizedOutgoingNodes.set(0, node2);
            normalizedOutgoingNodes.set(1, node1);
        }
    }
}
