package net.hellomouse.kontrol.electrical.block.entity;

import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualResistor;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;

public class SwitchBlockEntity extends ResistorBlockEntity {
    private boolean open = true;

    public SwitchBlockEntity() { super(ElectricalBlockRegistry.SWITCH_BLOCK_ENTITY); }

    public void onUpdate() {
        if (nodalVoltages.size() != 2)
            return;

        ((VirtualResistor)internalCircuit.getComponents().get(0)).setResistance(open ? 1e9 : resistance);
    }

    public void toggle() {
        open = !open;
        if (this.circuit != null) circuit.markDirty();
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

    @Override
    public VirtualCircuit getInternalCircuit() {
        internalCircuit.clear();
        if (normalizedOutgoingNodes.size() == 2) {
            sortOutgoingNodesByPolarity();
            internalCircuit.addComponent(new VirtualResistor(resistance), normalizedOutgoingNodes.get(0), normalizedOutgoingNodes.get(1));
        }
        return internalCircuit;
    }
}
