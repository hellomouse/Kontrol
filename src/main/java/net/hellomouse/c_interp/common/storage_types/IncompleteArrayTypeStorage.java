package net.hellomouse.c_interp.common.storage_types;

import net.hellomouse.c_interp.common.MachineSettings;
import net.hellomouse.c_interp.common.storage_types.interfaces.IArray;
import net.hellomouse.c_interp.common.storage_types.interfaces.IIncompleteType;

public class IncompleteArrayTypeStorage extends AbstractTypeStorage implements IIncompleteType, IArray {
    public final AbstractTypeStorage elementType;

    public IncompleteArrayTypeStorage(AbstractTypeStorage elementType) {
        this.elementType = elementType;
    }

    @Override
    public AbstractTypeStorage getElementType() {
        return elementType;
    }

    @Override
    public String getId() { return super.getId() + elementType.getId(); }

    @Override
    public String getFullName() { return elementType.getFullName() + "[]"; }

    @Override
    public int getSize() { return MachineSettings.INVALID_SIZE; }

    @Override
    public String toString() {
        return elementType + "[?]";
    }
}
