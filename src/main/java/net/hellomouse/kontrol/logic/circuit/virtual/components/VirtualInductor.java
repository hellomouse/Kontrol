package net.hellomouse.kontrol.logic.circuit.virtual.components;

import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuitConstants;

public class VirtualInductor extends VirtualCurrentSource {
    private double inductance;

    public VirtualInductor(double inductance) {
        super(0.0); // Uncharged capacitor is short (0 V voltage source)
        this.inductance = inductance;
    }

    public double getInductance() {
        return inductance;
    }
    public void setCapacitance(double L) {
        inductance = L;
        condition.value = L;
    }

    @Override
    public boolean requireTicking() { return true; }

    // See VirtualCircuit for divergence checking logic
    @Override
    public boolean doesNumericIntegration() { return true; }

    @Override
    public void tick() {
        // V = L * dI / dt
        // Or I += V / L * dt (Euler approximation)
        setCurrent(getCurrent() - getVoltage() / inductance * VirtualCircuitConstants.DT);
    }

    @Override
    public double getEnergy() {
        // E = 1/2 * LI^2
        double I = getCurrent();
        return 0.5 * inductance * I * I;
    }
}
