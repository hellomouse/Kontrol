package net.hellomouse.c_interp.types.interfaces;

import java.math.BigDecimal;
import java.math.MathContext;

public interface IDecimalType {
    int mantissaBitSize();
    int exponentBitSize();
    int totalBitSize();

    BigDecimal getSmallestNonZeroValue();
    BigDecimal getLargestValue();

    MathContext getMathContext();

    default boolean canFit(BigDecimal value) {
        return value.abs().compareTo(getSmallestNonZeroValue()) >= 0 && value.abs().compareTo(getLargestValue()) <= 0;
    }
}
