package net.hellomouse.kontrol.electrical.circuit.virtual.components;

import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.IBaseCondition;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.IResistanceCondition;
import static net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuitConstants.UNKNOWN_ENERGY;


/**
 * Resistor component, recommended to extend this for most resistor
 * like elements.
 *
 * See IBaseCondition for specific javadoc on common component methods
 *
 * @see IBaseCondition
 * @author Bowserinator
 */
public class VirtualResistor extends AbstractVirtualComponent implements IResistanceCondition {
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
    }

    @Override
    public double getCurrent() {
        // In direction of node1 to node2
        // voltage across resistor is defined as negative but
        // current positive, so we take -1 * voltage
        return -getVoltage() / getResistance();
    }

    @Override
    public double getEnergy() {
        // Resistors don't store energy
        return UNKNOWN_ENERGY;
    }

    @Override
    public String toString() {
        return super.toString() + "\nR = " + resistance + " ohms";
    }
}
