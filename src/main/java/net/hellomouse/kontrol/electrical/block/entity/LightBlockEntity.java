package net.hellomouse.kontrol.electrical.block.entity;

import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualResistor;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;


public class LightBlockEntity extends AbstractPolarizedElectricalBlockEntity {
    // TODO: dont model light as light, make resistor block entity or something

    public LightBlockEntity() {
        super(ElectricalBlockRegistry.RESISTOR_BLOCK_ENTITY);
    }

    @Override
    public VirtualCircuit getInternalCircuit() {
        internalCircuit.clear();

        double resistance = 1.0;
        if (normalizedOutgoingNodes.size() == 2) {
            // TODO: resistance in internal circuit
            // TODO: config library for all values
            // TODO: model as Diode?
            // (diode block entity)
            internalCircuit.addComponent(new VirtualResistor(resistance), normalizedOutgoingNodes.get(0), normalizedOutgoingNodes.get(1));
        }

        return internalCircuit;
    }
}
