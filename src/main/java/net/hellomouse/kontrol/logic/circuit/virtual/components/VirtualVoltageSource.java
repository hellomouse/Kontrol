package net.hellomouse.kontrol.logic.circuit.virtual.components;

import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCondition;
import static net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuitConstants.UNKNOWN_ENERGY;

public class VirtualVoltageSource extends AbstractVirtualComponent {
    private double voltage;

    public VirtualVoltageSource(double voltage) {
        super();
        this.voltage = voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
        condition.value = voltage;
    }

    @Override
    public void setNodes(int node1, int node2) {
        super.setNodes(node1, node2);
        condition = new VirtualCondition(node1, node2, voltage, VirtualCondition.Condition.VOLTAGE_DIFFERENCE);
    }

    @Override
    public double getVoltage() {
        if (this.isDisabled())
            return super.getVoltage();
        return voltage;
    }

    @Override
    public double getEnergy() {
        return UNKNOWN_ENERGY;
    }
}