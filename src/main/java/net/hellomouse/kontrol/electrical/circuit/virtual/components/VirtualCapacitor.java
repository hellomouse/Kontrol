package net.hellomouse.kontrol.electrical.circuit.virtual.components;

import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.*;

import static net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuitConstants.CAPACITOR_INITIAL_R;
import static net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuitConstants.DT;


/**
 * Capacitor component.
 * See IBaseCondition for specific javadoc on common component methods
 *
 * @see IBaseCondition
 * @author Bowserinator
 */
public class VirtualCapacitor extends VirtualCurrentSource implements IResistanceCondition {
    private double capacitance;
    private double resistance = CAPACITOR_INITIAL_R;
    private double initialValue = 0.0;

    public VirtualCapacitor(double capacitance) {
        super(0.0); // Uncharged capacitor is short (0 V voltage source)
        this.capacitance = capacitance;
    }

    public void setVoltage(double voltage) {
        setCurrent(voltage / resistance);
        initialValue = voltage;
    }

    public double getCapacitance() {
        return capacitance;
    }
    public void setCapacitance(double C) { capacitance = C; }

    @Override
    public double getResistance() { return resistance; }

    @Override
    public void setResistance(double resistance) { this.resistance = resistance; }

    @Override
    public boolean requireTicking() { return true; }

    @Override
    public boolean doesNumericIntegration() { return true; }

    @Override
    public void initialUpdateEnergySourceCount() {
        circuit.incEnergySources();
    }

    @Override
    public void tick() {
        resistance = DT / capacitance;
        setCurrent(getVoltage() / getResistance());
    }

    @Override
    public double getEnergy() {
        // E = 1/2 * CV^2
        double V = getVoltage();
        return 0.5 * capacitance * V * V;
    }

    @Override
    public String toString() {
        return super.toString() + "\nC = " + capacitance + " C  |  V_0 = " + initialValue + " V";
    }
}
