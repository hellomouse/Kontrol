package net.hellomouse.kontrol.logic.circuit.virtual.components;

import net.hellomouse.kontrol.logic.circuit.virtual.components.conditions.IBaseCondition;
import net.hellomouse.kontrol.logic.circuit.virtual.components.conditions.IFixedVoltageCondition;

import static net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuitConstants.UNKNOWN_ENERGY;

/**
 * Fixed nodal voltage
 * See IBaseCondition for specific javadoc on common component methods
 *
 * @see IBaseCondition
 * @author Bowserinator
 */
public class VirtualFixedNode extends AbstractVirtualComponent implements IFixedVoltageCondition {
    private double voltage;

    public VirtualFixedNode(double voltage) {
        super();
        this.voltage = voltage;
    }

    /**
     * @see IBaseCondition#setNodes(int, int)
     * @param node1 Node to set to V = voltage
     * @param node2 Unused, but override
     */
    @Override
    public void setNodes(int node1, int node2) {
        // Fixed node only uses node1
        super.setNodes(node1, node1);
    }

    @Override
    public void setVoltage(double voltage) {
        if (this.voltage == 0.0 && voltage != 0.0)
            circuit.incEnergySources();
        else if (this.voltage != 0.0 && voltage == 0.0)
            circuit.decEnergySources();
        this.voltage = voltage;
    }

    @Override
    public double getVoltage() { return voltage; }

    @Override
    public double getEnergy() {
        return voltage == 0.0 ? 0.0 : UNKNOWN_ENERGY;
    }
}
