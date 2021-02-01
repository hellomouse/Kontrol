package net.hellomouse.kontrol.logic.circuit.virtual.components;

import net.hellomouse.kontrol.logic.circuit.virtual.components.conditions.IBaseCondition;
import net.hellomouse.kontrol.logic.circuit.virtual.components.conditions.IFixedVoltageCondition;

/**
 * Ground component
 * See IBaseCondition for specific javadoc on common component methods
 *
 * @see IBaseCondition
 * @author Bowserinator
 */
public class VirtualGround extends AbstractVirtualComponent implements IFixedVoltageCondition {
    public VirtualGround() {
        super();
    }

    /**
     * @see IBaseCondition#setNodes(int, int)
     * @param node1 Node to set to V = 0
     * @param node2 Unused, but override
     */
    @Override
    public void setNodes(int node1, int node2) {
        // Ground only uses node1
        super.setNodes(node1, node1);
    }

    // Needs to have setVoltage implemented, but should never be used
    @Override
    public void setVoltage(double v) {
        throw new IllegalStateException("Cannot call setVoltage on ground node");
    }

    // Ground has 0 voltage and energy, being a constrained node
    @Override
    public double getVoltage() { return 0.0; }

    @Override
    public double getEnergy() { return 0.0; }
}
