package net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions;

/**
 * VirtualComponents with this interface will have a line in the matrix
 * for KCL modified so total current out += current source
 *
 * Conflicts with all other conditions except for IResistanceCondition
 * (Ie, should not be implemented with any other condition interface)
 *
 * @author Bowserinator
 */
public interface ICurrentCondition {
    void setCurrent(double current);
    double getCurrent();
}
