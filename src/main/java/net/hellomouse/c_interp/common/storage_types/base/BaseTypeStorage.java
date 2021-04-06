package net.hellomouse.c_interp.common.storage_types.base;

import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;


public class BaseTypeStorage extends AbstractTypeStorage {
    public enum Primitive { INTEGER, FLOAT, VOID, BOOL };

    public final String name;
    public final int size;
    public final int castPriority;
    public final Primitive primitive;

    public BaseTypeStorage(String name, int size, int castPriority, Primitive primitive) {
        this.name = name;
        this.size = size;
        this.castPriority = castPriority;
        this.primitive = primitive;
    }

    public BaseTypeStorage copy() {
        BaseTypeStorage copy = new BaseTypeStorage(name, size, castPriority, primitive);
        copy.allowedTypeSpecifiers = allowedTypeSpecifiers;
        copy.defaultTypeSpecifiers = defaultTypeSpecifiers;
        return copy;
    }

    @Override
    public String getId() {
        return super.getId() + name + size + castPriority + primitive + storageSpecifiers + typeSpecifiers + typeQualifiers + functionSpecifiers;
    }

    @Override
    public boolean isVoid() { return primitive == Primitive.VOID; }

    @Override
    public String getFullName() { return name; }

    @Override
    public String toString() {
        return "[" + name  + "]";
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BaseTypeStorage)) return false;
        return ((BaseTypeStorage)o).name.equals(name) && ((BaseTypeStorage)o).castPriority == castPriority && ((BaseTypeStorage)o).primitive == primitive;
    }
}
