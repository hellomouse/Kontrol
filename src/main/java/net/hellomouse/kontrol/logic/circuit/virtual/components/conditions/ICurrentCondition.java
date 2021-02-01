package net.hellomouse.kontrol.logic.circuit.virtual.components.conditions;

/**
 * VirtualComponents with this interface will have a line in the matrix
 * for KCL modified so total current out += current source
 *
 * Conflicts with all other conditions except for IResistanceCondition
 * (Ie, cannot be implemented with any other condition interface)
 * This is done via the _preventDuplicate method.
 *
 * @see notes.md
 * @author Bowserinator
 */
public interface ICurrentCondition {
    default float _preventDuplicate() { return 0.0f; }

    void setCurrent(double current);
    double getCurrent();
}
