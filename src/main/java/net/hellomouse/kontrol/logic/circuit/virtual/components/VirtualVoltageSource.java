package net.hellomouse.kontrol.logic.circuit.virtual.components;

import net.hellomouse.kontrol.logic.circuit.virtual.components.conditions.IBaseCondition;
import net.hellomouse.kontrol.logic.circuit.virtual.components.conditions.IVoltageDifferenceCondition;
import static net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuitConstants.UNKNOWN_ENERGY;

/**
 * Voltage source component, recommended to extend this for most voltage
 * source based elements.
 *
 * See IBaseCondition for specific javadoc on common component methods
 *
 * @see IBaseCondition
 * @author Bowserinator
 */
public class VirtualVoltageSource extends AbstractVirtualComponent implements IVoltageDifferenceCondition {
    private double voltage;

    public VirtualVoltageSource(double voltage) {
        super();
        this.voltage = voltage;
    }

    public void setVoltage(double voltage) {
        if (this.voltage == 0.0 && voltage != 0.0)
            circuit.incEnergySources();
        else if (this.voltage != 0.0 && voltage == 0.0)
            circuit.decEnergySources();
        this.voltage = voltage;
    }

    @Override
    public double getVoltage() {
        if (isHiZ())
            return super.getVoltage();
        if (isDisabled())
            return 0.0;
        return voltage;
    }

    @Override
    public double getEnergy() {
        // Voltage sources don't store energy
        return UNKNOWN_ENERGY;
    }
}
