package net.hellomouse.kontrol.registry;

import net.minecraft.block.MaterialColor;

import java.util.HashMap;

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

    public static MaterialColor nameToMaterialColor(String color) {
        if (color.equals("white")) return MaterialColor.WHITE;
        if (color.equals("red")) return MaterialColor.RED;
        if (color.equals("orange")) return MaterialColor.ORANGE;
        if (color.equals("pink")) return MaterialColor.PINK;
        if (color.equals("yellow")) return MaterialColor.YELLOW;
        if (color.equals("lime")) return MaterialColor.LIME;
        if (color.equals("green")) return MaterialColor.GREEN;
        if (color.equals("light_blue")) return MaterialColor.LIGHT_BLUE;
        if (color.equals("cyan")) return MaterialColor.CYAN;
        if (color.equals("blue")) return MaterialColor.BLUE;
        if (color.equals("magenta")) return MaterialColor.MAGENTA;
        if (color.equals("purple")) return MaterialColor.PURPLE;
        if (color.equals("brown")) return MaterialColor.BROWN;
        if (color.equals("gray")) return MaterialColor.GRAY;
        if (color.equals("light_gray")) return MaterialColor.LIGHT_GRAY;
        if (color.equals("black")) return MaterialColor.BLACK;
        throw new IllegalStateException("Color is invalid: received " + color + ", but doesn't exist.");
    }
}
