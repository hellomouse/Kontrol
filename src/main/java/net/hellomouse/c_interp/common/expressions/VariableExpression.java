package net.hellomouse.c_interp.common.expressions;

import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.interfaces.IRuntimeConstant;
import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.common.expressions.storage.PassedParameter;
import net.hellomouse.c_interp.common.expressions.storage.Variable;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.interpreter.InterpreterState;
import net.hellomouse.c_interp.interpreter.InterpreterUtil;

public class VariableExpression implements IExpression {
    public final String variableName;
    public final Variable variable;
    public final String scopeId;

    public VariableExpression(String variableName, Variable variable, String scopeId) {
        this.variableName = variableName;
        this.variable = variable;

        if (variable instanceof PassedParameter)
            this.scopeId = "";
        else
            this.scopeId = scopeId;
    }

    public AbstractTypeStorage getType() { return variable.getType(); }

    public String toString() { return variableName; }

    public String getRuntimeVarName() {
        return InterpreterUtil.scopeVar(variableName, scopeId);
    }

    public IRuntimeConstant runtimeEval(InterpreterState state) {
        String variableName = getRuntimeVarName();
        Variable var = state.getCurrentScope().getVariable(variableName);

        state.interpreter.listeners.onVariableAccess(var);

        // TODO: explain compiler checks valid, at runtime must be goto skip
        if (var == null) {
            return new ConstantValue(state.interpreter.machine, "0"); // TODO: return whatever
        }

        IExpression value = var.getValue();

        if (value != null) {
            return value.runtimeEval(state).copy();
        }
        return new ConstantValue(state.interpreter.machine, "0"); // TODO: return whatever
    }
}
