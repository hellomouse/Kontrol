package net.hellomouse.kontrol.electrical.circuit.virtual.components;

import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.IBaseCondition;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.IFixedVoltageCondition;

import static net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuitConstants.UNKNOWN_ENERGY;


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

    @Override
    public void setNodes(int node1, int node2) {
        // Only uses node1
        super.setNodes(node1, node1);
    }

    @Override
    public void setVoltage(double voltage) {
        updateCircuitEnergySourceCount(this.voltage, voltage);
        this.voltage = voltage;
    }

    @Override
    public double getVoltage() { return voltage; }

    @Override
    public double getEnergy() {
        return voltage == 0.0 ? 0.0 : UNKNOWN_ENERGY;
    }

    @Override
    public void initialUpdateEnergySourceCount() {
        updateCircuitEnergySourceCount(0.0, voltage);
    }

    @Override
    public void setDisabled(boolean disabled) {
        updateEnergySourcesOnStateChange(this.disabled, disabled, hiZ, hiZ, voltage);
        super.setDisabled(disabled);
    }

    @Override
    public void setHiZ(boolean hiZ) {
        updateEnergySourcesOnStateChange(disabled, disabled, this.hiZ, hiZ, voltage);
        super.setHiZ(hiZ);
    }
}
