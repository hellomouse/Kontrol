package net.hellomouse.c_interp.common.specifiers;

import java.util.ArrayList;

/**
 * Utilities pertaining to type qualifiers such as const, restrict, etc...
 * @author Bowserinator
 */
public class TypeQualifier {
    public static final int CONST    = 0x1;
    public static final int RESTRICT = 0x2;
    public static final int VOLATILE = 0x4;
    public static final int ATOMIC   = 0x8;

    public static final int ALL = CONST | RESTRICT | VOLATILE | ATOMIC;
    public static final int[] ALL_SPECIFIERS = { CONST, RESTRICT, VOLATILE, ATOMIC };

    /**
     * Verify if an int representing the specifiers is valid
     * @param specifiers Int made of the specifier flags
     */
    public static void verifySpecifiers(int specifiers) {
        // No conflict other than restrict type restrictions
        // which won't be handled here
    }

    /**
     * Generate specifier flag int from string of declaration types
     * @param declarationTypes Arraylist of Strings of each token in the type specifier, ie ['const']
     * @param defaultSpecifiers Default specifier flag int (get from type)
     * @return new specifier int
     */
    public static int getSpecifiers(ArrayList<String> declarationTypes, int defaultSpecifiers) {
        for (String specifier : declarationTypes) {
            switch (specifier) {
                case "const" -> { defaultSpecifiers |= CONST; }
                case "restrict" -> { defaultSpecifiers |= RESTRICT; }
                case "volatile" -> { defaultSpecifiers |= VOLATILE; }
                case  "_Atomic" -> { defaultSpecifiers |= ATOMIC; }
            }
        }
        return defaultSpecifiers;
    }

    /**
     * Convert a single specifier to a string, cannot be combined (|=) together
     * @param specifier Specifier int
     * @return String representation
     */
    public static String specifierToString(int specifier) {
        switch(specifier) {
            case CONST:    return "const";
            case RESTRICT: return "restrict";
            case VOLATILE: return "volatile";
            case ATOMIC:   return "_Atomic";
        }
        throw new IllegalStateException("Unknown type qualifier " + specifier);
    }

    private TypeQualifier() {}
}
