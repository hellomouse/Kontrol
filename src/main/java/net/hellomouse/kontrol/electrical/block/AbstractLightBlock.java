package net.hellomouse.kontrol.electrical.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;


/**
 * Abstract electrical light emitting block
 * @author Bowserinator
 */
public abstract class AbstractLightBlock extends AbstractPolarizedElectricalBlock {
    public static final IntProperty BRIGHTNESS = Properties.POWER;

    public AbstractLightBlock(AbstractBlock.Settings settings) {
        super(settings, true);
        setDefaultState(getStateManager().getDefaultState().with(BRIGHTNESS, 0));
    }

    /**
     * Get the BRIGHTNESS property based on the variables below, called from LightBlockEntity
     * @param voltage Voltage across component
     * @param current Current through component
     * @param power Power dissipated by component
     * @param temperature Temperature of component
     * @return Brightness value to set, 0 - 15
     */
    public abstract int getBrightness(double voltage, double current, double power, double temperature);

    /**
     * Get tint color (for item / default)
     * @return 0xRRGGBB hex color
     */
    public abstract int getColor();

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager){
        super.appendProperties(stateManager);
        stateManager.add(BRIGHTNESS);
    }
}
