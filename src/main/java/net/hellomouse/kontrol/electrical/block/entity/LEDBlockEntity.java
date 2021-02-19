package net.hellomouse.kontrol.electrical.block.entity;

import net.hellomouse.kontrol.electrical.block.AbstractLightBlock;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;


/**
 * Light emitting block that behaves like a diode
 * @author Bowserinator
 */
public class LEDBlockEntity extends DiodeBlockEntity {
    public static LEDData WHITE, RED, GREEN, BLUE, YELLOW, PURPLE, CYAN;
    public static LEDData[] COLORS;
    static {
        WHITE  = new LEDData(3.3, 0xaaaaaa, 0xffffff, "white");
        RED    = new LEDData(1.7, 0x660000, 0xeb4d42, "red");
        GREEN  = new LEDData(2.2, 0x006600, 0x5be02f, "green");
        BLUE   = new LEDData(3.5, 0x000066, 0x3030ff, "blue");
        YELLOW = new LEDData(2.1, 0x666600, 0xffe600, "yellow");
        PURPLE = new LEDData(3.2, 0x660066, 0xff00ff, "purple");
        CYAN   = new LEDData(3.2, 0x006666, 0x00ffff, "cyan");
        COLORS = new LEDData[]{ WHITE, RED, GREEN, BLUE, YELLOW, PURPLE, CYAN };
    }

    public LEDBlockEntity() {
        super(ElectricalBlockRegistry.LED_BLOCK_ENTITY);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (!(block instanceof AbstractLightBlock))
            throw new IllegalStateException("Block at " + pos + " has LEDBlockEntity, but doesn't extend AbstractLEDBlock");
        if (nodalVoltages.size() != 2 || circuit == null)
            return;

        double voltage = Math.abs(nodalVoltages.get(0) - nodalVoltages.get(1));
        double current = Math.abs(internalCircuit.getComponents().get(0).getCurrent());
        double power = Math.abs(voltage * current);

        int brightness = ((AbstractLightBlock) block).getBrightness(voltage, current, power, thermal.temperature);
        if (brightness < 0) brightness = 0;
        else if (brightness > 15) brightness = 15;
        world.setBlockState(pos, blockState.with(AbstractLightBlock.BRIGHTNESS, brightness));
    }

    /**
     * Data storage for LED variants
     * @author Bowserinator
     */
    public static class LEDData {
        public final double forwardVoltage;
        public final int onColor, offColor;
        public final String name;

        /**
         * Construct LEDData
         * @param forwardVoltage Forward voltage, positive value (V), ie 0.7 V
         * @param offColor 0xRRGGBB hex int
         * @param onColor  0xRRGGBB hex int
         * @param name     Name of color as lowercase string, ie "cyan"
         */
        public LEDData(double forwardVoltage, int offColor, int onColor, String name) {
            this.forwardVoltage = forwardVoltage;
            this.offColor = offColor;
            this.onColor = onColor;
            this.name = name;
        }
    }
}
