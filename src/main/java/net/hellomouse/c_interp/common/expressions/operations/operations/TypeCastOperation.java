package net.hellomouse.c_interp.common.expressions.operations.operations;

import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.interfaces.IRuntimeConstant;
import net.hellomouse.c_interp.common.expressions.operations.abstracts.AbstractUnaryOperation;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.interpreter.InterpreterState;

/**
 * Casting between different types.
 * @author Bowserinator
 */
public class TypeCastOperation extends AbstractUnaryOperation {
    public final AbstractTypeStorage type;

    public TypeCastOperation(AbstractTypeStorage type, IExpression operand, Machine machine) {
        super(34, operand, machine, "()");
        this.type = type;
    }

    @Override
    public AbstractTypeStorage getType() { return type; }

    @Override
    public IRuntimeConstant runtimeEval(InterpreterState state) {
        // TODO
        return operands.get(0).runtimeEval(state);
    }

    @Override
    public String toString() {
        return "((" + type + ") " + " " + operands.get(0) + ")";
    }
}
