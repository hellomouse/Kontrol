package net.hellomouse.kontrol.logic.circuit.virtual.components;

import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCondition;
import static net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuitConstants.UNKNOWN_ENERGY;

public class VirtualResistor extends AbstractVirtualComponent {
    private double resistance;

    public VirtualResistor(double resistance) {
        super();
        this.resistance = resistance;
    }

    public double getResistance() {
        return resistance;
    }

    public void setResistance(double resistance) {
        this.resistance = resistance;
        condition.value = resistance;
    }

    @Override
    public void setNodes(int node1, int node2) {
        super.setNodes(node1, node2);
        condition = new VirtualCondition(node1, node2, resistance, VirtualCondition.Condition.RESISTANCE);
    }

    @Override
    public double getEnergy() {
        return UNKNOWN_ENERGY;
    }
}
