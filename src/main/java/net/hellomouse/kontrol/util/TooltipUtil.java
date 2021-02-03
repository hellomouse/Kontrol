package net.hellomouse.kontrol.util;

import net.hellomouse.kontrol.Kontrol;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.List;

/**
 * Utilities for generating common tooltips
 * @author Bowserinator
 */
public class TooltipUtil {
    /**
     * Adds a default <Press Shift> tooltip if shift is not
     * currently held. Returns if shift is held down, which
     * allows you to code like this:
     *
     * <pre>{@code
     * if (TooltipUtil.shiftTooltip(tooltip) {
     *     tooltip.add(myTooltip); // Displays when shift is held
     * }
     * }</pre>
     *
     * @param tooltip Tooltip list
     * @return Is shift held?
     */
    public static boolean shiftTooltip(List<Text> tooltip) {
        if (Screen.hasShiftDown())
            return true;
        tooltip.add(new TranslatableText("item." + Kontrol.MOD_ID + ".tooltip.shift"));
        return false;
    }

    private TooltipUtil() {}
}
