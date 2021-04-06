package net.hellomouse.c_interp.common.expressions.util;

import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.specifiers.TypeSpecifier;
import net.hellomouse.c_interp.common.storage_types.AbstractTypeStorage;
import net.hellomouse.c_interp.common.storage_types.base.BaseTypeStorage;
import net.hellomouse.c_interp.common.storage_types.base.IntBaseTypeStorage;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Utilities for dealing with numeric literals
 * @author Bowserinator
 */
public class Literals {
    // All strings below should be LOWERCASE

    // -- Integer literals -- \\
    private static final String HEX_PREFIX = "0x";
    private static final String OCT_PREFIX = "0";
    private static final String BIN_PREFIX = "0b";

    private static final String SIGNED_LONG_SUFFIX = "l";
    private static final String[] UNSIGNED_LONG_SUFFIXES = {"ul", "lu"};

    private static final String SIGNED_LONG_LONG_SUFFIX = "ll";
    private static final String[] UNSIGNED_LONG_LONG_SUFFIXES = {"ull", "llu"};

    private static final String UNSIGNED_SUFFIX = "u";

    // -- Float literals -- \\
    private static final String FLOAT_SUFFIX = "f";
    private static final String LONG_DOUBLE_SUFFIX = "l";

    /**
     * Process an integer literal, ie "10000000ull". Returns an IntLiteralData
     * which contains a BigInteger (processed to the proper value), and an AbstractType
     * for the value
     *
     * @see IntLiteralData
     * @param machine Machine instance
     * @param str String representing an int literal
     * @return IntLiteralData, or null if int literal was not valid
     */
    public static IntLiteralData processIntegerLiterals(Machine machine, String str) {
        str = str.toLowerCase();
        int radix = 10;

        // ------------------------
        // PREFIX
        // Prefix determines radix
        if (str.startsWith(HEX_PREFIX)) {
            str = str.substring(HEX_PREFIX.length());
            radix = 16;
        }
        else if (str.startsWith(BIN_PREFIX)) {
            str = str.substring(BIN_PREFIX.length());
            radix = 2;
        }
        else if (str.startsWith(OCT_PREFIX) && str.length() > OCT_PREFIX.length()) { // Special case, don't remove '0' -> ''
            str = str.substring(OCT_PREFIX.length());
            radix = 8;
        }


        // ------------------------
        // SUFFIX
        // Suffix determines type
        int typeSpecifier = 0x0;
        int signSpecifier = (machine.types.INT.getDefaultSpecifiers() & TypeSpecifier.SIGNED) != 0 ?
                TypeSpecifier.SIGNED : TypeSpecifier.UNSIGNED;
        boolean suffixHandled = false;

        for (String ullSuffix : UNSIGNED_LONG_LONG_SUFFIXES) {
            if (str.endsWith(ullSuffix)) {
                str = str.substring(0, str.length() - ullSuffix.length());
                typeSpecifier = TypeSpecifier.LONG_LONG;
                signSpecifier = TypeSpecifier.UNSIGNED;
                suffixHandled = true;
                break;
            }
        }

        if (!suffixHandled && str.endsWith(SIGNED_LONG_LONG_SUFFIX)) {
            str = str.substring(0, str.length() - SIGNED_LONG_LONG_SUFFIX.length());
            typeSpecifier = TypeSpecifier.LONG_LONG;
            signSpecifier = TypeSpecifier.SIGNED;
            suffixHandled = true;
        }

        if (!suffixHandled) {
            for (String ulSuffix : UNSIGNED_LONG_SUFFIXES) {
                if (str.endsWith(ulSuffix)) {
                    str = str.substring(0, str.length() - ulSuffix.length());
                    typeSpecifier = TypeSpecifier.LONG;
                    signSpecifier = TypeSpecifier.UNSIGNED;
                    suffixHandled = true;
                    break;
                }
            }
        }

        if (!suffixHandled && str.endsWith(SIGNED_LONG_SUFFIX)) {
            str = str.substring(0, str.length() - SIGNED_LONG_SUFFIX.length());
            typeSpecifier = TypeSpecifier.LONG;
            signSpecifier = TypeSpecifier.SIGNED;
            suffixHandled = true;
        }

        if (!suffixHandled && str.endsWith(UNSIGNED_SUFFIX)) {
            str = str.substring(0, str.length() - UNSIGNED_SUFFIX.length());
            signSpecifier = TypeSpecifier.UNSIGNED;
        }

        BigInteger val;
        try { val = new BigInteger(str, radix); }
        catch (NumberFormatException e) { return null; }

        IntBaseTypeStorage type = (IntBaseTypeStorage)machine.primitives.getIntTypeReference(typeSpecifier | signSpecifier);

        // Attempt to numericPromote int -> long
        if (!type.canFit(val) && typeSpecifier == 0x0) {
            typeSpecifier = TypeSpecifier.LONG;
            type = (IntBaseTypeStorage)machine.primitives.getIntTypeReference(typeSpecifier | signSpecifier);
        }

        // Attempt to numericPromote long -> long long
        if (!type.canFit(val) && typeSpecifier == TypeSpecifier.LONG) {
            typeSpecifier = TypeSpecifier.LONG_LONG;
            type = (IntBaseTypeStorage)machine.primitives.getIntTypeReference(typeSpecifier | signSpecifier);
        }

        return new IntLiteralData(type, val);
    }


    /**
     * Process a floating literal, ie "123.45f". Returns a FloatLiteralData
     * which contains a BigDecimal (processed to the proper value), and an AbstractType
     * for the value
     *
     * @see FloatLiteralData
     * @param machine Machine instance
     * @param str String representing a float literal
     * @return FloatLiteralData, or null if float literal was not valid
     */
    public static FloatLiteralData processFloatingLiterals(Machine machine, String str) {
        str = str.toLowerCase();

        BigDecimal val;
        BaseTypeStorage type = machine.primitives.DOUBLE;

        if (str.endsWith(FLOAT_SUFFIX)) {
            str = str.substring(0, str.length() - FLOAT_SUFFIX.length());
            type = machine.primitives.FLOAT;
        }

        else if (str.endsWith(LONG_DOUBLE_SUFFIX)) {
            str = str.substring(0, str.length() - LONG_DOUBLE_SUFFIX.length());
            type = machine.primitives.LONG_DOUBLE;
        }

        try { val = new BigDecimal(str); }
        catch (NumberFormatException e) { return null; }
        return new FloatLiteralData(type, val);
    }

    /**
     * Process a string literal, ie "\"Hello world!\"". Returns a String or null if the
     * provided string is not a valid string literal
     * @param str String representing a string literal
     * @return String of the literal content, or null if not valid
     */
    public static String processStringLiteral(String str) {
        return (str.startsWith("\"") && str.endsWith("\"")) ?
                str.substring(1, str.length() - 1) :
                null;
    }

    /** Stores the result from an int literal */
    public static class IntLiteralData {
        public final AbstractTypeStorage type;
        public final BigInteger value;

        public IntLiteralData(AbstractTypeStorage type, BigInteger value) {
            this.type = type;
            this.value = value;
        }
    }

    /** Stores the result from a float literal */
    public static class FloatLiteralData {
        public final AbstractTypeStorage type;
        public final BigDecimal value;

        public FloatLiteralData(AbstractTypeStorage type, BigDecimal value) {
            this.type = type;
            this.value = value;
        }
    }

    private Literals() {}
}
