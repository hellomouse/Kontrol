package net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions;

import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.AbstractVirtualComponent;

/**
 * Base interface extended by other IBaseConditions. This should
 * only be extended by other condition interfaces and only be implemented
 * by AbstractVirtualComponent; all other components should use the interface
 * that corresponds to their condition.
 *
 * All condition interfaces should extend this interface.
 *
 * Implementing this interface by itself does nothing as the element
 * will be skipped over during matrix solving.
 *
 * @author Bowserinator
 */
public interface IBaseCondition {
    /**
     * Assigns nodes to the object. An object can have at most
     * two nodes; more complex multi-terminal devices should be modelled
     * using simple two-node components. Said modelling of multi-component
     * objects will have to be done in the block entity, not the virtual circuit.
     *
     * See PolarityTests for nodal polarities of various objects.
     *
     * This method is run in Virtual Circuit when an
     * component is automatically added
     *
     * @param node1 Node ID 1 (generally positive terminal)
     * @param node2 Node ID 2 (generally negative terminal)
     * @see VirtualCircuit#addComponent(AbstractVirtualComponent, int, int)
     */
    void setNodes(int node1, int node2);

    /**
     * Energy computation, return VirtualCircuitConstants.UNKNOWN_ENERGY if
     * the component doesn't have a stored energy defined (ie, resistors).
     * This is stored ENERGY, not POWER.
     * @return Stored energy (J)
     */
    double getEnergy();

    /**
     * Returns power either generated or dissipated by the component.
     * This is an absolute value quantity with no polarity, meaning
     * it is up to individual components to interpret this value.
     * @return Power (W)
     */
   double getPower();

    /**
     * Defined as V_node2 - V_node1, can be negative depending
     * on polarity of nodes. This also means swapping node id order
     * will swap the sign on the voltage. Overrides should maintain
     * this polarity convention.
     * @return Voltage across component (V)
     */
    double getVoltage();

    /**
     * Defined as positive if current goes from V_node1 to V_node2, otherwise
     * negative. Swapping node order will swap sign on the current
     *
     * Note getCurrentThrough uses a series resistor
     * to calculate current for most components, so getCurrent() must have an
     * override in VirtualResistor or any component that implements VirtualResistor
     *
     * @return Current through component (A)
     */
    double getCurrent();

    /**
     * Function to run every time the circuit is tick, can be used, for example,
     * to update component values
     */
    void tick();

    /**
     * Does the component update itself every tick? Used for certain optimizations,
     * as well as calling tick() when the circuit is ticked.
     * @return Require tick?
     */
    boolean requireTicking();

    /**
     * Does the component do numeric integration? Currently this is
     * not used, but may be used in the future
     * @return Does the component do numeric integration?
     */
    boolean doesNumericIntegration();

    /**
     * Is the component nonlinear? Used for certain circuit optimizations and hardcoded
     * solving methods in VirtualCircuit
     * @return Is nonlinear?
     */
    boolean isNonLinear();
}
