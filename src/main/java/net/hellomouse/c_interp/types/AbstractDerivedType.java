package net.hellomouse.c_interp.types;

import net.hellomouse.c_interp.common.Machine;

public abstract class AbstractDerivedType extends AbstractType {
    public final AbstractType baseType;

    public AbstractDerivedType(Machine machine, int specifiers, int qualifiers, AbstractType baseType) {
        super(machine, specifiers, qualifiers);
        this.baseType = baseType;
        this.verifySpecifiers();
    }

    public void verifySpecifiers() { baseType.verifySpecifiers(); }
    public int getAllowedSpecifiers() { return baseType.getAllowedSpecifiers(); }
    public int getDefaultSpecifiers() { return baseType.getDefaultSpecifiers(); }
}
