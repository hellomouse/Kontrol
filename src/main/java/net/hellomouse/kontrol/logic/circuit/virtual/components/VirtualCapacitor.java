package net.hellomouse.kontrol.logic.circuit.virtual.components;

import net.hellomouse.kontrol.logic.circuit.virtual.components.conditions.IBaseCondition;
import static net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuitConstants.DT;

/**
 * Capacitor component.
 * See IBaseCondition for specific javadoc on common component methods
 *
 * @see IBaseCondition
 * @author Bowserinator
 */
public class VirtualCapacitor extends VirtualVoltageSource {
    private double capacitance;
    private double prev = 0.0;

    public VirtualCapacitor(double capacitance) {
        super(0.0); // Uncharged capacitor is short (0 V voltage source)
        this.capacitance = capacitance;
    }

    public double getCapacitance() {
        return capacitance;
    }
    public void setCapacitance(double C) { capacitance = C; }

    @Override
    public boolean requireTicking() { return true; }

    // See VirtualCircuit for divergence checking logic
    @Override
    public boolean doesNumericIntegration() { return true; }

    @Override
    public void tick() {
        // I = C * dV / dt
        // Or V += I / C * dt (Euler approximation)
        double current = getCurrent() / capacitance * DT;
        current -= (current - prev) * 0.5;
        setVoltage(getVoltage() + current);
        prev = current;
    }

    @Override
    public double getEnergy() {
        // E = 1/2 * CV^2
        double V = getVoltage();
        return 0.5 * capacitance * V * V;
    }
}
