package net.hellomouse.c_interp.types;

import net.hellomouse.c_interp.common.Machine;

public abstract class AbstractPrimitiveType extends AbstractType {
    public final int castPriority;

    public AbstractPrimitiveType(Machine machine, int specifiers, int qualifiers, int castPriority) {
        super(machine, specifiers, qualifiers);
        this.castPriority = castPriority;
        this.verifySpecifiers();
    }

    public PointerType getPointer(int levelIncrement) {
        return new PointerType(machine, specifiers, qualifiers, this, levelIncrement);
    }
}
