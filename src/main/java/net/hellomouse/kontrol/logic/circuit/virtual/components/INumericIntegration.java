package net.hellomouse.kontrol.logic.circuit.virtual.components;

/**
 * A component that does numeric integration. These methods are used
 * for steady state divergence checking.
 * @author Bowserinator
 */
public interface INumericIntegration {
    /**
     * If the component is diverging then snap it to a safe
     * value, usually a steady state. This method is not called automatically
     * and is usually called at the end of tick()
     * @see net.hellomouse.kontrol.logic.circuit.virtual.VirtualCondition.isDivergent()
     */
    void checkDivergence();
}
