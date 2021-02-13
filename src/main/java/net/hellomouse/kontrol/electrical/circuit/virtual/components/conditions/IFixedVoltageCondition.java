package net.hellomouse.kontrol.electrical.circuit.virtual.components.conditions;

/**
 * VirtualComponents with this interface will have a line in the matrix
 * for a fixed nodal voltage (Node = V)
 *
 * Conflicts with all other conditions
 * (Ie, should not be implemented with any other condition interface)
 *
 * @author Bowserinator
 */
public interface IFixedVoltageCondition {
    double getVoltage();
    void setVoltage(double voltage);
}
