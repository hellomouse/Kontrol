package net.hellomouse.kontrol.logic.circuit.virtual.components;

import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuitConstants;
import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCondition;
import net.hellomouse.kontrol.logic.circuit.virtual.components.conditions.IBaseCondition;

import java.util.ArrayList;
import java.util.List;

import static net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuitConstants.OPEN_CIRCUIT_R;

/**
 * Inductor component.
 * See IBaseCondition for specific javadoc on common component methods
 *
 * @see IBaseCondition
 * @author Bowserinator
 */
public class VirtualInductor extends VirtualCurrentSource implements INumericIntegration {
    private double inductance;
    private double initialValue = 0.0;

    public VirtualInductor(double inductance) {
        super(0.0); // Uncharged capacitor is short (0 V voltage source)
        this.inductance = inductance;
    }

    public double getInductance() { return inductance; }
    public void setCapacitance(double L) { inductance = L; }

    @Override
    public boolean requireTicking() { return true; }

    // See VirtualCircuit for divergence checking logic
    @Override
    public boolean doesNumericIntegration() { return true; }

    @Override
    public void setCurrent(double current) {
        initialValue = current;
        super.setCurrent(current);
    }

    @Override
    public void tick() {
        // V = L * dI / dt
        // Or I += V / L * dt (Euler approximation)
        super.setCurrent(getCurrent() - getVoltage() / inductance * VirtualCircuitConstants.DT); // Don't alter initial state, use super
        checkDivergence();
    }

    @Override
    public double getEnergy() {
        // E = 1/2 * LI^2
        double I = getCurrent();
        return 0.5 * inductance * I * I;
    }

    public void checkDivergence() {
        ArrayList<Double> steadyStateVoltages = circuit.getSteadyStateNodalVoltages();
        double SS_voltage = steadyStateVoltages.get(node1) - steadyStateVoltages.get(node2);
        double SS_value = SS_voltage * OPEN_CIRCUIT_R; // Modelled as low value resistor at steady state, I = V / R = V * (1 / R)

        if (VirtualCondition.isDivergent(SS_value, initialValue, getCurrent()))
            super.setCurrent(SS_value); // Don't alter initial state, use super
    }
}
