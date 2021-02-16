package net.hellomouse.kontrol.electrical.block.entity;

import net.hellomouse.kontrol.electrical.block.AbstractWireBlock;
import net.hellomouse.kontrol.electrical.items.multimeters.MultimeterReading;
import net.hellomouse.kontrol.electrical.circuit.CircuitValues;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualResistor;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;

import net.hellomouse.kontrol.registry.util.ColorData;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.Direction;


public class CableBlockEntity extends AbstractElectricalBlockEntity {
    // TODO: resistance should be in constructor

    protected ColorData.COLOR_STRING color;

    public CableBlockEntity() { super(ElectricalBlockRegistry.WIRE_BLOCK_ENTITY); }

    /** For use in classes that extend this */
    protected CableBlockEntity(BlockEntityType<?> entityType) { super(entityType); }

    public CableBlockEntity color(ColorData.COLOR_STRING color) {
        this.color = color;
        return this;
    }

    public ColorData.COLOR_STRING getColor() { return color; }

    @Override
    public VirtualCircuit getInternalCircuit() {
        // [Internal circuit]:
        // - 1 internal node at center
        // - Up to 6 outgoing nodes on each side

        internalCircuit.clear();
        for (int outNode : normalizedOutgoingNodes)
            internalCircuit.addComponent(new VirtualResistor(CircuitValues.LOW_VOLTAGE_RESISTANCE), -1, outNode);
        return internalCircuit;
    }

    public MultimeterReading getReading() {
        if (normalizedOutgoingNodes.size() == 0)
            return super.getReading().error();

        double absoluteVoltage = 0.0;
        for (double val : nodalVoltages)
            absoluteVoltage += val;
        absoluteVoltage /= nodalVoltages.size();

        if (normalizedOutgoingNodes.size() == 2) {
            double voltage = nodalVoltages.get(0) - nodalVoltages.get(1);
            double current = internalCircuit.getComponents().get(0).getCurrent();

            return super.getReading()
                .absoluteVoltage(absoluteVoltage)
                .voltage(voltage)
                .current(current)
                .power(Math.abs(voltage * current));
        }
        return super.getReading().absoluteVoltage(absoluteVoltage);
    }

    @Override
    public boolean canAttach(Direction dir, BlockEntity otherEntity) {
        if (world == null || world.getBlockState(pos) == null)
            throw new IllegalStateException("Invalid block entity: no block state found");

        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof AbstractWireBlock))
            throw new IllegalStateException("Invalid block entity: block state's block does not extend AbstractWireBlock, rather it is " + state.getBlock());

        if (color == null)
            return true;
        if (otherEntity instanceof CableBlockEntity)
            return ((CableBlockEntity)otherEntity).getColor() == color || ((CableBlockEntity)otherEntity).getColor() == null;

        return true;
    }
}
