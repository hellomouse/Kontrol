package net.hellomouse.c_interp.common.expressions.types;

import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.specifiers.TypeSpecifier;
import net.hellomouse.c_interp.common.storage_types.base.BaseTypeStorage;
import net.hellomouse.c_interp.common.storage_types.base.FloatBaseTypeStorage;
import net.hellomouse.c_interp.common.storage_types.base.IntBaseTypeStorage;

import java.util.HashMap;

/**
 * A class for handling primitive types during compile time
 * @author Bowserinator
 */
public class Primitives {
    public final BaseTypeStorage VOID, BOOL, CHAR, UCHAR, SHORT, USHORT, INT, UINT, LONG, ULONG, LONG_LONG, ULONG_LONG,
        FLOAT, DOUBLE, LONG_DOUBLE;

    public final BaseTypeStorage[] INTEGER_TYPES;
    public final BaseTypeStorage[] FLOAT_TYPES;

    private final HashMap<String, BaseTypeStorage> injectedPrimitives = new HashMap<>();

    /**
     * Construct a primitives instance
     * @param machine Machine instance
     */
    public Primitives(Machine machine) {
        // Type definitions
        VOID  = new BaseTypeStorage("void", machine.settings.getInvalidSize(), -1, BaseTypeStorage.Primitive.VOID);
        BOOL  = new BaseTypeStorage("bool", machine.settings.getBoolSize(), 0, BaseTypeStorage.Primitive.BOOL);

        CHAR  = new IntBaseTypeStorage("char", machine.settings.getCharSize(), 1);
        UCHAR = new IntBaseTypeStorage("char", machine.settings.getCharSize(), 2);

        SHORT  = new IntBaseTypeStorage("int", machine.settings.getShortSize(), 3);
        USHORT = new IntBaseTypeStorage("int", machine.settings.getShortSize(), 4);
        INT  = new IntBaseTypeStorage("int", machine.settings.getIntSize(), 5);
        UINT = new IntBaseTypeStorage("int", machine.settings.getIntSize(), 6);
        LONG  = new IntBaseTypeStorage("int", machine.settings.getLongSize(), 7);
        ULONG = new IntBaseTypeStorage("int", machine.settings.getLongSize(), 8);
        LONG_LONG  = new IntBaseTypeStorage("int", machine.settings.getLongLongSize(), 9);
        ULONG_LONG = new IntBaseTypeStorage("int", machine.settings.getLongLongSize(), 10);

        FLOAT  = new FloatBaseTypeStorage("float", machine.settings.getFloatMantissa(), machine.settings.getFloatExponent(), 11);
        DOUBLE = new FloatBaseTypeStorage("double", machine.settings.getDoubleMantissa(), machine.settings.getDoubleExponent(), 12);
        LONG_DOUBLE = new FloatBaseTypeStorage("double", machine.settings.getLongDoubleMantissa(), machine.settings.getLongDoubleExponent(), 13);

        INTEGER_TYPES = new BaseTypeStorage[]{CHAR, UCHAR, SHORT, USHORT, INT, UINT, LONG, ULONG, LONG_LONG, ULONG_LONG};
        FLOAT_TYPES = new BaseTypeStorage[]{FLOAT, DOUBLE, LONG_DOUBLE};

        // Allowed specifiers
        final int SIGN_SPECIFIERS = TypeSpecifier.SIGNED | TypeSpecifier.UNSIGNED;

        CHAR.setAllowedTypeSpecifiers(SIGN_SPECIFIERS);
        SHORT.setAllowedTypeSpecifiers(SIGN_SPECIFIERS | TypeSpecifier.SHORT);
        INT.setAllowedTypeSpecifiers(SIGN_SPECIFIERS | TypeSpecifier.SHORT | TypeSpecifier.LONG | TypeSpecifier.LONG_LONG);
        LONG.setAllowedTypeSpecifiers(SIGN_SPECIFIERS | TypeSpecifier.LONG);
        LONG_LONG.setAllowedTypeSpecifiers(SIGN_SPECIFIERS | TypeSpecifier.LONG_LONG);

        UCHAR.setAllowedTypeSpecifiers(SIGN_SPECIFIERS);
        USHORT.setAllowedTypeSpecifiers(SIGN_SPECIFIERS | TypeSpecifier.SHORT);
        UINT.setAllowedTypeSpecifiers(SIGN_SPECIFIERS);
        ULONG.setAllowedTypeSpecifiers(SIGN_SPECIFIERS | TypeSpecifier.LONG);
        ULONG_LONG.setAllowedTypeSpecifiers(SIGN_SPECIFIERS | TypeSpecifier.LONG_LONG);

        LONG_DOUBLE.setAllowedTypeSpecifiers(TypeSpecifier.LONG);

        // Default specifiers
        CHAR.setDefaultTypeSpecifiers(machine.settings.isCharSigned() ? TypeSpecifier.SIGNED : TypeSpecifier.UNSIGNED);
        UCHAR.setDefaultTypeSpecifiers(TypeSpecifier.UNSIGNED);
        SHORT.setDefaultTypeSpecifiers(TypeSpecifier.SIGNED | TypeSpecifier.SHORT);
        USHORT.setDefaultTypeSpecifiers(TypeSpecifier.UNSIGNED | TypeSpecifier.SHORT);
        INT.setDefaultTypeSpecifiers(TypeSpecifier.SIGNED);
        UINT.setDefaultTypeSpecifiers(TypeSpecifier.UNSIGNED);
        LONG.setDefaultTypeSpecifiers(TypeSpecifier.SIGNED | TypeSpecifier.LONG);
        ULONG.setDefaultTypeSpecifiers(TypeSpecifier.UNSIGNED | TypeSpecifier.LONG);
        LONG_LONG.setDefaultTypeSpecifiers(TypeSpecifier.SIGNED | TypeSpecifier.LONG_LONG);
        ULONG_LONG.setDefaultTypeSpecifiers(TypeSpecifier.UNSIGNED | TypeSpecifier.LONG_LONG);
        LONG_DOUBLE.setDefaultTypeSpecifiers(TypeSpecifier.LONG);

        // Lock
        for (BaseTypeStorage type : INTEGER_TYPES)
            type.lock();
        for (BaseTypeStorage type : FLOAT_TYPES)
            type.lock();
        VOID.lock();
        BOOL.lock();
    }

