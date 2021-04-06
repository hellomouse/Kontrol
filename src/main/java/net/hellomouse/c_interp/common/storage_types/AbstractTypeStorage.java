package net.hellomouse.c_interp.common.storage_types;

import net.hellomouse.c_interp.common.expressions.exceptions.InvalidSpecifierException;
import net.hellomouse.c_interp.common.specifiers.FunctionSpecifier;
import net.hellomouse.c_interp.common.specifiers.StorageSpecifier;
import net.hellomouse.c_interp.common.specifiers.TypeQualifier;
import net.hellomouse.c_interp.common.specifiers.TypeSpecifier;

import java.util.ArrayList;

public abstract class AbstractTypeStorage {
    protected int typeSpecifiers;
    protected int typeQualifiers;
    protected int storageSpecifiers;
    protected int functionSpecifiers;

    protected int defaultTypeSpecifiers = 0x0;
    protected int defaultTypeQualifiers = 0x0;
    protected int defaultStorageSpecifiers = 0x0;
    protected int defaultFunctionSpecifiers = 0x0;

    protected int allowedTypeSpecifiers = 0x0;

    private boolean locked = false;

    public void addSpecifiers(ArrayList<String> typeSpecifiers, ArrayList<String> typeQualifiers, ArrayList<String> storageSpecifiers, ArrayList<String> functionSpecifiers) {
        checkLock();
        this.typeSpecifiers |= TypeSpecifier.getSpecifiers(typeSpecifiers, defaultTypeSpecifiers);
        this.typeQualifiers |= TypeQualifier.getSpecifiers(typeQualifiers, defaultTypeQualifiers);
        this.storageSpecifiers |= StorageSpecifier.getSpecifiers(storageSpecifiers, defaultStorageSpecifiers);
        this.functionSpecifiers |= FunctionSpecifier.getSpecifiers(functionSpecifiers, defaultFunctionSpecifiers);
        verifySpecifiers();
        postCheck();
    }

    public void setAllowedTypeSpecifiers(int allowedTypeSpecifiers) {
        checkLock();
        this.allowedTypeSpecifiers = allowedTypeSpecifiers;
    }

    public void postCheck() {}

    public void setSpecifiers(int typeSpecifiers, int typeQualifiers, int storageSpecifiers, int functionSpecifiers) {
        checkLock();
        this.typeSpecifiers = typeSpecifiers;
        this.typeQualifiers = typeQualifiers;
        this.storageSpecifiers = storageSpecifiers;
        this.functionSpecifiers = functionSpecifiers;
        verifySpecifiers();
        postCheck();
    }

    public void setSpecifiers(int typeSpecifiers) {
        checkLock();
        setSpecifiers(typeSpecifiers, 0x0, 0x0, 0x0);
    }


    public void verifySpecifiers() {
        TypeSpecifier.verifySpecifiers(typeSpecifiers);
        TypeQualifier.verifySpecifiers(typeQualifiers);
        StorageSpecifier.verifySpecifiers(storageSpecifiers);
        FunctionSpecifier.verifySpecifiers(functionSpecifiers);

        // Allowed specifier check
        for (int specifier : TypeSpecifier.ALL_SPECIFIERS) {
            if ((specifier & allowedTypeSpecifiers) == 0 && (specifier & typeSpecifiers) != 0)
                throw new InvalidSpecifierException("both '" + TypeSpecifier.specifierToString(specifier) + "' and '" + getFullName() + "' in declaration specifiers");
        }
    }

    public abstract int getSize();

    public boolean isVoid() { return false; }

    public boolean isScalar() { return true; }

    public int getTypeSpecifiers() { return typeSpecifiers; }
    public int getTypeQualifiers() { return typeQualifiers; }
    public int getStorageSpecifiers() { return storageSpecifiers; }
    public int getFunctionSpecifiers() { return functionSpecifiers; }

    public void setDefaultTypeSpecifiers(int defaultTypeSpecifiers) {
        checkLock();
        this.defaultTypeSpecifiers = defaultTypeSpecifiers;
    }

    public abstract String getFullName();

    public PointerTypeStorage toPointer(int pointerLevel) {
        return new PointerTypeStorage(this, pointerLevel);
    }

    public void clearTypedef() {
        checkLock();
        this.storageSpecifiers = 0x0;
    }

    public String getId() {
        return String.valueOf(typeSpecifiers +
                typeQualifiers * TypeSpecifier.ALL +
                storageSpecifiers * TypeSpecifier.ALL * TypeQualifier.ALL +
                functionSpecifiers * TypeSpecifier.ALL * TypeQualifier.ALL * StorageSpecifier.ALL);
    }

    // TOOD: better name
    public boolean approximatelyEquals(Object o) {
        if (o == null) return false;
        return this.getClass().equals(o.getClass());
    }

    public void lock() { this.locked = true; }

    private void checkLock() {
        if (locked) throw new IllegalStateException("locked"); // TODO
    }
}
