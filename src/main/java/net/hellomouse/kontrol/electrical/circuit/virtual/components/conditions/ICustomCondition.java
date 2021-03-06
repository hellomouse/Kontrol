package net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions;

import org.ejml.simple.SimpleMatrix;

/**
 * VirtualComponents with this interface will be processed depending
 * on a custom function defined by the user
 *
 * Conflicts with all other conditions
 * (Ie, should not be implemented with any other condition interface)
 *
 * To use a custom condition, you should uncomment the custom condition
 * solver method in VirtualCondition and uncomment the solve line in VirtualCircuit
 *
 * @author Bowserinator
 */
public interface ICustomCondition {
    void modifyMatrix(SimpleMatrix matrix, SimpleMatrix solutions);
}
