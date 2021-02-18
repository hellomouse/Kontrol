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
    public LEDBlockEntity() {
        super(ElectricalBlockRegistry.LED_BLOCK_ENTITY);
    }

    @Override
    public void onUpdate() {
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
}
