package net.hellomouse.kontrol.electrical.circuit.virtual.components;

import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.IBaseCondition;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.IResistanceCondition;

/**
 * Diode component, modelled as voltage source
 * See IBaseCondition for specific javadoc on common component methods
 *
 * @see IBaseCondition
 * @author Bowserinator
 */
public class VirtualDiode extends VirtualCurrentSource implements IResistanceCondition {
    private double V_forward;
    private double resistance = 10;

    public VirtualDiode(double V_forward) {
        super(-V_forward / 10);
        this.V_forward = V_forward;
    }

    public void setVForward(double V) { V_forward = V; }
    public double getVForward() { return V_forward; }

    public double getResistance() { return resistance; }
    public void setResistance(double resistance) { this.resistance = resistance; }

    // Solving logic in VirtualCircuit
    @Override
    public boolean isNonLinear() { return true; }

    @Override
    public String toString() {
        return super.toString() + "\nV_fwd = " + V_forward + " V";
    }
}
