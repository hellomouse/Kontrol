package net.hellomouse.kontrol.electrical.circuit.virtual.components;

import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuitConstants;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions.IBaseCondition;


/**
 * An abstract electrical component used for computation
 * only (should contain NO references to Minecraft world)
 *
 * In general, node1 is the positive terminal and node2
 * is the negative. This varies, see tests/PolarityTests
 * for more polarity info
 *
 * JavaDoc for methods can be found in IBaseCondition
 * @see IBaseCondition
 * @author Bowserinator
 */
public abstract class AbstractVirtualComponent implements IBaseCondition {
    protected int node1, node2;
    protected VirtualCircuit circuit;

    protected boolean disabled = false; // Disabled behavior depends in component
    protected boolean hiZ = false;      // High impedance, replaced with high value resistor in solving

    public void setNodes(int node1, int node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    // --- Info getters ---- \\
    public double getPower() {
        return Math.abs(getVoltage() * getCurrent());
    }
    public double getVoltage() {
        return circuit.getNodalVoltage(node2) - circuit.getNodalVoltage(node1);
    }
    public double getCurrent() {
        return circuit.getCurrentThrough(node1, node2);
    }
    public double getEnergy() { return VirtualCircuitConstants.UNKNOWN_ENERGY; }


    // --- Component properties --- \\
    public boolean requireTicking() {return false; }
    public boolean doesNumericIntegration() { return false; }
    public boolean isNonLinear() { return false; }


    // --- Energy source count syncing --- \\

    /** Apply any updates to energy source count when added, usually uses updateCircuitEnergySourceCount with prevValue = 0.0 */
    public void initialUpdateEnergySourceCount() {}

    /**
     * General helper method to update energy source count
     * @param prevValue Old energy source value (ie, voltage source voltage or current source current)
     * @param newValue New energy source value
     */
    protected void updateCircuitEnergySourceCount(double prevValue, double newValue) {
        if (prevValue == 0.0 && newValue != 0.0)
            circuit.incEnergySources();
        else if (prevValue != 0.0 && newValue == 0.0)
            circuit.decEnergySources();
    }

    /**
     * Update energy source count automatically using updateCircuitEnergySourceCount when either disabled or hiZ
     * is changed. Both the old and new state must be provided, if there is no change make both parameters the same.
     * Value is the value of the energy source (ie, voltage source voltage or current source current). Use this in an
     * overridden setHiZ and setDisabled method
     * @param oldDisabled Old disabled state
     * @param newDisabled New disabled state
     * @param oldHiZ Old hiZ state
     * @param newHiz New hiZ state
     * @param value Value of the energy source
     */
    protected void updateEnergySourcesOnStateChange(boolean oldDisabled, boolean newDisabled, boolean oldHiZ, boolean newHiz, double value) {
        if (oldDisabled == newDisabled && oldHiZ == newHiz)
            return;

        // Not disabled state => disabled state
        if (!oldDisabled && !oldHiZ)
            updateCircuitEnergySourceCount(value, 0.0);
        // Disabled state => not disabled state
        else if (!newHiz && !newDisabled)
            updateCircuitEnergySourceCount(0.0, value);
    }


    // --- States --- \\
    public void setDisabled(boolean disabled) { this.disabled = disabled; }
    public boolean isDisabled() { return disabled; }

    public void setHiZ(boolean hiZ) { this.hiZ = hiZ; }
    public boolean isHiZ() { return hiZ; }


    // --- Misc setters / getters --- \\
    public void setCircuit(VirtualCircuit c) { this.circuit = c; }

    public int getNode1() { return node1; }
    public int getNode2() { return node2; }


    // --- Simulation --- \\
    public void tick() {}


    // --- Information --- \\
    public String toString() {
        return this.getClass().getSimpleName() + " from " + node1 + " to " + node2 +
                (disabled ? " (disabled)" : "") +
                (hiZ ? " (Hi-Z)" : "") + "\n" +
                "I = " + getCurrent() + " A  |  V = " + getVoltage() + " V";
    }
}
