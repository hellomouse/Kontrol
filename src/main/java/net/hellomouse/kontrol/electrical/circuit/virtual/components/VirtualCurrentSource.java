package net.hellomouse.kontrol.electrical.circuit.virtual.components;

import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.IBaseCondition;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.ICurrentCondition;

import static net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuitConstants.UNKNOWN_ENERGY;


/**
 * Current source component, recommended to extend this for most current
 * source based elements.
 *
 * See IBaseCondition for specific javadoc on common component methods
 *
 * @see IBaseCondition
 * @author Bowserinator
 */
public class VirtualCurrentSource extends AbstractVirtualComponent implements ICurrentCondition {
    private double current;

    public VirtualCurrentSource(double current) {
        super();
        this.current = current;
    }

    @Override
    public void setCurrent(double current) {
        updateCircuitEnergySourceCount(this.current, current);
        this.current = current;
    }

    @Override
    public double getCurrent() {
        if (isDisabled() || isHiZ())
            return 0.0;
        return current;
    }

    @Override
    public double getEnergy() {
        // Current source doesn't store energy
        return UNKNOWN_ENERGY;
    }

    @Override
    public void initialUpdateEnergySourceCount() {
        updateCircuitEnergySourceCount(0.0, current);
    }

    @Override
    public void setDisabled(boolean disabled) {
        updateEnergySourcesOnStateChange(this.disabled, disabled, hiZ, hiZ, current);
        super.setDisabled(disabled);
    }

    @Override
    public void setHiZ(boolean hiZ) {
        updateEnergySourcesOnStateChange(disabled, disabled, this.hiZ, hiZ, current);
        super.setHiZ(hiZ);
    }
}
