package net.hellomouse.kontrol.electrical.block.entity;

import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class BuzzerBlockEntity extends ResistorBlockEntity {
    private float pitch = 1.0f;
    private float voltageThreshold = 0.5f;

    public BuzzerBlockEntity() {
        super(ElectricalBlockRegistry.BUZZER_BLOCK_ENTITY);
        setRotate(true);
    }

    public BuzzerBlockEntity pitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    public BuzzerBlockEntity voltageThreshold(float voltageThreshold) {
        this.voltageThreshold = voltageThreshold;
        return this;
    }

    @Override
    public void onUpdate() {
        if (internalCircuit.getComponents().size() > 0 && Math.abs(internalCircuit.getComponents().get(0).getVoltage()) > voltageThreshold && !world.isClient) {
            world.playSound(null, this.pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 1f, pitch);
        }
    }
}
