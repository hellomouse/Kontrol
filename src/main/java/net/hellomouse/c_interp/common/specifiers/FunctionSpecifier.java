package net.hellomouse.c_interp.common.specifiers;

import java.util.ArrayList;

/**
 * Function specifiers such as inline or _Noreturn
 * @author Bowserinator
 */
public class FunctionSpecifier {
    public static final int INLINE = 0x1;
    public static final int NORETURN = 0x2;

    public static final int ALL = INLINE | NORETURN;
    public static final int[] ALL_SPECIFIERS = { INLINE, NORETURN };

    /**
     * Verify if an int representing the specifiers is valid
     * @param specifiers Int made of the specifier flags
     */
    public static void verifySpecifiers(int specifiers) {
        // No conflict
    }

    /**
     * Generate specifier flag int from string of declaration types
     * @param declarationTypes Arraylist of Strings of each token in the type specifier, ie ['unsigned', 'long', 'int']
     *                         The last term should be the actual type (in above example it is 'int'). This is important
     *                         as last term is skipped!
     * @param defaultSpecifiers Default specifier flag int (get from type)
     * @return new specifier int
     */
    public static int getSpecifiers(ArrayList<String> declarationTypes, int defaultSpecifiers) {
        for (String specifier : declarationTypes) {
            switch (specifier) {
                case "inline" -> { defaultSpecifiers |= INLINE; }
                case "_Noreturn" -> { defaultSpecifiers |= NORETURN; }
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
            case INLINE: return "inline";
            case NORETURN: return "_Noreturn";
        }
        throw new IllegalStateException("Unknown function specifier " + specifier);
    }

    private FunctionSpecifier() {}
}
