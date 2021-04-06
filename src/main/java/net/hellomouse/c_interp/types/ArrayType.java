package net.hellomouse.c_interp.types;

import net.hellomouse.c_interp.common.Machine;

public class ArrayType extends AbstractDerivedType {
    public final int size;

    public ArrayType(Machine machine, int specifiers, int qualifiers, AbstractType baseType, int size) {
        super(machine, specifiers, qualifiers, baseType);
        this.size = size;
    }

    public PointerType getPointer(int levelIncrement) {
        return baseType.getPointer(levelIncrement);
    }

    public String getName() { return baseType.getName() + "[" + size + "]"; }
    public int getSize() { return baseType.getSize() * size; }
}
