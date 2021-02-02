package net.hellomouse.kontrol.logic.circuit.virtual.components;

import net.hellomouse.kontrol.logic.circuit.virtual.components.conditions.IBaseCondition;
import net.hellomouse.kontrol.logic.circuit.virtual.components.conditions.IVoltageDifferenceCondition;

/**
 * Diode component, modelled as voltage source
 * See IBaseCondition for specific javadoc on common component methods
 *
 * @see IBaseCondition
 * @author Bowserinator
 */
public class VirtualDiode extends VirtualVoltageSource implements IVoltageDifferenceCondition {
    private double V_forward;

    public VirtualDiode(double V_forward) {
        super(V_forward);
        this.V_forward = V_forward;
    }

    public void setVForward(double V) { V_forward = V; }
    public double getVForward() { return V_forward; }

    // Solving logic in VirtualCircuit
    @Override
    public boolean isNonLinear() { return true; }

    @Override
    public String toString() {
        return super.toString() + "\nV_fwd = " + V_forward + " V";
    }
}
