package net.hellomouse.c_interp.common.expressions.storage;

import net.hellomouse.c_interp.common.expressions.RegisterExpression;
import net.hellomouse.c_interp.common.expressions.interfaces.IExpression;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;

public class Variable {
    private AbstractTypeStorage type;
    private IExpression value;
    private String name;

    public Variable(String name, AbstractTypeStorage type, IExpression value) {
        this.name = name;
        this.type = type;
        this.value = value;

        // Todo explain registers dont track type
        // TODO: rid of approximatelyEquals
        if (value != null && !(value instanceof RegisterExpression) && type != null && !type.approximatelyEquals(value.getType()))
            throw new IllegalStateException("Major type mismatch between declared type and assigned value: " + type + " " + value.getType());
    }

    public Variable copy() {
        return new Variable(name, type, value);
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public AbstractTypeStorage getType() { return type; }

    public void setType(AbstractTypeStorage type) { this.type = type; }

    public IExpression getValue() { return value; }

    public void setValue(IExpression value) { this.value = value; }

    public String toString() {
        return type + " " + name + " = " + value;
    }
}
