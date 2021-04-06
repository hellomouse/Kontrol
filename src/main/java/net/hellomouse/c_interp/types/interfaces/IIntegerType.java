package net.hellomouse.c_interp.types.interfaces;

import java.math.BigInteger;

public interface IIntegerType {
    BigInteger getMinValue();
    BigInteger getMaxValue();

    default boolean canFit(BigInteger value) {
        return value.compareTo(getMinValue()) >= 0 && value.compareTo(getMaxValue()) <= 0;
    }
}
