package net.hellomouse.kontrol.logic.circuit.virtual.components.conditions;

import org.ejml.simple.SimpleMatrix;

/**
 * VirtualComponents with this interface will be processed depending
 * on a custom function defined by the user
 *
 * Conflicts with all other conditions
 * (Ie, cannot be implemented with any other condition interface)
 * This is done via the _preventDuplicate method.
 *
 * To use a custom condition, you should uncomment the custom condition
 * solver method in VirtualCondition and uncomment the solve line in VirtualCircuit
 *
 * @see notes.md
 * @author Bowserinator
 */
public interface ICustomCondition {
    default char _preventDuplicate() { return ' '; }

    void modifyMatrix(SimpleMatrix matrix, SimpleMatrix solutions);
}
