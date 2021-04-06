package net.hellomouse.c_interp.instructions.expression;

import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.instructions.AbstractInstruction;
import net.hellomouse.c_interp.interpreter.InterpreterState;

public class ExpressionInstruction extends AbstractInstruction {
    public final IExpression expression;

    public ExpressionInstruction(IExpression expression) {
        this.expression = expression;
    }

    public String toASMLine() {
        return getInstrName() + " " + expression;
    }

    public String getInstrName() {
        return "EXPR";
    }

    public void interpret(InterpreterState state) {
        state.log.definition("expression " + expression);
        expression.runtimeEval(state);
    }
}
