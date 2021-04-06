package net.hellomouse.c_interp.common.expressions;

import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.interfaces.IRuntimeConstant;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.interpreter.InterpreterState;
import net.hellomouse.c_interp.interpreter.InterpreterUtil;

public class RegisterExpression implements IExpression {
    public final int register;
    public final String scopeId;

    public RegisterExpression(int register, String scopeId) {
        this.register = register;
        this.scopeId = scopeId;
    }


    public AbstractTypeStorage getType() { return null; }

    public String toString() { return "$" + register; }

    public IRuntimeConstant runtimeEval(InterpreterState state) {
        return (IRuntimeConstant)state.getCurrentScope().variables.get(InterpreterUtil.scopeVar(register + "$", scopeId)).getValue();
    }
}
