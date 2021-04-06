package net.hellomouse.c_interp.common.storage_types;

import net.hellomouse.c_interp.common.storage_types.interfaces.IArray;

public class ArrayTypeStorage extends AbstractTypeStorage implements IArray {
    public final AbstractTypeStorage elementType;
    public final int length;

    public ArrayTypeStorage(AbstractTypeStorage elementType, int length) {
        this.elementType = elementType;
        this.length = length;
    }

    @Override
    public String getId() {
        return super.getId() + elementType.getId() + length;
    }

    public AbstractTypeStorage getElementType() {
        return elementType;
    }

    @Override
    public String getFullName() { return elementType.getFullName() + "[" + length + "]"; }

    @Override
    public String toString() {
        return elementType + "[" + length + "]";
    }

    public int getSize() {
        return length * elementType.getSize();
    }
}
