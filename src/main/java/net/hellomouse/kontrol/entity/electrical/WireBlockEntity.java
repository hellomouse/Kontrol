package net.hellomouse.kontrol.entity.electrical;

import net.hellomouse.kontrol.items.electrical.multimeters.MultimeterReading;
import net.hellomouse.kontrol.logic.circuit.CircuitValues;
import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.logic.circuit.virtual.components.VirtualResistor;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;

import net.minecraft.block.entity.BlockEntityType;


public class WireBlockEntity extends AbstractElectricalBlockEntity {
    // TODO: resistance should be in constructor

    public WireBlockEntity() { super(ElectricalBlockRegistry.WIRE_BLOCK_ENTITY); }

    /** For use in classes that extend this */
    protected WireBlockEntity(BlockEntityType<?> entityType) { super(entityType); }

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
}
