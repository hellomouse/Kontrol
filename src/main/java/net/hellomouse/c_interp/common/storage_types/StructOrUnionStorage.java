package net.hellomouse.c_interp.common.storage_types;

import net.hellomouse.c_interp.common.expressions.storage.NameAndType;
import net.hellomouse.c_interp.common.storage_types.interfaces.INamedType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringJoiner;

public class StructOrUnionStorage extends AbstractTypeStorage implements INamedType {
    public final boolean isStruct;
    public final String name;
    public final ArrayList<NameAndType> fields;
    public final HashMap<String, Integer> valueIndexMap = new HashMap<>();

    private int sizeCache = -1;
    private int typeQualifiers, storageSpecifiers;
    private final String id;

    public StructOrUnionStorage(boolean isStruct, String name, ArrayList<NameAndType> fields) {
        this.isStruct = isStruct;
        this.name = name;
        this.fields = fields;

        for (int i = 0; i < fields.size(); i++)
            valueIndexMap.put(fields.get(i).name, i);

        this.id = isStruct + name + fields;
    }

    @Override
    public boolean isScalar() { return false; }

    public void assignSpecifiers(int typeQualifiers, int storageSpecifiers) {
        this.typeQualifiers = typeQualifiers;
        this.storageSpecifiers = storageSpecifiers;
    }

    public int getTypeSpecifiers() { return 0x0; }
    public int getTypeQualifiers() { return typeQualifiers; }
    public int getStorageSpecifiers() { return storageSpecifiers; }
    public int getFunctionSpecifiers() { return 0x0; }


    // Space is intentional, even if name is blank
    @Override
    public String getFullName() { return (isStruct ? "struct" : "union") + " " + name; }

    @Override
    public String getName() { return name; }

    @Override
    public String getFullDeclaration() { return (isStruct ? "struct" : "union") + " " + name; }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",\n");
        for (NameAndType value : fields)
            joiner.add("    " + value.name + " = " + value.type);
        return (isStruct ? "struct" : "union") + " " + name + "{\n" + joiner + "}";
    }

    @Override
    public String getId() {
        return super.getId() + id;
    }

    @Override
    public int getSize() {
        if (sizeCache < 0) {
            int size = 0;
            for (NameAndType value : fields)
                size += value.type.getSize();
            sizeCache = size;
        }
        return sizeCache;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof StructOrUnionStorage)) return false;
        StructOrUnionStorage otherStruct = (StructOrUnionStorage)other;
        if (otherStruct.isStruct != isStruct || !otherStruct.name.equals(name) || otherStruct.fields.size() != fields.size())
            return false;

        for (int i = 0; i < fields.size(); i++) {
            // TODO: type cast
            if (!fields.get(i).name.equals(otherStruct.fields.get(i).name))
                return false;
        }
        return true;
    }
}
