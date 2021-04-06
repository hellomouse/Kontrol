package net.hellomouse.c_interp.common;

import java.util.logging.Logger;

public class MachineSettings {
    public static final Logger logger = Logger.getLogger("MachineSettings");

    public static final int INVALID_SIZE = -1;

    private boolean charIsSigned = false;

    private int memorySize = -1;
    private int functionSize = -1;

    // Size of primitives
    private int pointerSize = 4;
    private int boolSize = 1;

    private int charSize = 1;
    private int shortSize = 2;
    private int intSize = 4;
    private int longSize = 8;
    private int longLongSize = 12;

    private int floatMantissa = 23;
    private int floatExponent = 8;
    private int doubleMantissa = 52;
    private int doubleExponent = 11;
    private int longDoubleMantissa = 62;
    private int longDoubleExponent = 15;

    private boolean locked = false;

    public MachineSettings memorySize(int size, int pointerSize) {
        checkLock();
        this.memorySize = size;
        this.pointerSize = pointerSize;
        if (Math.pow(2, pointerSize * 8) < size) {
            logger.warning("Not all memory locations are addressable because pointer size is too low\n" +
                    "Pointer size of " + pointerSize + " bytes can address " + Math.pow(2, pointerSize * 8) + " addresses, total memory size is " + memorySize);
        }
        return this;
    }

    public MachineSettings integerTypeSizes(int charSize, int shortSize, int intSize, int longSize, int longLongSize) {
        checkLock();

        if (charSize < 1 || shortSize < 1 || intSize < 1 || longSize < 1 || longLongSize < 1)
            throw new IllegalStateException("All sizes must be >= 1, sizes are as given: char = " + charSize + ", short = " + shortSize + ", int = " +
                    intSize + ", long = " + longSize + ", long long = " + longLongSize);

        if (shortSize < 2)
            logger.warning("Short size should be at least 2 bytes, got " + shortSize + " bytes");
        if (intSize < 2)
            logger.warning("Int size should be at least 2 bytes, got " + intSize + " bytes");
        if (longSize < 4)
            logger.warning("Long size should be at least 4 bytes, got " + longSize + " bytes");
        if (longLongSize < 8)
            logger.warning("Long long size should be at least 8 bytes, got " + longLongSize + " bytes");

        this.charSize = charSize;
        this.shortSize = shortSize;
        this.intSize = intSize;
        this.longSize = longSize;
        this.longLongSize = longLongSize;
        return this;
    }

    public MachineSettings floatTypeSize(int floatMantissa, int floatExponent) {
        checkLock();
        this.floatMantissa = floatMantissa;
        this.floatExponent = floatExponent;

        int size = (1 + floatExponent + floatMantissa);
        if (size % 8 != 0)
            logger.warning("Float size of " + size + " bits should be a multiple of 8");
        return this;
    }

    public MachineSettings doubleTypeSize(int doubleMantissa, int doubleExponent) {
        checkLock();
        this.doubleMantissa = doubleMantissa;
        this.doubleExponent = doubleExponent;

        int size = (1 + doubleExponent + doubleMantissa);
        if (size % 8 != 0)
            logger.warning("Double size of " + size + " bits should be a multiple of 8");
        return this;
    }

    public MachineSettings longDoubleTypeSize(int longDoubleMantissa, int longDoubleExponent) {
        checkLock();
        this.longDoubleMantissa = longDoubleMantissa;
        this.longDoubleExponent = longDoubleExponent;

        int size = (1 + longDoubleExponent + longDoubleMantissa);
        if (size % 8 != 0)
            logger.warning("Long double size of " + size + " bits should be a multiple of 8");
        return this;
    }

    public MachineSettings functionSize(int functionSize) {
        checkLock();
        this.functionSize = functionSize;
        return this;
    }

    public MachineSettings boolSize(int boolSize) {
        checkLock();
        this.boolSize = boolSize;
        return this;
    }

    public MachineSettings charIsSigned(boolean charIsSigned) {
        checkLock();
        this.charIsSigned = charIsSigned;
        return this;
    }

    public boolean isCharSigned() { return charIsSigned; }

    public int getMemorySize() { return memorySize; }
    public int getPointerSize() { return pointerSize; }
    public int getInvalidSize() { return INVALID_SIZE; }
    public int getFunctionSize() { return functionSize; }
    public int getBoolSize() { return boolSize; }

    public int getCharSize() { return charSize; }
    public int getShortSize() { return shortSize; }
    public int getIntSize() { return intSize; }
    public int getLongSize() { return longSize; }
    public int getLongLongSize() { return longLongSize; }


    public int getFloatMantissa() { return floatMantissa; }
    public int getFloatExponent() { return floatExponent; }
    public int getDoubleMantissa() { return doubleMantissa; }
    public int getDoubleExponent() { return doubleExponent; }
    public int getLongDoubleMantissa() { return longDoubleMantissa; }
    public int getLongDoubleExponent() { return longDoubleExponent; }

    public int getFloatSize() { return 1 + getFloatExponent() + getFloatMantissa(); }
    public int getDoubleSize() { return 1 + getDoubleExponent() + getDoubleMantissa(); }
    public int getLongDoubleSize() { return 1 + getLongDoubleExponent() + getLongDoubleMantissa(); }

    public void lock() { locked = true; }

    private void checkLock() {
        if (locked) throw new IllegalStateException("MachineSettings instance is locked, cannot be changed");
    }

}
