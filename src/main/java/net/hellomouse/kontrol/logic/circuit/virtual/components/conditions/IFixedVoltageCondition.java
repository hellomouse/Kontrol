package net.hellomouse.kontrol.logic.circuit.virtual.components.conditions;

/**
 * VirtualComponents with this interface will have a line in the matrix
 * for a fixed nodal voltage (Node = V)
 *
 * Conflicts with all other conditions
 * (Ie, cannot be implemented with any other condition interface)
 * This is done via the _preventDuplicate method.
 *
 * @see notes.md
 * @author Bowserinator
 */
public interface IFixedVoltageCondition {
    default int _preventDuplicate() { return 0; }

    double getVoltage();
    void setVoltage(double voltage);
}
