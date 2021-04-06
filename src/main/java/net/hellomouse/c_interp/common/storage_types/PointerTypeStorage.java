package net.hellomouse.c_interp.common.storage_types;

import net.hellomouse.c_interp.common.MachineSettings;

public class PointerTypeStorage extends AbstractTypeStorage {
    public final AbstractTypeStorage baseType;
    public final int pointerCount;

    private final String id;

    public PointerTypeStorage(AbstractTypeStorage baseType, int pointerCount) {
        if (baseType instanceof PointerTypeStorage) {
            pointerCount += ((PointerTypeStorage) baseType).pointerCount;
            baseType = ((PointerTypeStorage) baseType).baseType;
        }

        this.baseType = baseType;
        this.pointerCount = pointerCount;
        this.id = baseType.getId() + pointerCount;
    }

    @Override
    public String getFullName() { return baseType.getFullName() + "*".repeat(pointerCount); }

    @Override
    public String getId() { return super.getId() + id; }

    @Override
    public String toString() {
        return "(" + "*".repeat(pointerCount) + baseType + ")";
    }

    @Override
    public int getSize() {
        // TODO temp
        return MachineSettings.INVALID_SIZE;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PointerTypeStorage)) return false;
        PointerTypeStorage otherPointer = (PointerTypeStorage)other;
        return otherPointer.pointerCount == pointerCount && otherPointer.baseType.equals(baseType);
    }
}
