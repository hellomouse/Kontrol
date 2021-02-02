package net.hellomouse.kontrol.logic.circuit.virtual.components;

import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCondition;
import net.hellomouse.kontrol.logic.circuit.virtual.components.conditions.IBaseCondition;

import java.util.ArrayList;
import java.util.List;

import static net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuitConstants.DT;

/**
 * Capacitor component.
 * See IBaseCondition for specific javadoc on common component methods
 *
 * @see IBaseCondition
 * @author Bowserinator
 */
public class VirtualCapacitor extends VirtualVoltageSource implements INumericIntegration {
    private double capacitance;
    private double prev = 0.0;
    private double initialValue = 0.0;

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

    @Override
    public boolean doesNumericIntegration() { return true; }

    @Override
    public void setVoltage(double voltage) {
        initialValue = voltage;
        super.setVoltage(voltage);
    }

    @Override
    public void tick() {
        // I = C * dV / dt
        // Or V += I / C * dt (Euler approximation)
        double current = getCurrent() / capacitance * DT;
        current -= (current - prev) * 0.5;
        super.setVoltage(getVoltage() + current); // Don't alter initial state, use super
        prev = current;

        checkDivergence();
    }

    @Override
    public double getEnergy() {
        // E = 1/2 * CV^2
        double V = getVoltage();
        return 0.5 * capacitance * V * V;
    }

    public void checkDivergence() {
        ArrayList<Double> steadyStateVoltages = circuit.getSteadyStateNodalVoltages();
        double SS_value = steadyStateVoltages.get(node1) - steadyStateVoltages.get(node2);

        if (VirtualCondition.isDivergent(SS_value, initialValue, getVoltage()))
            super.setVoltage(SS_value); // Don't alter initial state, use super
    }
}
