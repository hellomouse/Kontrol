package net.hellomouse.c_interp.common.expressions.storage;

import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;

/**
 * Simple storage for a single name and type value, used for instance,
 * for struct member storage
 * @author Bowserinator
 */
public class NameAndType {
    public final String name;
    public final AbstractTypeStorage type;

    /**
     * Construct name and type
     * @param name Name
     * @param type Type
     */
    public NameAndType(String name, AbstractTypeStorage type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        return name + "," + type.toString();
    }
}
