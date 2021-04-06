package net.hellomouse.c_interp.instructions.statement;

import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.interfaces.IRuntimeConstant;
import net.hellomouse.c_interp.common.expressions.storage.Variable;
import net.hellomouse.c_interp.instructions.AbstractInstruction;
import net.hellomouse.c_interp.interpreter.InterpreterState;
import net.hellomouse.c_interp.interpreter.InterpreterUtil;

public class RegisterInstruction extends AbstractInstruction {
    public final int register;
    public final IExpression expression;
    public final String scopeId;

    public RegisterInstruction(int register, IExpression expression, String scopeId) {
        this.register = register;
        this.expression = expression;
        this.scopeId = scopeId;
    }
    public String toASMLine() {
        return getInstrName() + "," + register + " = " + expression;
    }
    public String getInstrName() {
        return "REGISTER";
    }

    public void interpret(InterpreterState state) {
        state.log.register("register assignment $" + register  + " = " + expression);

        IRuntimeConstant val = expression.runtimeEval(state);
        if (val != null) val = val.copy();

        if (val != null) {
            state.addVariable(new Variable(InterpreterUtil.scopeVar(register + "$", scopeId), null, val));
        }
    }
}
