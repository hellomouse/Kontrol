package net.hellomouse.c_interp.types;

import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.specifiers.StorageSpecifier;
import net.hellomouse.c_interp.common.specifiers.TypeQualifier;
import net.hellomouse.c_interp.common.specifiers.TypeSpecifier;

public abstract class AbstractType {
    protected final int specifiers;
    protected final int qualifiers;
    protected final Machine machine;

    public AbstractType(Machine machine, int specifiers, int qualifiers) {
        this.machine = machine;
        this.specifiers = specifiers;
        this.qualifiers = qualifiers;
    }

    public PointerType getPointer() { return getPointer(1); }
    public abstract PointerType getPointer(int levelIncrement);

    public abstract String getName();

    public abstract int getSize();

    public void verifySpecifiers() {
        // Generic check
        TypeSpecifier.verifySpecifiers(specifiers);
        TypeQualifier.verifySpecifiers(specifiers);
        StorageSpecifier.verifySpecifiers(specifiers);

        // Allowed specifier check
        for (int specifier : TypeSpecifier.ALL_SPECIFIERS) {
            if ((specifier & getAllowedSpecifiers()) == 0 && (specifier & specifiers) != 0)
                throw new IllegalStateException("Type " + getName() + " cannot have specifier '" + TypeSpecifier.specifierToString(specifier) + "'");
        }
    }

    public abstract int getAllowedSpecifiers();
    public abstract int getDefaultSpecifiers();

    public int getSpecifiers() { return specifiers; }
    public int getQualifiers() { return qualifiers; }

    public boolean equals(Object other) {
        if (!(other instanceof AbstractType)) return false;
        return ((AbstractType) other).getName().equals(getName()) && ((AbstractType) other).specifiers == specifiers;
    }
}
