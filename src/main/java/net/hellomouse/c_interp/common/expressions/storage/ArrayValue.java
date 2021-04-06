package net.hellomouse.c_interp.common.expressions.storage;

import net.hellomouse.c_interp.common.expressions.interfaces.ICompileTimeValue;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.common.storage_types.ArrayTypeStorage;
import net.hellomouse.c_interp.interpreter.InterpreterState;

import java.util.ArrayList;
import java.util.StringJoiner;

public class ArrayValue implements ICompileTimeValue {
    public ArrayList<IExpression> values;
    public final ArrayTypeStorage type;

    public ArrayValue(ArrayTypeStorage arrayType, ArrayList<IExpression> values) {
        this.type = arrayType;
        this.values = values;
    };

    public AbstractTypeStorage getType() { return type; }

    public String toString() {
        StringJoiner str = new StringJoiner(",");
        for (IExpression value : values)
            str.add(value.toString());
        return "{" + str.toString() + "}";
    }

    public ConstantValue runtimeEval(InterpreterState state) {
        // TODO
        return null;
    }
}