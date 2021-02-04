package net.hellomouse.kontrol.electrical.items.multimeters;

import net.hellomouse.kontrol.Kontrol;
import net.hellomouse.kontrol.util.FormatUtil;
import net.hellomouse.kontrol.util.Units;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;


/**
 * Reading data for a multimeter sample. Can be auto-formatted
 * for different tiers of multimeters.
 *
 * To construct and set a value, use a pattern like the one below:
 * <pre>@code{
 * new MultimeterReading()
 *      .voltage(...)
 *      .current(...);
 * }</pre>
 *
 * To get a List of Minecraft Text type for a reading, use:
 * <pre>@code{
 * myElectricalBlockEntity
 *      .getReading()
 *      .parse(FLAGS, precision)
 * }</pre>
 *
 * @author Bowserinator
 */
public class MultimeterReading {
    // Storage info
    private double
        voltage = Double.NaN,
        current = Double.NaN,
        temperature = 0.0,
        power = Double.NaN,
        absoluteVoltage = Double.NaN;

    private List<Text> misc = null;
    private List<Double> nodalVoltages = null;
    private List<Integer> nodeIds = null;
    private String blockType;
    private boolean error = false;

    private Direction positive = null,
                      negative = null;

    // Flags for parsing
    public static final int
        FLAG_VOLTAGE     = 0x1,
        FLAG_CURRENT     = 0x2,
        FLAG_TEMPERATURE = 0x4,
        FLAG_DELTA_V     = 0x8,  // Voltage difference, not velocity
        FLAG_POWER       = 0x10,
        FLAG_POLARITY    = 0x20,
        FLAG_ABSOLUTE    = 0x40, // Meant for debugging tools only (ie creative mode)
        FLAG_NODE_IDS    = 0x80, // Meant for debugging tools only (ie creative mode)
        FLAG_MISC        = 0x100,
        FLAG_COLORED     = 0x200,
        FLAG_NAME        = 0x400; // Meant for debugging tools only
    public static final int FLAG_ALL =
        FLAG_VOLTAGE | FLAG_CURRENT | FLAG_TEMPERATURE | FLAG_ABSOLUTE | FLAG_NODE_IDS | FLAG_MISC | FLAG_COLORED | FLAG_NAME | FLAG_DELTA_V | FLAG_POWER | FLAG_POLARITY;


    /**
     * Returns a list of text to be rendered, generated from the data.
     * Different multimeter tiers should pass different flags. The following
     * flags do the following:
     *
     * - FLAG_VOLTAGE     : Display voltage at central node of component
     * - FLAG_CURRENT     : Display current through component
     * - FLAG_TEMPERATURE : Display temperature of component
     * - FLAG_DELTA_V     : Display voltage across component
     * - FLAG_POWER       : Power generated / dissipated by component
     * - FLAG_ABSOLUTE    : Display all absolute internal node voltages (Meant for debugging)
     * - FLAG_NODE_IDS    : Display internal circuit nodal ids (Meant for debugging)
     * - FLAG_MISC        : Display any other Text added by the component
     * - FLAG_COLORED     : Render some of the above text with color formatting
     * - FLAG_NAME        : Render block entity class name (Meant for debugging)
     *
     * Flags can be combined with bitwise OR, ie a thermometer might have flags:
     *     FLAGS_TEMPERATURE | FLAGS_COLORED
     *
     * If the reading is marked as error, returns an default error message
     *
     * @param flags Flags to render with
     * @param precision How many decimal places to use?
     * @return Text to render line after line
     */
    public ArrayList<Text> parse(int flags, int precision) {
        ArrayList<Text> lines = new ArrayList<>();

        if (error) {
            lines.add(new TranslatableText("item." + Kontrol.MOD_ID + ".multimeter.no_circuit_error"));
            return lines;
        }

        if ((flags & FLAG_NAME) != 0)
            lines.add(new LiteralText(Formatting.BOLD + "" + Formatting.GOLD +  blockType));

        StringJoiner infoLine = new StringJoiner(",  ");
        boolean infoLineExists = false;
        boolean color = (flags & FLAG_COLORED) != 0;

        if ((flags & FLAG_DELTA_V) != 0 && !Double.isNaN(voltage)) {
            infoLineExists = true;
            infoLine.add((color ? Formatting.GOLD.toString() : "") +
                    FormatUtil.SIFormat(voltage, precision, Units.DELTA_VOLT));
        }
        if ((flags & FLAG_VOLTAGE) != 0 && !Double.isNaN(absoluteVoltage)) {
            infoLineExists = true;
            infoLine.add((color ? Formatting.LIGHT_PURPLE.toString() : "") +
                    FormatUtil.SIFormat(absoluteVoltage, precision, Units.ABS_VOLT));
        }
        if ((flags & FLAG_CURRENT) != 0 && !Double.isNaN(current)) {
            infoLineExists = true;
            infoLine.add((color ? Formatting.AQUA.toString() : "") +
                    FormatUtil.SIFormat(current, precision, Units.AMP));
        }
        if ((flags & FLAG_POWER) != 0 && !Double.isNaN(power)) {
            infoLineExists = true;
            infoLine.add((color ? Formatting.RED.toString() : "") +
                    FormatUtil.SIFormat(power, precision, Units.WATT));
        }
        if ((flags & FLAG_TEMPERATURE) != 0 && !Double.isNaN(temperature)) {
            infoLineExists = true;
            infoLine.add((color ? Formatting.DARK_RED.toString() : "") +
                    String.format("%." + precision + "f", temperature) + " " + Units.CELSIUS);
        }

        if ((flags & FLAG_POLARITY) != 0 && positive != null && negative != null) {
            infoLineExists = true;

            // Works on ENUM names, ie NORTH
            String posDir = positive.toString().substring(0, 1).toUpperCase();
            String negDir = negative.toString().substring(0, 1).toUpperCase();

            infoLine.add((color ? Formatting.RED.toString() : "") + posDir + "+ " +
                         (color ? Formatting.AQUA.toString() : "") + negDir + "-");
        }

        if (infoLineExists)
            lines.add(new LiteralText(infoLine.toString()));

        if ((flags & FLAG_ABSOLUTE) != 0 && nodalVoltages != null) {
            Text array = new LiteralText(Arrays.toString(
                    nodalVoltages.stream().map(v -> FormatUtil.SIFormat(v, precision, Units.VOLT)).toArray()));
            lines.add(new TranslatableText("item." + Kontrol.MOD_ID + ".multimeter.nodal_voltages").append(array));
        }

        if ((flags & FLAG_NODE_IDS) != 0 && nodeIds != null) {
            Text array = new LiteralText(nodeIds.toString());
            lines.add(new TranslatableText("item." + Kontrol.MOD_ID + ".multimeter.nodal_ids").append(array));
        }

        if ((flags & FLAG_MISC) != 0 && misc != null)
            lines.addAll(misc);

        return lines;
    }

