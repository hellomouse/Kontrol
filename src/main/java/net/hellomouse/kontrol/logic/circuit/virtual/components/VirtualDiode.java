package net.hellomouse.kontrol.logic.circuit.virtual.components;

public class VirtualDiode extends VirtualVoltageSource {
    private double V_forward;

    public VirtualDiode(double V_forward) {
        super(V_forward);
        this.V_forward = V_forward;
    }

    public void setVForward(double V) { V_forward = V; }
    public double getVForward() { return V_forward; }

    @Override
    public boolean isNonLinear() { return true; }
}
