package net.hellomouse.kontrol.logic.circuit.virtual.components;

import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCondition;
import static net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuitConstants.UNKNOWN_ENERGY;

public class VirtualCurrentSource extends AbstractVirtualComponent {
    private double current;

    public VirtualCurrentSource(double current) {
        super();
        this.current = current;
    }

    public void setCurrent(double current) {
        this.current = current;
        condition.value = current;
    }

    @Override
    public void setNodes(int node1, int node2) {
        super.setNodes(node1, node2);
        condition = new VirtualCondition(node1, node2, current, VirtualCondition.Condition.CURRENT);
    }

    @Override
    public double getCurrent() {
        return current;
    }

    @Override
    public double getEnergy() {
        return UNKNOWN_ENERGY;
    }
}