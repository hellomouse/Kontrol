package net.hellomouse.kontrol.electrical.block.entity;

import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualCapacitor;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;

// TODO: implement polarized
public class CapacitorBlockEntity extends AbstractPolarizedElectricalBlockEntity {
    public CapacitorBlockEntity() {
        super(ElectricalBlockRegistry.CAPACITOR_BLOCK_ENTITY);
    }

    // TODO: require ticking ture

    @Override
    public VirtualCircuit getInternalCircuit() {
        internalCircuit.clear();

        if (normalizedOutgoingNodes.size() == 2) {
            sortOutgoingNodesByPolarity();
            internalCircuit.addComponent(new VirtualCapacitor(1.0), normalizedOutgoingNodes.get(0), normalizedOutgoingNodes.get(1));
        }

        return internalCircuit;
    }
}
