package net.hellomouse.kontrol.logic.circuit.virtual.components;

import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCondition;

/**
 * An abstract electrical component used for computation
 * only (should contain NO references to Minecraft world)
 */
public abstract class AbstractVirtualComponent {
    protected int node1, node2;
    protected VirtualCondition condition;
    protected VirtualCircuit circuit;
    protected boolean disabled = false;

    // --- Run when added as a component --- \\
    public void setNodes(int node1, int node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    public void setCircuit(VirtualCircuit c) {
        this.circuit = c;
    }

    // --- Getters and unique calculations --- \\\
    public abstract double getEnergy();

    public double getVoltage() {
        return circuit.getNodalVoltage(node2) - circuit.getNodalVoltage(node1);
    }

    public double getCurrent() {
        if (condition.type == VirtualCondition.Condition.RESISTANCE)  // I = V / R
            return getVoltage() / condition.value;
        return circuit.getCurrentThrough(condition.node1, condition.node2);
    }

    public VirtualCondition getCondition() {
        return condition;
    }

    // ---- Special methods ---- \\
    public void tick() {}

    // ---- Properties ---- \\
    public boolean requireTicking() { return false; }
    public boolean doesNumericIntegration() { return false; }
    public boolean isNonLinear() { return false; }

    public void setDisabled(boolean disabled) { this.disabled = disabled; }
    public boolean isDisabled() { return disabled; }
}
