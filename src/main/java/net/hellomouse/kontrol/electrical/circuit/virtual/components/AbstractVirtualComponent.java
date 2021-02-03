package net.hellomouse.kontrol.electrical.circuit.virtual.components;

import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
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

    public double getPower() {
        return Math.abs(getVoltage() * getCurrent());
    }
    public double getVoltage() {
        return circuit.getNodalVoltage(node2) - circuit.getNodalVoltage(node1);
    }
    public double getCurrent() {
        return circuit.getCurrentThrough(node1, node2);
    }

    public void tick() {}

    public boolean requireTicking() {return false; }
    public boolean doesNumericIntegration() { return false; }
    public boolean isNonLinear() { return false; }

    public void setDisabled(boolean disabled) { this.disabled = disabled; }
    public boolean isDisabled() { return disabled; }

    public void setHiZ(boolean hiZ) { this.hiZ = hiZ; }
    public boolean isHiZ() { return hiZ; }

    public void setCircuit(VirtualCircuit c) { this.circuit = c; }

    public int getNode1() { return node1; }
    public int getNode2() { return node2; }

    public String toString() {
        return this.getClass().getSimpleName() + " from " + node1 + " to " + node2 +
                (disabled ? " (disabled)" : "") +
                (hiZ ? " (Hi-Z)" : "") + "\n" +
                "I = " + getCurrent() + " A  |  V = " + getVoltage() + " V";
    }
}
