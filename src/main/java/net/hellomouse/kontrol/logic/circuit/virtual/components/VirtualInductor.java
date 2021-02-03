package net.hellomouse.kontrol.logic.circuit.virtual.components;

import net.hellomouse.kontrol.logic.circuit.virtual.components.conditions.IBaseCondition;
import net.hellomouse.kontrol.logic.circuit.virtual.components.conditions.IResistanceCondition;

import static net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuitConstants.*;


/**
 * Inductor component.
 * See IBaseCondition for specific javadoc on common component methods
 *
 * @see IBaseCondition
 * @author Bowserinator
 */
public class VirtualInductor extends VirtualCurrentSource implements IResistanceCondition {
    private double inductance;
    private double initialValue = 0.0;
    private double resistance = INDUCTOR_INITIAL_R;

    public VirtualInductor(double inductance) {
        super(0.0); // Uncharged capacitor is short (0 V voltage source)
        this.inductance = inductance;
    }

    public double getInductance() { return inductance; }
    public void setCapacitance(double L) { inductance = L; }

    public double getResistance() { return resistance; }
    public void setResistance(double resistance) { this.resistance = resistance; }

    @Override
    public boolean requireTicking() { return true; }

    @Override
    public boolean doesNumericIntegration() { return true; }

    @Override
    public void setCurrent(double current) {
        initialValue = current;
        super.setCurrent(current);
    }

    @Override
    public void tick() {
        resistance = inductance / DT;
        super.setCurrent(getCurrent() - getVoltage() / getResistance());
    }

    @Override
    public double getEnergy() {
        // E = 1/2 * LI^2
        double I = getCurrent();
        return 0.5 * inductance * I * I;
    }

    @Override
    public String toString() {
        return super.toString() + "\nL = " + inductance + " H  |  I_0 = " + initialValue + " A";
    }
}
