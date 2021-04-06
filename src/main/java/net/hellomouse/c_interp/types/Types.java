package net.hellomouse.c_interp.types;

import net.hellomouse.c_interp.common.Machine;

import java.util.ArrayList;

public class Types {
    public static AbstractPrimitiveType getTypeFromDeclaration(ArrayList<String> declarationTypes, Machine machine, int specifiers, int qualifiers) {
        String value = declarationTypes.get(declarationTypes.size() - 1);

        // Primitive type lookup
        switch(value) {
            case "char" -> { return new PrimitiveTypes.CharType(machine, specifiers, qualifiers); }
            case "int" -> { return new PrimitiveTypes.IntType(machine, specifiers, qualifiers); }
            case "long", "short" -> {
                declarationTypes.add("int");
                return new PrimitiveTypes.IntType(machine, specifiers, qualifiers);
            }
            case "float" -> { return new PrimitiveTypes.FloatType(machine, specifiers, qualifiers); }
            case "double" -> { return new PrimitiveTypes.DoubleType(machine, specifiers, qualifiers); }
            case "void" -> { return new PrimitiveTypes.VoidType(machine, specifiers, qualifiers); }
        }

        // Unknown primitive
        return null;
    }

    public static PrimitiveTypes.IntType getIntType(Machine machine, int specifiers, int qualifiers) {
        return new PrimitiveTypes.IntType(machine, specifiers, qualifiers);
    }

    public static PrimitiveTypes.CharType getCharType(Machine machine, int specifiers, int qualifiers) {
        return new PrimitiveTypes.CharType(machine, specifiers, qualifiers);
    }
}
