package net.hellomouse.c_interp.common.expressions;

import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.interpreter.InterpreterState;

public class StructOrUnionMemberExpression implements IExpression {
    public final AbstractTypeStorage type;
    public final String name;

    public StructOrUnionMemberExpression(AbstractTypeStorage type, String name) {
        this.type = type;
        this.name = name;
    }

    public AbstractTypeStorage getType() {
        return type;
    }

    public String toString() {
        return name;
    }

    public ConstantValue runtimeEval(InterpreterState state) { return null; }
}
