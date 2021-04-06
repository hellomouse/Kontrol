package net.hellomouse.c_interp.common.specifiers;

import net.hellomouse.c_interp.common.expressions.exceptions.InvalidSpecifierException;

import java.util.ArrayList;

/**
 * Utilities pertaining to type specifiers such as unsigned, signed,
 * short, long, and long long
 * @author Bowserinator
 */
public class TypeSpecifier {
    public static final int SIGNED    = 0x1;
    public static final int UNSIGNED  = 0x2;
    public static final int SHORT     = 0x4;
    public static final int LONG      = 0x8;
    public static final int LONG_LONG = 0x10;

    public static final int ALL =
            SIGNED | UNSIGNED | SHORT | LONG | LONG_LONG;
    public static final int[] ALL_SPECIFIERS = { SIGNED, UNSIGNED, SHORT, LONG, LONG_LONG };

    /**
     * Verify if an int representing the specifiers is valid
     * @param specifiers Int made of the specifier flags
     */
    public static void verifySpecifiers(int specifiers) {
        // No modifiers (default)
        if (specifiers == 0x0) return;

        // Cannot have signed unsigned
        if ((specifiers & SIGNED) != 0 && (specifiers & UNSIGNED) != 0)
            throw new InvalidSpecifierException("both 'signed' and 'unsigned' in declaration specifiers");
        // Cannot have a short long
        if ((specifiers & SHORT) != 0 && (specifiers & LONG) != 0)
            throw new InvalidSpecifierException("both 'long' and 'short' in declaration specifiers");
        // Cannot have long and long long, only long long
        // Should never happen unless there is an error in parsing specifier flags
        if ((specifiers & LONG) != 0 && (specifiers & LONG_LONG) != 0)
            throw new IllegalStateException("Parsed specifiers 'long', 'long long' together, but only 'long long' should be flagged.");
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
        boolean seenSignSpecifier = false;
        int longCount = 0;

        for (String specifier : declarationTypes) {
            switch (specifier) {
                // Sign specifier (signed / unsigned)
                case "unsigned" -> {
                    if (seenSignSpecifier)
                        throw new InvalidSpecifierException("both 'signed' and 'unsigned' in declaration specifiers");
                    defaultSpecifiers |= UNSIGNED;
                    defaultSpecifiers &= ~SIGNED;
                    seenSignSpecifier = true;
                }
                case "signed" -> {
                    if (seenSignSpecifier)
                        throw new InvalidSpecifierException("both 'signed' and 'unsigned' in declaration specifiers");
                    defaultSpecifiers |= SIGNED;
                    defaultSpecifiers &= ~UNSIGNED;
                    seenSignSpecifier = true;
                }

                // Short / long
                case "short" -> defaultSpecifiers |= SHORT;
                case "long" -> longCount++;
            }
        }

        // Long / long long handling
        if (longCount == 1)
            defaultSpecifiers |= LONG;
        else if (longCount == 2)
            defaultSpecifiers |= LONG_LONG;
        else if (longCount > 2)
            throw new InvalidSpecifierException("'" + ("long ".repeat(longCount - 1)) + "long' is too long");

        return defaultSpecifiers;
    }

    /**
     * Convert a single specifier to a string, cannot be combined (|=) together
     * @param specifier Specifier int
     * @return String representation
     */
    public static String specifierToString(int specifier) {
        switch(specifier) {
            case SIGNED:    return "signed";
            case UNSIGNED:  return "unsigned";
            case SHORT:     return "short";
            case LONG:      return "long";
            case LONG_LONG: return "long long";
        }
        throw new IllegalStateException("Unknown type specifier " + specifier);
    }

    private TypeSpecifier() {}
}
