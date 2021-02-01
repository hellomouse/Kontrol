package net.hellomouse.kontrol.logic.circuit.virtual.components.conditions;

/**
 * VirtualComponents with this interface will have a line in the matrix
 * for KCL performed with current calculated from V / R.
 *
 * Conflicts with all other conditions except for ICurrentCondition
 * (Ie, cannot be implemented with any other condition interface)
 * This is done via the _preventDuplicate method.
 *
 * @see notes.md
 * @author Bowserinator
 */
public interface IResistanceCondition extends IBaseCondition {
    default float _preventDuplicate() { return 0.0f; }

    double getResistance();
    void setResistance(double resistance);
}

