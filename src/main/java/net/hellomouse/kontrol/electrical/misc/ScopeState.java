package net.hellomouse.kontrol.electrical.misc;

import net.hellomouse.kontrol.util.Units;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.PersistentState;


/**
 * A state to store & save oscilloscope data
 * @author Bowserinator
 */
public class ScopeState extends PersistentState {
    // By default, height of display screen = 20 [unit]
    // This value is scaled by yScale
    public static final int DEFAULT_SCALE_HEIGHT = 20;
    public final ScopeGraphics graphics;

    private int maxReadings;
    private int[] readings;

    private String unit = Units.VOLT;
    private float timeScale = 1.0f;  // Acts like a multiplier for the sample rate in practice
    private float yScale = 1.0f;
    private float yDelta = 0.0f;

    // What index of readings is considered the first reading
    // New readings overwrite from left to right and shift this value
    private int dataStart = 0;

    // Texture needs update?
    private boolean requireUpdate = true;

    /**
     * Construct a new scope state
     * @param id Unique id
     * @param maxReadings Max readings to keep before discarding (>= 1)
     * @param graphics ScopeGraphics data
     */
    public ScopeState(String id, int maxReadings, ScopeGraphics graphics) {
        super(id);
        this.maxReadings = maxReadings;
        this.graphics = graphics;
        this.clear();
    }

    /**
     * Forcibly update data. Only dataStart and readings are required
     * as these are synced from server => client, while the rest can
     * be sent client => server
     * @param dataStart Data start index
     * @param readings Readings array
     */
    public void forceUpdate(int dataStart, int[] readings) {
        requireUpdate = true;
        this.dataStart = dataStart;
        this.readings = readings;
    }

    /**
     * Appends a new reading, added to readings[] array. If array is full
     * will begin overwriting beginning, and will update dataStart accordingly.
     * The effect is a graph that's always shifted leftwards a new entry is added.
     *
     * @param reading Reading value
     */
    public void addReading(double reading) {
        // Scale double to a pixel coordinate offset from the bottom axis
        // (This is absolute and ignores yDelta)
        float maxValue = DEFAULT_SCALE_HEIGHT / yScale;
        int scaled = (int)((reading) / maxValue * graphics.displayHeightInternal);

        dataStart = (dataStart + 1) % readings.length;
        readings[dataStart] = scaled;
        requireUpdate = true;
        markDirty();
    }

    /** Delete all readings */
    public void clear() {
        readings = new int[maxReadings];
        dataStart = 0;
        requireUpdate = true;
        markDirty();
    }

    /**
     * Set the X (time) and Y (y) scale. This will clear all readings.
     * @param timeScale How much to scale x axis
     * @param yScale How much to scale y axis
     */
    public void setScale(float timeScale, float yScale) {
        if (timeScale == this.timeScale && yScale == this.yScale)
            return;
        this.timeScale = timeScale;
        this.yScale = yScale;
        requireUpdate = true;
        clear();
    }

    /**
     * Sets the unit to display
     * @param unit A string, recommended you use constants defined in util/Units
     * @see net.hellomouse.kontrol.util.Units
     */
    public void setUnit(String unit) {
        this.unit = unit;
        requireUpdate = true;
        markDirty();
    }

    /**
     * Sets offset on y axis, this is an absolute offset that will be scaled
     * based on yScale
     * @param yDelta offset
     */
    public void setYDelta(float yDelta) {
        this.yDelta = yDelta;
        requireUpdate = true;
        markDirty();
    }

    /**
     * Load state from tag
     * @param tag Compound tag
     */
    public void fromTag(CompoundTag tag) {
        maxReadings = tag.getInt("maxReadings");
        readings    = tag.getIntArray("readings");
        timeScale   = tag.getFloat("timeScale");
        yScale      = tag.getFloat("yScale");
        yDelta      = tag.getFloat("yDelta");
        dataStart   = tag.getInt("dataStart");
    }

    /**
     * Save state to tag
     * @param tag Tag
     * @return Tag with new data
     */
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt("maxReadings", maxReadings);
        tag.putIntArray("readings", readings);
        tag.putFloat("timeScale", timeScale);
        tag.putFloat("yScale", yScale);
        tag.putFloat("yDelta", yDelta);
        tag.putInt("dataStart", dataStart);
        return tag;
    }

    /**
     * Run in ScopeRenderer when the texture is reconstructed.
     * Not recommended to call elsewhere as it might cancel graph updates.
     */
    public void finishedUpdating() { requireUpdate = false; }

    /**
     * Does the texture for the state require updating? (State changed?)
     * @return Requires update
     */
    public boolean requiresUpdate() { return requireUpdate; }

    /**
     * Get readings, stored as integer offset from bottom axis on chart
     * This means this only stores data to plot the graph (y = displayHeight - readings[i])
     * and not any of the original data points.
     * @return Readings
     */
    public int[] getReadings() { return readings; }

    public int getMaxReadings() { return maxReadings; }

    public String getUnit() { return unit; }

    public float getTimeScale() { return timeScale; }

    public float getYScale() { return yScale; }

    public float getYDelta() { return yDelta; }

    public int getDataStart() { return dataStart; }


    /**
     * Graphics config for the scope data. Not saved to tag as this should be a constant that depends
     * on the type of scope rather than per scope.
     * @author Bowserinator
     */
    public static class ScopeGraphics {
        public final int displayHeight;
        public final int displayWidth;
        public final int[] padding;
        public final int displayWidthInternal;
        public final int displayHeightInternal;

        public final int backgroundColor;
        public final int borderColor;
        public final int waveFormColor;
        public final int gridColor;
        public final int axisColor;

        public final int xDivisions;
        public final int yDivisions;

        /**
         * Construct ScopeGraphics
         * @param width Physical width of the texture (including padding) in px
         * @param height Physical height of the texture (including padding) in px
         * @param padding Padding array of length 4 representing padding for [left, right, top, bottom] in px
         *
         * @param backgroundColor ABGR hex int for background color
         * @param borderColor ABGR hex int for the border bounding the data region
         * @param waveFormColor ABGR hex int for the waveform
         * @param gridColor ABGR hex int for the grid division color
         * @param axisColor ABGR hex int for X/Y axis
         *
         * @param xDivisions Number of grid divisions on X axis
         * @param yDivisions Number of grid divisions on Y axis
         */
        public ScopeGraphics(int width, int height, int[] padding, int backgroundColor, int borderColor, int waveFormColor, int gridColor, int axisColor, int xDivisions, int yDivisions) {
            this.displayWidth = width;
            this.displayHeight = height;

            if (padding.length != 4)
                throw new IllegalStateException("Padding should be of length 4 (left, right, top, bottom), got " + padding.length);
            this.padding = padding;
            this.displayWidthInternal = this.displayWidth - padding[0] - padding[1];
            this.displayHeightInternal = this.displayHeight - padding[2] - padding[3];

            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
            this.waveFormColor = waveFormColor;
            this.gridColor = gridColor;
            this.axisColor = axisColor;

            this.xDivisions = xDivisions;
            this.yDivisions = yDivisions;
        }
    }
}
