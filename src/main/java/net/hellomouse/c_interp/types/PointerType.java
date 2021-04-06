package net.hellomouse.c_interp.types;

import net.hellomouse.c_interp.common.Machine;

public class PointerType extends AbstractDerivedType {
    public final int pointerLevel;

    public PointerType(Machine machine, int specifiers, int qualifiers, AbstractType baseType, int pointerLevel) {
        super(machine, specifiers, qualifiers, baseType);
        this.pointerLevel = pointerLevel;
    }

    public PointerType getPointer(int levelIncrement) {
        return new PointerType(machine, specifiers, qualifiers, baseType, pointerLevel + levelIncrement);
    }

    public String getName() { return baseType.getName() + " " + "*".repeat(pointerLevel); }
    public int getSize() { return machine.settings.getPointerSize(); }
}
