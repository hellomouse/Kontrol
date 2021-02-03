package net.hellomouse.kontrol.electrical.block.entity;

import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualResistor;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;

public class ResistorBlockEntity extends AbstractPolarizedElectricalBlockEntity {

    // TODO: compute resistance somehow based on temperature
    private double resistance = 1.0;

    public ResistorBlockEntity() { super(ElectricalBlockRegistry.RESISTOR_BLOCK_ENTITY); }

    /** For use in classes that extend this */
    public ResistorBlockEntity(BlockEntityType<?> entityType) { super(entityType); }

    /**
     * Sets resistance and returns this. Lets you use a pattern like this:
     * return new ResistorBlockEntity().resistance(x)
     * @param resistance Resistance value (ohms)
     * @return this
     */
    public ResistorBlockEntity resistance(double resistance) {
        this.resistance = resistance;
        return this;
    }

    public void onUpdate() {
        // TODO Not for all resistors, temporary
        if (nodalVoltages.size() != 2)
            return;

        double voltage = Math.abs(nodalVoltages.get(1) - nodalVoltages.get(0));
        double current = voltage / resistance;
        double power = voltage * current;

        int brightness = (int)(power * 10000);
        if (brightness < 0) brightness = 0;
        if (brightness > 15) brightness = 15;

        // this.world.setBlockState(this.pos, world.getBlockState(pos).with(LEDBlock.BRIGHTNESS, brightness), 3);
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

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        resistance = tag.getDouble("resistance");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putDouble("resistance", resistance);
        return super.toTag(tag);
    }
}
