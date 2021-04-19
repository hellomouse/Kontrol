package net.hellomouse.kontrol.electrical.microcontroller.C8051;

import net.minecraft.util.Pair;

public class C8051PortUtil {
    public static Pair<Integer, Integer> getPortAndBitFromAddress(int address) {
        switch (address) {
            case 0x80: return new Pair<>(0, 0);
            case 0x81: return new Pair<>(0, 1);
            case 0x82: return new Pair<>(0, 2);
            case 0x83: return new Pair<>(0, 3);
            case 0x84: return new Pair<>(0, 4);
            case 0x85: return new Pair<>(0, 5);
            case 0x86: return new Pair<>(0, 6);
            case 0x87: return new Pair<>(0, 7);

            case 0x90: return new Pair<>(1, 0);
            case 0x91: return new Pair<>(1, 1);
            case 0x92: return new Pair<>(1, 2);
            case 0x93: return new Pair<>(1, 3);
            case 0x94: return new Pair<>(1, 4);
            case 0x95: return new Pair<>(1, 5);
            case 0x96: return new Pair<>(1, 6);
            case 0x97: return new Pair<>(1, 7);

            case 0xA0: return new Pair<>(2, 0);
            case 0xA1: return new Pair<>(2, 1);
            case 0xA2: return new Pair<>(2, 2);
            case 0xA3: return new Pair<>(2, 3);
            case 0xA4: return new Pair<>(2, 4);
            case 0xA5: return new Pair<>(2, 5);
            case 0xA6: return new Pair<>(2, 6);
            case 0xA7: return new Pair<>(2, 7);

            case 0xB0: return new Pair<>(3, 0);
            case 0xB1: return new Pair<>(3, 1);
            case 0xB2: return new Pair<>(3, 2);
            case 0xB3: return new Pair<>(3, 3);
            case 0xB4: return new Pair<>(3, 4);
            case 0xB5: return new Pair<>(3, 5);
            case 0xB6: return new Pair<>(3, 6);
            case 0xB7: return new Pair<>(3, 7);
        }
        return new Pair<>(-1, -1);
        // throw new IllegalStateException("Unknown sbit port"); // TODO: other sbits for P4-P7
    }

    public static final int getMdoutPortIdFromPortAndBitOffset(int virtualPortId) {
        switch (virtualPortId) {
            // 1.X
            case 8 * 1 + 6: return 4;
            case 8 * 1 + 4: return 6;
            case 8 * 1 + 2: return 10;
            case 8 * 1 + 0: return 12;

            case 8 * 1 + 7: return 5;
            case 8 * 1 + 5: return 9;
            case 8 * 1 + 3: return 11;
            case 8 * 1 + 1: return 13;

            // 0.X
            case 6: return 14;
            case 4: return 16;
            case 2: return 18;
            case 0: return 20;

            case 7: return 15;
            case 5: return 17;
            case 3: return 19;
            case 1: return 21;

            // 2.X
            case 8 * 2 + 6: return 22;
            case 8 * 2 + 4: return 24;
            case 8 * 2 + 3: return 28;
            case 8 * 2 + 1: return 30;

            case 8 * 2 + 7: return 23;
            case 8 * 2 + 5: return 25;
            case 8 * 2 + 2: return 27;
            case 8 * 2 + 0: return 29;

            // 3.X
            case 8 * 3 + 7: return 32;
            case 8 * 3 + 5: return 34;
            case 8 * 3 + 3: return 36;
            case 8 * 3 + 0: return 38;

            case 8 * 3 + 6: return 31;
            case 8 * 3 + 4: return 33;
            case 8 * 3 + 2: return 35;
            case 8 * 3 + 1: return 37;
        }
        return -1;
    }
}
