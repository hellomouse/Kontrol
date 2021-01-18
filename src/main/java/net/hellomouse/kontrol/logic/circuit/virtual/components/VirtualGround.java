package net.hellomouse.kontrol.logic.circuit.virtual.components;

import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCondition;

public class VirtualGround extends AbstractVirtualComponent {
    public VirtualGround() {
        super();
    }

    /**
     * Only node1 is used.
     * @param node1 Node to set to V = 0
     * @param node2 Unused, but override
     */
    public void setNodes(int node1, int node2) {
        super.setNodes(node1, node1);
        condition = new VirtualCondition(node1, node1, 0, VirtualCondition.Condition.FIXED_VOLTAGE);
    }

    /* Ground has 0 voltage and energy, being a constrained node */
    @Override
    public double getVoltage() { return 0.0; }

    @Override
    public double getEnergy() { return 0.0; }
}
