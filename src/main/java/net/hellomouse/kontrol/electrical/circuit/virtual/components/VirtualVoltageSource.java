package net.hellomouse.kontrol.electrical.circuit.virtual.components;

import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.IBaseCondition;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.IVoltageDifferenceCondition;
import static net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuitConstants.UNKNOWN_ENERGY;

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

    @Override
    public void initialUpdateEnergySourceCount() { updateCircuitEnergySourceCount(0.0, voltage); }

    @Override
    public void setVoltage(double voltage) {
        updateCircuitEnergySourceCount(this.voltage, voltage);
        this.voltage = voltage;
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
