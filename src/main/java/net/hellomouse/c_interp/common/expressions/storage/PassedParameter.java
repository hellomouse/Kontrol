package net.hellomouse.c_interp.common.expressions.storage;

import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;

public class PassedParameter extends Variable {
    public PassedParameter(String name, AbstractTypeStorage type) {
        super(name, type, null);
    }
}
