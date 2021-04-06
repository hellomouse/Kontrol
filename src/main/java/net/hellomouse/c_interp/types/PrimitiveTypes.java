package net.hellomouse.c_interp.types;

import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.specifiers.TypeSpecifier;
import net.hellomouse.c_interp.types.interfaces.IDecimalType;
import net.hellomouse.c_interp.types.interfaces.IIntegerType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public class PrimitiveTypes {
    public static class VoidType extends AbstractPrimitiveType {
        public VoidType(Machine machine, int specifiers, int qualifiers) { super(machine, specifiers, qualifiers,0); }

        public String getName() { return "void"; }
        public int getSize() { return 0; }

        public int getAllowedSpecifiers() { return 0x0; }
        public int getDefaultSpecifiers() { return 0x0; }
    }

    public static class BoolType extends AbstractPrimitiveType implements IIntegerType {
        public BoolType(Machine machine, int specifiers, int qualifiers) { super(machine, specifiers, qualifiers, 0); }

        public String getName() { return "_Bool"; }
        public int getSize() { return machine.settings.getCharSize(); }

        public int getAllowedSpecifiers() { return 0x0; }
        public int getDefaultSpecifiers() { return 0x0; }

        public BigInteger getMinValue() { return BigInteger.valueOf(0); }
        public BigInteger getMaxValue() { return BigInteger.valueOf(1); }
    }

    public static class CharType extends AbstractPrimitiveType implements IIntegerType {
        private int size = -1;

        public CharType(Machine machine, int specifiers, int qualifiers) { super(machine, specifiers, qualifiers,1); }

        public String getName() { return "char"; }
        public int getSize() {
            if (size != -1) return size;
            size = machine.settings.getCharSize();
            return size;
        }

        public int getAllowedSpecifiers() { return TypeSpecifier.SIGNED | TypeSpecifier.UNSIGNED; }
        public int getDefaultSpecifiers() { return TypeSpecifier.UNSIGNED; }

        public BigInteger getMinValue() {
            if ((TypeSpecifier.UNSIGNED & specifiers) != 0)
                return BigInteger.valueOf(0);
            return BigInteger.valueOf(2).pow(getSize() * 8 - 1).negate();
        }

        public BigInteger getMaxValue() {
            if ((TypeSpecifier.UNSIGNED & specifiers) != 0)
                return BigInteger.valueOf(2).pow(getSize() * 8).subtract(BigInteger.valueOf(1));
            return BigInteger.valueOf(2).pow(getSize() * 8 - 1).subtract(BigInteger.valueOf(1));
        }
    }

    public static class IntType extends AbstractPrimitiveType implements IIntegerType {
        private int size = -1;

        public IntType(Machine machine, int specifiers, int qualifiers) { super(machine, specifiers, qualifiers, getPriority(specifiers)); }

        private static int getPriority(int specifiers) {
            if ((specifiers & TypeSpecifier.SHORT) != 0) return 2;
            if ((specifiers & TypeSpecifier.LONG) != 0)  return 4;
            if ((specifiers & TypeSpecifier.LONG_LONG) != 0) return 5;
            return 3;
        }

        public String getName() {
            if ((TypeSpecifier.SHORT & specifiers) != 0)
                return "short";
            else if ((TypeSpecifier.LONG & specifiers) != 0)
                return "long";
            else if ((TypeSpecifier.LONG_LONG & specifiers) != 0)
                return "long long";
            return "int";
        }
        public int getSize() {
            if (size != -1) return size;
            if ((TypeSpecifier.SHORT & specifiers) != 0)
                size = machine.settings.getShortSize();
            else if ((TypeSpecifier.LONG & specifiers) != 0)
                size = machine.settings.getLongSize();
            else if ((TypeSpecifier.LONG_LONG & specifiers) != 0)
                size = machine.settings.getLongLongSize();
            else
                size = machine.settings.getIntSize();
            return size;
        }

        public int getAllowedSpecifiers() { return TypeSpecifier.ALL; }
        public int getDefaultSpecifiers() { return 0x0; }

        public BigInteger getMinValue() {
            if ((TypeSpecifier.UNSIGNED & specifiers) != 0)
                return BigInteger.valueOf(0);
            return BigInteger.valueOf(2).pow(getSize() * 8 - 1).negate();
        }

        public BigInteger getMaxValue() {
            if ((TypeSpecifier.UNSIGNED & specifiers) != 0)
                return BigInteger.valueOf(2).pow(getSize() * 8).subtract(BigInteger.valueOf(1));
            return BigInteger.valueOf(2).pow(getSize() * 8 - 1).subtract(BigInteger.valueOf(1));
        }
    }

    public static class FloatType extends AbstractPrimitiveType implements IDecimalType {
        protected MathContext mathContext;

        public FloatType(Machine machine, int specifiers, int qualifiers) { super(machine, specifiers, qualifiers,6); }
        public FloatType(Machine machine, int specifiers, int qualifiers, int castPriority) { super(machine, specifiers, qualifiers, castPriority); }

        public String getName() { return "float"; }
        public int getSize() { return totalBitSize() / 8; }

        public int getAllowedSpecifiers() { return 0x0; }
        public int getDefaultSpecifiers() { return 0x0; }

        public int mantissaBitSize() { return machine.settings.getFloatMantissa(); }
        public int exponentBitSize() { return machine.settings.getFloatExponent(); }
        public int totalBitSize() { return machine.settings.getFloatSize(); }

        public BigDecimal getSmallestNonZeroValue() {
            // 2^(-m) * 2^(2^-(n-1) + 2)
            createMathContextIfNecessary();
            int exponent = -(int)Math.pow(2, exponentBitSize() - 1) + 2;
            return new BigDecimal(2).pow(-mantissaBitSize(), mathContext).multiply(new BigDecimal(2).pow(exponent, mathContext));
        }
        public BigDecimal getLargestValue() {
            // (2 - 2^(-mantissa size)) * 2^(2^(exponent bit size) - 1) = 1.999.... * 2^(2^(n-1) - 1)
            createMathContextIfNecessary();
            BigDecimal base = new BigDecimal(2).subtract(new BigDecimal(2).pow(-mantissaBitSize(), mathContext));
            int exponent = (int)Math.pow(2, exponentBitSize() - 1) - 1;
            return base.multiply(new BigDecimal(2).pow(exponent, mathContext));
        }

        public MathContext getMathContext() {
            createMathContextIfNecessary();
            return mathContext;
        }

        private void createMathContextIfNecessary() {
            if (mathContext == null)
                mathContext = new MathContext(getSize() * 8, RoundingMode.HALF_UP);
        }
    }

    public static class DoubleType extends FloatType {
        private final boolean isLong;

        public DoubleType(Machine machine, int specifiers, int qualifiers) {
            super(machine, specifiers, qualifiers, getPriority(specifiers));
            isLong = (specifiers & TypeSpecifier.LONG) != 0;
        }

        private static int getPriority(int specifiers) {
            return ((specifiers & TypeSpecifier.LONG) != 0) ? 8 : 7;
        }

        public String getName() { return (isLong  ? "long " : "") + "double"; }

        public int getAllowedSpecifiers() { return TypeSpecifier.LONG; }
        public int getDefaultSpecifiers() { return 0x0; }

        public int mantissaBitSize() { return isLong ? machine.settings.getLongDoubleMantissa() : machine.settings.getDoubleMantissa(); }
        public int exponentBitSize() { return isLong ? machine.settings.getDoubleExponent() : machine.settings.getDoubleExponent(); }
    }
}
