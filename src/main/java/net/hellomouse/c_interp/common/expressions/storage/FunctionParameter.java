package net.hellomouse.c_interp.common.expressions.storage;

import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;

public class FunctionParameter {
    public final AbstractTypeStorage type;
    public final String name;

    public final boolean unnamed, vararg;

    public FunctionParameter(AbstractTypeStorage type, String name) {
        this.type = type;
        this.name = name;

        this.unnamed = name == null;
        this.vararg = type == null;
    }

    public String toString() {
        return type + " " + name;
    }

    public boolean equals(Object o) {
        if (!(o instanceof FunctionParameter)) return false;
        FunctionParameter param = (FunctionParameter)o;
        return param.type.equals(type) && param.unnamed == unnamed && param.vararg == vararg;
    }
}
