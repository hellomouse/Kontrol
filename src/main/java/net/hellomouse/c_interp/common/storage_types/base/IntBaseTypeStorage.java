package net.hellomouse.c_interp.common.storage_types.base;

import net.hellomouse.c_interp.common.specifiers.TypeSpecifier;

import java.math.BigInteger;

/**
 * An integer base type
 * @author Bowserinator
 */
public class IntBaseTypeStorage extends BaseTypeStorage {
    private BigInteger minValue, maxValue;
    private boolean unsigned = false;

    /**
     * Construct an integer base type
     * @param name Name of the base type without specifiers. For 'long', 'short' and 'long long', this
     *             name should be 'int'.
     * @param size Size of the type, in bytes
     * @param castPriority Priority when casting, higher number = higher priority
     */
    public IntBaseTypeStorage(String name, int size, int castPriority) {
        super(name, size, castPriority, Primitive.INTEGER);
    }

    @Override
    public BaseTypeStorage copy() {
        IntBaseTypeStorage copy = new IntBaseTypeStorage(name, size, castPriority);
        copy.allowedTypeSpecifiers = allowedTypeSpecifiers;
        copy.defaultTypeSpecifiers = defaultTypeSpecifiers;
        return copy;
    }

    @Override
    public void setDefaultTypeSpecifiers(int specifiers) {
        super.setDefaultTypeSpecifiers(specifiers);

        this.unsigned = (specifiers & TypeSpecifier.UNSIGNED) != 0;

        // Max unsigned = 2^(size * 8) - 1
        // Max signed   = 2^(size * 8 - 1) - 1
        this.maxValue = unsigned ?
                BigInteger.valueOf(2).pow(getSize() * 8).subtract(BigInteger.valueOf(1)) :
                BigInteger.valueOf(2).pow(getSize() * 8 - 1).subtract(BigInteger.valueOf(1));

        // Min unsigned = 0
        // Min signed   = -2^(size * 8 - 1)
        this.minValue = unsigned ?
                BigInteger.valueOf(0) :
                BigInteger.valueOf(2).pow(getSize() * 8 - 1).negate();
    }

    @Override
    public void postCheck() {
        this.unsigned = (this.getTypeSpecifiers() & TypeSpecifier.UNSIGNED) != 0;

        // Max unsigned = 2^(size * 8) - 1
        // Max signed   = 2^(size * 8 - 1) - 1
        this.maxValue = unsigned ?
                BigInteger.valueOf(2).pow(getSize() * 8).subtract(BigInteger.valueOf(1)) :
                BigInteger.valueOf(2).pow(getSize() * 8 - 1).subtract(BigInteger.valueOf(1));

        // Min unsigned = 0
        // Min signed   = -2^(size * 8 - 1)
        this.minValue = unsigned ?
                BigInteger.valueOf(0) :
                BigInteger.valueOf(2).pow(getSize() * 8 - 1).negate();
    }

    @Override
    public String getFullName(){
        // TODO: append all long unsigned (specifiers)
        return name;
    }

    public BigInteger getMaxValue() { return maxValue; }
    public BigInteger getMinValue() { return minValue; }

    public boolean isUnsigned() { return unsigned; }

    public boolean canFit(BigInteger value) {
        return value.compareTo(minValue) >= 0 && value.compareTo(maxValue) <= 0;
    }

    /**
     * Normalize a value to within the range of the integer type, simulating
     * what would happen in overflow.
     * @param value Value to normalize
     * @return Normalized value
     */
    public BigInteger normalize(BigInteger value) {
        BigInteger twoPower = (BigInteger.valueOf(2)).pow(size * 8);
        BigInteger halfPower = (BigInteger.valueOf(2)).pow(size * 8 - 1);

        if (value.compareTo(BigInteger.ZERO) < 0) {
            if (value.compareTo(minValue) >= 0)
                return value;
            if (unsigned)
                return value.mod(twoPower);

            BigInteger temp = minValue.subtract(value).divide(twoPower).add(BigInteger.ONE);
            return value.add(temp.multiply(twoPower));
        }
        else {
            if (value.compareTo(maxValue) <= 0)
                return value;
            if (unsigned)
                return value.mod(twoPower);

            BigInteger temp = value.subtract(maxValue).divide(twoPower).add(BigInteger.ONE);
            return value.subtract(temp.multiply(twoPower));
        }
    }
}
