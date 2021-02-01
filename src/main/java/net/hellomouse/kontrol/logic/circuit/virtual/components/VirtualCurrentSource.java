package net.hellomouse.kontrol.logic.circuit.virtual.components;

import net.hellomouse.kontrol.logic.circuit.virtual.components.conditions.IBaseCondition;
import net.hellomouse.kontrol.logic.circuit.virtual.components.conditions.ICurrentCondition;
import static net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuitConstants.UNKNOWN_ENERGY;

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

    public void setCurrent(double current) {
        if (this.current == 0.0 && current != 0.0)
            circuit.incEnergySources();
        else if (this.current != 0.0 && current == 0.0)
            circuit.decEnergySources();
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
}
