package net.hellomouse.c_interp.common.storage_types;

import net.hellomouse.c_interp.common.MachineSettings;
import net.hellomouse.c_interp.common.storage_types.interfaces.IIncompleteType;
import net.hellomouse.c_interp.common.storage_types.interfaces.INamedType;

public class IncompleteStructOrUnionStorage extends AbstractTypeStorage implements IIncompleteType, INamedType {
    public final boolean isStruct;
    public final String name;

    public IncompleteStructOrUnionStorage(boolean isStruct, String name) {
        this.isStruct = isStruct;
        this.name = name;
    }

    @Override
    public String getId() {
        return super.getId() + isStruct + name;
    }

    @Override
    public String getName() { return name; }

    @Override
    public String getFullName() { return (isStruct ? "struct" : "union") + " " + name; }

    @Override
    public String getFullDeclaration() {
        return (isStruct ? "struct" : "union") + " " + name;
    }

    @Override
    public int getSize() { return MachineSettings.INVALID_SIZE; }

    @Override
    public String toString() {
        return (isStruct ? "struct" : "union") + " " + name + "{\n  ??\n}";
    }
}
