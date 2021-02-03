package net.hellomouse.kontrol.logic.circuit.virtual.components.conditions;

/**
 * VirtualComponents with this interface will have a line in the matrix
 * for KCL performed with current calculated from V / R.
 *
 * Conflicts with all other conditions except for ICurrentCondition
 * (Ie, should not be implemented with any other condition interface)
 *
 * @author Bowserinator
 */
public interface IResistanceCondition extends IBaseCondition {
    double getResistance();
    void setResistance(double resistance);
}
