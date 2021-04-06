package net.hellomouse.c_interp.common.expressions;

import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.interfaces.IRuntimeConstant;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.interpreter.InterpreterState;

public class FunctionReturnExpression implements IExpression {
    public final AbstractTypeStorage returnType;

    public FunctionReturnExpression(AbstractTypeStorage returnType) {
        this.returnType = returnType;
    }

    public AbstractTypeStorage getType() {
        return returnType;
    }

    @Override
    public IRuntimeConstant runtimeEval(InterpreterState state) {
        return state.frames.pop().getReturnValue();
    }

    public String toString() {
        return "get return value";
    }
}
