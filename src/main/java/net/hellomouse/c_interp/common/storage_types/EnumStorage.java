package net.hellomouse.c_interp.common.storage_types;

import net.hellomouse.c_interp.common.MachineSettings;
import net.hellomouse.c_interp.common.storage_types.interfaces.INamedType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.StringJoiner;

public class EnumStorage extends AbstractTypeStorage implements INamedType {
    public final String name;
    public final ArrayList<EnumValue> values;

    private final String id;

    public EnumStorage(String name, ArrayList<EnumValue> values) {
        this.name = name;
        this.values = values;
        this.id = name + values;
    }

    @Override
    public String getFullName() { return "int"; }

    @Override
    public String getName() { return name; }

    @Override
    public String getId() { return super.getId() + id; }

    @Override
    public int getSize() { return MachineSettings.INVALID_SIZE; } // TODO

    @Override
    public String getFullDeclaration() { return "enum " + name; }

    @Override
    public String toString() {
        StringJoiner val = new StringJoiner(",");
        for (EnumValue value : values)
            val.add(value.name + "=" + value.value);
        return "enum{" + val + "}";
    }

    public static class EnumValue {
        public final String name;
        public final BigInteger value;

        public EnumValue(String name, BigInteger value) {
            this.name = name;
            this.value = value;
        }
    }
}