    /**
     * Applies a corruption effect based on how damaged the item is
     * @param lines Lines to be altered in-place with corrupted text
     * @param damage How damaged is the multimeter? Will affect results. Float from 0 to 1 (1 = 100% durability)
     */
    public static void damageLines(List<Text> lines, float damage) {
        for (int i = 0; i < lines.size(); i++) {
            Text text = lines.get(i);

            // Make sure damage values match with the JSON predicates
            if (damage > 0.6)
                lines.set(i, new LiteralText( corruptString(text.getString(), 0.5f) ));
            else if (damage > 0.4)
                lines.set(i, new LiteralText( corruptString(text.getString(), 0.1f) ));
        }
    }

    /**
     * Randomly adds regions of obfuscated characters to a string. Will not insert
     * the obfuscation formatting if it breaks an existing formatting code, although it may
     * remove existing formatting.
     *
     * @param str String to corrupt
     * @param chance Frequency of toggling, number 0 to 1 (1 = always, 0 = never)
     *               Lower numbers decrease overall corruption, but create longer stretches.
     *               Larger numbers have more overall but shorter bursts
     * @return Corrupted string
     */
    private static String corruptString(String str, float chance) {
        final String[] corrupt = { Formatting.OBFUSCATED.toString(), Formatting.RESET.toString() };
        StringBuilder result = new StringBuilder();
        int strIndex = 0;  // Toggle between the 2 corruption

        for (int i = 0; i < str.length(); i++) {
            if (Math.random() < chance && !(str.charAt(i) == '§' || (i > 0 && str.charAt(i - 1) == '§')) ) {
                result.append(corrupt[strIndex]);
                strIndex = 1 - strIndex;
            }
            result.append(str.charAt(i));
        }
        return result.toString();
    }

    public MultimeterReading() {}

    public MultimeterReading voltage(double voltage) {
        this.voltage = voltage;
        return this;
    }

    public MultimeterReading current(double current) {
        this.current = current;
        return this;
    }

    public MultimeterReading temperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    public MultimeterReading power(double power) {
        this.power = power;
        return this;
    }

    public MultimeterReading misc(List<Text> misc) {
        this.misc = misc;
        return this;
    }

    public MultimeterReading nodalVoltages(List<Double> nodalVoltages) {
        this.nodalVoltages = nodalVoltages;
        return this;
    }

    public MultimeterReading absoluteVoltage(double absoluteVoltage) {
        this.absoluteVoltage = absoluteVoltage;
        return this;
    }

    public MultimeterReading nodeIds(List<Integer> nodeIds) {
        this.nodeIds = nodeIds;
        return this;
    }

    public MultimeterReading blockType(String type) {
        this.blockType = type;
        return this;
    }

    public MultimeterReading polarity(Direction positive, Direction negative) {
        this.positive = positive;
        this.negative = negative;
        return this;
    }

    public MultimeterReading error() {
        this.error = true;
        return this;
    }
}
