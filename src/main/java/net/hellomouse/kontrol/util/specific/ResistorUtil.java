package net.hellomouse.kontrol.util.specific;

import net.hellomouse.kontrol.electrical.block.entity.ResistorBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;


/**
 * Utilities related to resistors, ie calculating resistor band colors
 * @author Bowserinator
 */
public class ResistorUtil {
    // Color for invalid bands or resistances
    public static final int INVALID_COLOR = 0x333333;

    /**
     * Returns resistor color for tintIndex given a block
     * @param state Block state of the block
     * @param view World
     * @param pos Position of the block
     * @param tintIndex Band to tint, 0 <= Band index <= 3
     * @return Int representing hex color 0xRRGGBB
     */
    public static int getColorForBlock(BlockState state, BlockRenderView view, BlockPos pos, int tintIndex) {
        BlockEntity blockEntity = view.getBlockEntity(pos);
        if (blockEntity instanceof ResistorBlockEntity) {
            Object data = ((ResistorBlockEntity)blockEntity).getRenderAttachmentData();
            if (data != null)
                return getColor((Double)data, tintIndex);
        }
        return ResistorUtil.INVALID_COLOR;
    }

    /**
     * Returns resistor color for tintIndex given an item stack
     * @param stack ItemStack for a resistor
     * @param tintIndex Band to tint, 0 <= Band index <= 3
     * @return Int representing hex color 0xRRGGBB
     */
    public static int getColorForItemStack(ItemStack stack, int tintIndex) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag != null && tag.contains("Resistance"))
            return getColor(tag.getDouble("Resistance"), tintIndex);
        return ResistorUtil.INVALID_COLOR;
    }

    /**
     * Computes the tint for a given resistance
     * @param resistance Resistance in ohms
     * @param tintIndex 0 <= Band index <= 3
     * @return Int representing hex color 0xRRGGBB
     */
    public static int getColor(double resistance, int tintIndex) {
        int pow10 = (int)Math.floor(Math.log10(resistance) / 3) * 3;
        double val = resistance / Math.pow(10.0, pow10);

        if (tintIndex == 0)
            return colorFromNumber((int)val / 100);
        if (tintIndex == 1)
            return colorFromNumber(((int)val % 100) / 10);
        if (tintIndex == 2)
            return colorFromNumber((int)val % 10);
        if (tintIndex == 3)
            return colorFromNumber(pow10);
        return INVALID_COLOR;
    }

    /**
     * Returns a color given a digit
     * @param digit 0 <= digit <= 9
     * @return int representing an 0xRRGGBB color
     */
    private static int colorFromNumber(int digit) {
        // Colors usd are different than those in ColorData to
        // better represent the resistor band colors
        switch (digit) {
            case 0: return 0x0;      // Black
            case 1: return 0x9b3400; // Brown
            case 2: return 0xde0000; // Red
            case 3: return 0xfb9742; // Orange
            case 4: return 0xffff00; // Yellow
            case 5: return 0x1caf4c; // Green
            case 6: return 0x0070c3; // Blue
            case 7: return 0x8f66ff; // Violet
            case 8: return 0x7e7e7e; // Gray
            case 9: return 0xffffff; // White
            case -1: return 0xffd700; // Gold
            case -2: return 0xf1f2f1; // Silver
        }
        return INVALID_COLOR;
    }

    private ResistorUtil() {}
}
