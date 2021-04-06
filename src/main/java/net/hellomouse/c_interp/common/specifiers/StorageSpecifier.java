package net.hellomouse.c_interp.common.specifiers;

import net.hellomouse.c_interp.common.expressions.exceptions.InvalidSpecifierException;

import java.util.ArrayList;

/**
 * Specifiers for storage, ie typedef, static, etc...
 * @author Bowserinator
 */
public class StorageSpecifier {
    public static final int TYPEDEF      = 0x1;
    public static final int EXTERN       = 0x2;
    public static final int STATIC       = 0x4;
    public static final int THREAD_LOCAL = 0x8;
    public static final int AUTO         = 0x10;
    public static final int REGISTER     = 0x20;

    public static final int ALL = TYPEDEF | EXTERN | STATIC | THREAD_LOCAL | AUTO | REGISTER;
    public static final int[] ALL_SPECIFIERS = { TYPEDEF, EXTERN, STATIC, THREAD_LOCAL, AUTO, REGISTER };

    /**
     * Verify if an int representing the specifiers is valid
     * @param specifiers Int made of the specifier flags
     */
    public static void verifySpecifiers(int specifiers) {
        if (specifiers == 0x0) return;

        for (int SPECIFIER : ALL_SPECIFIERS) {
            if ((specifiers & SPECIFIER) != 0 && specifiers != SPECIFIER)
                throw new InvalidSpecifierException("multiple storage classes in declaration specifiers");
        }
    }

    /**
     * Generate specifier flag int from string of declaration types
     * @param declarationTypes Arraylist of Strings of each token in the storage specifier, ie ['auto']
     * @param defaultSpecifiers Default specifier flag int (get from type)
     * @return new specifier int
     */
    public static int getSpecifiers(ArrayList<String> declarationTypes, int defaultSpecifiers) {
        for (String specifier : declarationTypes) {
            switch (specifier) {
                case "typedef" -> { defaultSpecifiers |= TYPEDEF; }
                case "extern" -> { defaultSpecifiers |= EXTERN; }
                case "static" -> { defaultSpecifiers |= STATIC; }
                case "_Thread_local" -> { defaultSpecifiers |= THREAD_LOCAL; }
                case "auto" -> { defaultSpecifiers |= AUTO; }
                case "register" -> { defaultSpecifiers |= REGISTER; }
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
            case TYPEDEF: return "typedef";
            case EXTERN:  return "extern";
            case STATIC:  return "static";
            case THREAD_LOCAL: return "_Thread_local";
            case AUTO: return "auto";
            case REGISTER: return "register";
        }
        throw new IllegalStateException("Unknown storage specifier " + specifier);
    }

    private StorageSpecifier() {}
}
