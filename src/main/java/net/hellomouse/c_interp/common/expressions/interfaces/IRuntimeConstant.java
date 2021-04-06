package net.hellomouse.c_interp.common.expressions.interfaces;

import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;

/**
 * A constant value at runtime
 * @author Bowserinator
 */
public interface IRuntimeConstant extends IExpression {
    // TODO: remove this
    /**
     * String when the value is displayed with
     * its current type.
     * @return String
     */
    String getStringValue();

    /**
     * Deep copy the current runtime constant
     * @return Copy of this
     */
    IRuntimeConstant copy();

    /**
     * Type bound checks and any other checks to be
     * done post-operation on a value
     * @param type Type of the variable value is assigned to
     */
    IRuntimeConstant postOperation(AbstractTypeStorage type);
}
