package net.hellomouse.kontrol.electrical.circuit.virtual.components;

import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.IBaseCondition;


/**
 * Diode component, modelled as voltage source
 * See IBaseCondition for specific javadoc on common component methods
 *
 * @see IBaseCondition
 * @author Bowserinator
 */
public class VirtualDiode extends VirtualVoltageSource {
    private double V_forward;

    public VirtualDiode(double V_forward) {
        super(V_forward);
        this.V_forward = V_forward;
    }

    public void setVForward(double V) { V_forward = V; }
    public double getVForward() { return V_forward; }

    public boolean shouldBeHiZ() {
        // Voltage drops when HiZ, and rises when voltage source, so getVoltage() is not consistent
        double V = circuit.getNodalVoltage(node1) - circuit.getNodalVoltage(node2);
        double I = getCurrent();

        // Enable diode if forward voltage reached
        // Multiply voltage by very small leeway to account for floating point errors
        return V * 1.00001 < getVForward() || I < 0;
    }

    // Solving logic is in VirtualCircuit
    @Override
    public boolean isNonLinear() { return true; }

    @Override
    public String toString() {
        return super.toString() + "\nV_fwd = " + V_forward + " V";
    }

    // Smaller than open circuit impedance of switches and such
    @Override
    public double getHiZR() { return 1e6; }
}
