package net.hellomouse.kontrol.electrical.block.entity;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.hellomouse.kontrol.electrical.circuit.CircuitValues;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualResistor;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.LogManager;


public class ResistorBlockEntity extends AbstractPolarizedElectricalBlockEntity implements RenderAttachmentBlockEntity, BlockEntityClientSerializable {

    // TODO: compute resistance somehow based on temperature
    protected double resistance = 1.0;

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
        setResistance(resistance);
        return this;
    }

    public void setResistance(double resistance, boolean dirty) {
        this.resistance = resistance;
        if (this.resistance <= 0.0) {
            LogManager.getLogger().warn("Attempted to set resistor at " + getPos() + " to invalid resistance " + resistance);
            this.resistance = CircuitValues.LOW_VOLTAGE_RESISTANCE; // TODO: default values
        }

        if (dirty) {
            if (nodalVoltages.size() == 2)
                ((VirtualResistor)internalCircuit.getComponents().get(0)).setResistance(resistance);
            markDirty();
        }
    }

    public void setResistance(double resistance) {
        setResistance(resistance, true);
    }

    public double getResistance() { return resistance; }

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
        setResistance(tag.getDouble("Resistance"), false);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putDouble("Resistance", resistance);
        return super.toTag(tag);
    }

    public void fromClientTag(CompoundTag tag) {
        setResistance(tag.getDouble("Resistance"), false);
    }

    public CompoundTag toClientTag(CompoundTag tag) {
        tag.putDouble("Resistance", resistance);
        return tag;
    }

    public Object getRenderAttachmentData() {
        return resistance;
    }
}
