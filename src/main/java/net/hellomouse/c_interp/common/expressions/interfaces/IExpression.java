package net.hellomouse.c_interp.common.expressions.interfaces;

import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.interpreter.InterpreterState;

/**
 * An expression of any sort
 * @author Bowserinator
 */
public interface IExpression {
    /**
     * Get the resultant type of the expression when evaluated
     * @return AbstractType
     */
    AbstractTypeStorage getType();

    /**
     * <p>Evaluate the expression at runtime. Return null to
     * prevent assignment to a temporary register if the return
     * value is not meaningful or doesn't exist.</p>
     *
     * <p>Note returning null will still directly assign to a variable,
     * so additional manipulation of the expression, such as with function
     * calls, must be dealt with in StatementHandler</p>
     *
     * @param state InterpreterState
     * @return Resultant IRuntimeConstant value
     * @see net.hellomouse.c_interp.compiler.handlers.StatementHandler
     */
    IRuntimeConstant runtimeEval(InterpreterState state);
}
