package net.hellomouse.kontrol.registry.util;

import net.minecraft.block.MaterialColor;

import java.util.HashMap;


/**
 * Dye color helpers
 * @author Bowserinator
 * @version 1.0
 */
public class ColorData {
    public enum COLOR_STRING {
        WHITE("white"),
        RED("red"),
        ORANGE("orange"),
        PINK("pink"),
        YELLOW("yellow"),
        LIME("lime"),
        GREEN("green"),
        LIGHT_BLUE("light_blue"),
        CYAN("cyan"),
        BLUE("blue"),
        MAGENTA("magenta"),
        PURPLE("purple"),
        BROWN("brown"),
        GRAY("gray"),
        LIGHT_GRAY("light_gray"),
        BLACK("black");

        private final String text;

        COLOR_STRING(final String text) { this.text = text; }

        @Override
        public String toString() { return text; }
    }

    public static final HashMap<COLOR_STRING, Integer> DYEABLE_COLORS;
    static {
        DYEABLE_COLORS = new HashMap<>();
        DYEABLE_COLORS.put(COLOR_STRING.WHITE, 0xffffff);
        DYEABLE_COLORS.put(COLOR_STRING.RED, 0x993333);
        DYEABLE_COLORS.put(COLOR_STRING.ORANGE, 0xd87f33);
        DYEABLE_COLORS.put(COLOR_STRING.PINK, 0xf27fa5);
        DYEABLE_COLORS.put(COLOR_STRING.YELLOW, 0xe5e533);
        DYEABLE_COLORS.put(COLOR_STRING.LIME, 0x7fcc19);
        DYEABLE_COLORS.put(COLOR_STRING.GREEN, 0x667f33);
        DYEABLE_COLORS.put(COLOR_STRING.LIGHT_BLUE, 0x6699d8);
        DYEABLE_COLORS.put(COLOR_STRING.CYAN, 0x4c7f99);
        DYEABLE_COLORS.put(COLOR_STRING.BLUE, 0x334cb2);
        DYEABLE_COLORS.put(COLOR_STRING.MAGENTA, 0xb24cd8);
        DYEABLE_COLORS.put(COLOR_STRING.PURPLE, 0x7f3fb2);
        DYEABLE_COLORS.put(COLOR_STRING.BROWN, 0x664c33);
        DYEABLE_COLORS.put(COLOR_STRING.GRAY, 0x4c4c4c);
        DYEABLE_COLORS.put(COLOR_STRING.LIGHT_GRAY, 0x999999);
        DYEABLE_COLORS.put(COLOR_STRING.BLACK, 0x191919);
    }

    /**
     * Maps a COLOR_STRING => MaterialColor
     * @param color Color string
     * @return MaterialColor
     */
    public static MaterialColor nameToMaterialColor(COLOR_STRING color) {
        switch(color) {
            case WHITE: return MaterialColor.WHITE;
            case RED: return MaterialColor.RED;
            case ORANGE: return MaterialColor.ORANGE;
            case PINK: return MaterialColor.PINK;
            case YELLOW: return MaterialColor.YELLOW;
            case LIME: return MaterialColor.LIME;
            case GREEN: return MaterialColor.GREEN;
            case LIGHT_BLUE: return MaterialColor.LIGHT_BLUE;
            case CYAN: return MaterialColor.CYAN;
            case BLUE: return MaterialColor.BLUE;
            case MAGENTA: return MaterialColor.MAGENTA;
            case PURPLE: return MaterialColor.PURPLE;
            case BROWN: return MaterialColor.BROWN;
            case GRAY: return MaterialColor.GRAY;
            case LIGHT_GRAY: return MaterialColor.LIGHT_GRAY;
            case BLACK: return MaterialColor.BLACK;
        }
        throw new IllegalStateException("Color is invalid: received " + color + ", but doesn't exist.");
    }
}
