package net.hellomouse.c_interp.common.storage_types.base;

import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;

import java.math.BigDecimal;
import java.math.MathContext;

public class FloatBaseTypeStorage extends BaseTypeStorage {
    public final BigDecimal smallestValue, largestValue;
    public final int mantissaSize, exponentSize;

    public FloatBaseTypeStorage(String name, int mantissaSize, int exponentSize, int castPriority) {
        super(name, (mantissaSize + exponentSize + 1) / 8, castPriority, Primitive.FLOAT);

        this.mantissaSize = mantissaSize;
        this.exponentSize = exponentSize;

        MathContext mathContext = new MathContext(mantissaSize);

        // 2^(-m) * 2^(2^-(n-1) + 2)
        int exponent = -(int)Math.pow(2, exponentSize - 1) + 2;
        smallestValue = new BigDecimal(2).pow(-mantissaSize, mathContext).multiply(new BigDecimal(2).pow(exponent, mathContext));

        // (2 - 2^(-mantissa size)) * 2^(2^(exponent bit size) - 1) = 1.999.... * 2^(2^(n-1) - 1)
        BigDecimal base = new BigDecimal(2).subtract(new BigDecimal(2).pow(-mantissaSize, mathContext));
        exponent = (int)Math.pow(2, exponentSize - 1) - 1;
        largestValue = base.multiply(new BigDecimal(2).pow(exponent, mathContext));
    }

    @Override
    public BaseTypeStorage copy() {
        FloatBaseTypeStorage copy = new FloatBaseTypeStorage(name, mantissaSize, exponentSize, castPriority);
        copy.allowedTypeSpecifiers = allowedTypeSpecifiers;
        copy.defaultTypeSpecifiers = defaultTypeSpecifiers;
        return copy;
    }

    @Override
    public String getFullName(){
        // TODO: append all long (specifiers)
        return name;
    }

    public BigDecimal normalize(BigDecimal value) {
        // Value is too small, => 0
        if (value.abs().compareTo(smallestValue) < 0)
            return new BigDecimal(0);

        // Value is too large, => INF
        if (value.compareTo(largestValue) > 0)
            return ConstantValue.POS_INFINITY;
        if (value.compareTo(largestValue.negate()) < 0)
            return ConstantValue.NEG_INFINITY;

        return value;
    }
}
