package net.hellomouse.kontrol.electrical.block.entity;

import net.hellomouse.kontrol.electrical.circuit.CircuitValues;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualGround;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualResistor;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;


public class ElectricalGroundEntity extends WireBlockEntity {
    public ElectricalGroundEntity() {
        super(ElectricalBlockRegistry.ELECTRICAL_GROUND_ENTITY);
    }

    @Override
    public VirtualCircuit getInternalCircuit() {
        // [Internal circuit]:
        // - 1 internal node at center
        // - Up to 6 outgoing nodes on each side
        // - Center node is grounded

        internalCircuit.clear();
        for (int outNode : normalizedOutgoingNodes)
            internalCircuit.addComponent(new VirtualResistor(CircuitValues.LOW_VOLTAGE_RESISTANCE), -1, outNode);
        internalCircuit.addComponent(new VirtualGround(), -1, -1);
        return internalCircuit;
    }

    @Override
    public boolean canStartFloodfill() { return true; }
}
