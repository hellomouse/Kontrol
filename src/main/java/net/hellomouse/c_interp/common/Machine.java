package net.hellomouse.c_interp.common;

import net.hellomouse.c_interp.common.expressions.types.Primitives;
import net.hellomouse.c_interp.common.specifiers.TypeSpecifier;
import net.hellomouse.c_interp.types.AbstractPrimitiveType;
import net.hellomouse.c_interp.types.AbstractType;
import net.hellomouse.c_interp.types.PrimitiveTypes;
import net.hellomouse.c_interp.types.Types;

import java.util.ArrayList;
import java.util.logging.Logger;



public class Machine {
    public final Logger logger = Logger.getLogger("Machine");

    public final MachineSettings settings;
    public final MachineTypes types = new MachineTypes(this);

    public final Primitives primitives;

    public Machine(MachineSettings settings) {
        this.settings = settings;
        this.primitives = new Primitives(this);
    }

    public int getTypeSize(ArrayList<String> declarationTypes, int pointerLevel) {
        if (pointerLevel > 0)
            return this.settings.getPointerSize();
        String typeName = declarationTypes.get(declarationTypes.size() - 1);
        AbstractType _type = getType(typeName, 0x0, 0x0);
        int specifiers = TypeSpecifier.getSpecifiers(declarationTypes, _type.getDefaultSpecifiers());
        _type = getType(typeName, specifiers, 0x0);
        return _type.getSize();
    }

    public AbstractType getType(String type, int specifiers, int qualifiers) {
        return Types.getTypeFromDeclaration(new ArrayList<>(){{ add(type); }}, this, specifiers, qualifiers);
    }


    public static class MachineTypes {
        private final Machine machine;

        public final AbstractType BOOL, CHAR, SHORT, INT, LONG, LONG_LONG, FLOAT, DOUBLE, LONG_DOUBLE, VOID;

        public MachineTypes(Machine machine) {
            this.machine = machine;

            BOOL = new PrimitiveTypes.BoolType(machine, 0x0, 0x0);
            CHAR  = new PrimitiveTypes.CharType(machine, 0x0, 0x0);
            SHORT = new PrimitiveTypes.IntType(machine, TypeSpecifier.SHORT, 0x0);
            INT   = new PrimitiveTypes.IntType(machine, 0x0, 0x0);
            LONG  = new PrimitiveTypes.IntType(machine, TypeSpecifier.LONG, 0x0);
            LONG_LONG = new PrimitiveTypes.IntType(machine, TypeSpecifier.LONG_LONG, 0x0);

            FLOAT = new PrimitiveTypes.FloatType(machine, 0x0, 0x0);
            DOUBLE = new PrimitiveTypes.DoubleType(machine, 0x0, 0x0);
            LONG_DOUBLE = new PrimitiveTypes.DoubleType(machine, TypeSpecifier.LONG, 0x0);

            VOID = new PrimitiveTypes.VoidType(machine, 0x0, 0x0);
        }

        public AbstractPrimitiveType getDefaultPrimitiveType(String name) {
            switch(name) {
                case "_Bool"  -> { return new PrimitiveTypes.BoolType(machine, BOOL.getDefaultSpecifiers(), 0x0); }
                case "char"   -> { return new PrimitiveTypes.CharType(machine, CHAR.getDefaultSpecifiers(), 0x0); }
                case "short"  -> { return new PrimitiveTypes.IntType(machine, SHORT.getDefaultSpecifiers(), 0x0); }
                case "int"    -> { return new PrimitiveTypes.IntType(machine, INT.getDefaultSpecifiers(), 0x0); }
                case "long"   -> { return new PrimitiveTypes.IntType(machine, LONG.getDefaultSpecifiers(), 0x0);  }
                case "float"  -> { return new PrimitiveTypes.FloatType(machine, FLOAT.getDefaultSpecifiers(), 0x0); }
                case "double" -> { return new PrimitiveTypes.DoubleType(machine, DOUBLE.getDefaultSpecifiers(), 0x0); }
                case "void"   -> { return new PrimitiveTypes.VoidType(machine, VOID.getDefaultSpecifiers(), 0x0); }
            }
            return null;
        }

        public AbstractPrimitiveType getTypeFromDeclaration(ArrayList<String> declarationTypes, int specifiers, int qualifiers) {
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
    }

}
