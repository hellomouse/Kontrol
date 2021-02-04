package net.hellomouse.kontrol.util;

/**
 * Various custom String formatting utils
 * @author Bowserinator
 */
public class FormatUtil {
    /** Assumes is well balanced (no prefix is in middle) */
    private final static String[] SI_PREFIXES = { "y", "z", "a", "f", "p", "n", "µ", "m", "", "k", "M", "G", "T", "P", "E", "Z", "Y" };

    /**
     * Format a value as SI notation, ie 0.01 -> 10 mUnit. If value is too large or small,
     * or is zero, it will just be formatted as [value w/ precision] [unit]
     *
     * @param value Value to format
     * @param precision Decimal places to format to
     * @param unit Unit to append to end of string, ie "V"
     * @return Formatted String
     */
    public static String SIFormat(double value, int precision, String unit) {
        final String format = String.format("%." + precision + "f %s", value, unit);
        if (value == 0.0) // Return 0.0...
            return format;

        int engMagnitude = (int)(Math.floor(Math.log10(Math.abs(value)) / 3));
        double pow10 = Math.pow(10.0, engMagnitude * 3);

        /* Numbers get rounded during formatting, so, ie, 0.99999999 V => 1000 mV instead of 1 V
         * This manually fixes that by checking if number is close enough to 1000 to simply round it */
        if (Math.abs(Math.abs(value / pow10) - 1000) < (precision == 0 ? 1 : 0.5 / precision)) {
            engMagnitude++;
            pow10 = Math.pow(10.0, engMagnitude * 3);
            value = pow10;
        }

        final int prefixIndex = engMagnitude + SI_PREFIXES.length / 2;

        if (prefixIndex >= 0 && prefixIndex < SI_PREFIXES.length)
            return String.format("%." + precision + "f %s%s", value / pow10, SI_PREFIXES[prefixIndex], unit);
        return format;
    }

    private FormatUtil() {}
}
