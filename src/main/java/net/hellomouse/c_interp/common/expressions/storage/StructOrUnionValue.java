package net.hellomouse.c_interp.common.expressions.storage;

import net.hellomouse.c_interp.common.expressions.interfaces.ICompileTimeValue;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.expressions.interfaces.IRuntimeConstant;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.common.storage_types.StructOrUnionStorage;
import net.hellomouse.c_interp.interpreter.InterpreterState;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class StructOrUnionValue implements ICompileTimeValue, IRuntimeConstant {
    private HashMap<String, IExpression> values = new HashMap<>();
    private final StructOrUnionStorage structOrUnionType;

    public StructOrUnionValue(StructOrUnionStorage structType) {
        this.structOrUnionType = structType;
    };

    public StructOrUnionValue(StructOrUnionValue other) {
        this.values = other.values; // TODO deep copy
        this.structOrUnionType = other.structOrUnionType;
    }

    public void addValue(String key, IExpression value) {
        values.put(key, value);
    }

    public AbstractTypeStorage getType() { return structOrUnionType; }

    public String toString() {
        StringJoiner str = new StringJoiner(",");
        for (Map.Entry<String, IExpression> value : values.entrySet())
            str.add(value.getKey() + "=" + value.getValue());
        return str.toString();
    }

    public String getStringValue() { return toString(); } // TODO

    public StructOrUnionValue copy() { return new StructOrUnionValue(this); }

    public ConstantValue runtimeEval(InterpreterState state) { return null; }

    @Override
    public IRuntimeConstant postOperation(AbstractTypeStorage type) {
        return this;
    }
}
