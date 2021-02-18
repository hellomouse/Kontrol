package net.hellomouse.kontrol.electrical.block.entity;

import net.hellomouse.kontrol.electrical.circuit.CircuitValues;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;


public class SwitchBlockEntity extends ResistorBlockEntity {
    private boolean open = true;

    public SwitchBlockEntity() {
        super(ElectricalBlockRegistry.SWITCH_BLOCK_ENTITY);
        setRotate(true);
    }

    public void toggle() {
        open = !open;
        if (this.circuit != null) {
            setResistance(open ? CircuitValues.HIGH_RESISTANCE : CircuitValues.LOW_RESISTANCE);
            circuit.markDirty();
        }
    }

    public boolean isOpen() { return open; }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        open = tag.getBoolean("open");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putBoolean("open", open);
        return super.toTag(tag);
    }
}