    /**
     * Get a reference to an int type by specifier. This only returns a reference,
     * so all the types are locked and cannot be modified.
     * @param specifiers Specifier flag int containing valid int specifiers
     * @return Reference to int type
     */
    public BaseTypeStorage getIntTypeReference(int specifiers) {
        if ((specifiers & TypeSpecifier.SIGNED) != 0) {
            if ((specifiers & TypeSpecifier.SHORT) != 0) return SHORT;
            if ((specifiers & TypeSpecifier.LONG) != 0) return LONG;
            if ((specifiers & TypeSpecifier.LONG_LONG) != 0) return LONG_LONG;
            return INT;
        }
        else {
            if ((specifiers & TypeSpecifier.SHORT) != 0) return USHORT;
            if ((specifiers & TypeSpecifier.LONG) != 0) return ULONG;
            if ((specifiers & TypeSpecifier.LONG_LONG) != 0) return ULONG_LONG;
            return UINT;
        }
    }

    /**
     * Get a reference to the unsigned variant of an int type. This only returns a reference,
     * so all the types are locked and cannot be modified.
     * @param intType Integer type to convert to unsigned
     * @return Reference to unsigned int type
     */
    public BaseTypeStorage toUnsignedIntTypeReference(IntBaseTypeStorage intType) {
        if (intType.isUnsigned()) return intType;
        if (intType.equals(CHAR)) return UCHAR;
        if (intType.equals(SHORT)) return USHORT;
        if (intType.equals(INT)) return UINT;
        if (intType.equals(LONG)) return ULONG;
        if (intType.equals(LONG_LONG)) return LONG_LONG;

        throw new IllegalStateException("Unknown int type " + intType.getFullName());
    }

    /**
     * Given the name of a primitive, returns the base type representation. For example,
     * 'short', 'long' returns INT, while 'double' returns DOUBLE. This will return a copy
     * that is fully editable.
     *
     * @param name Name of the base type
     * @return Copy of the base type
     */
    public BaseTypeStorage getBaseType(String name) {
        switch(name) {
            case "bool", "_Bool" -> { return BOOL.copy(); }
            case "char" -> { return CHAR.copy(); }
            case "short", "long", "int" -> { return INT.copy(); }
            case "float" -> { return FLOAT.copy(); }
            case "double" -> { return DOUBLE.copy(); }
            case "void" -> { return VOID.copy(); }
        }
        BaseTypeStorage type = injectedPrimitives.get(name);
        if (type != null)
            return type.copy();
        throw new IllegalStateException("Unknown base type name '" + name + "'");
    }

    /**
     * Inject a new primitive type
     * @param primitive Primitive type, will be locked
     */
    public void injectPrimitive(BaseTypeStorage primitive) {
        primitive.lock();
        injectedPrimitives.put(primitive.name, primitive);
    }
}
